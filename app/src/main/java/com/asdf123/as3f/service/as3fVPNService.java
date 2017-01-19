package com.asdf123.as3f.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;

import com.asdf123.as3f.log.MyLog;

import java.io.IOException;
import java.util.List;

/**
 * Created by l2 on 9/01/17.
 */

public class as3fVPNService extends VpnService implements Runnable {

    protected static final String TAG = "AnonymousVPN";
    public static int FLAG_VPN_START = 0;
    public static int FLAG_VPN_STOP = 1;
    private ParcelFileDescriptor mInterface;
    private Thread mThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String prefix = getPackageName();
        int action = intent.getIntExtra(prefix + ".ACTION", 0);

        if(action==FLAG_VPN_START) {
            //MyLog.i(TAG, "Starting VPN tunnel");
            // Stop the previous session by interrupting the thread.
            if (mThread != null) {
                mThread.interrupt();
            }
            mThread = new Thread(this, "as3fVPNThread");
            mThread.start();
        }
        else if(action==FLAG_VPN_STOP) {
            this.onDestroy();
            stopSelf();
            MyLog.i(TAG, "Desconectado VPN");
            as3fService.vpn_ready = false;


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            long traffic = sharedPref.getLong("traffic", 0) + (Util.getTraffic() - sharedPref.getLong("traffic0", 0));
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("traffic", traffic);
            editor.commit();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //MyLog.i(TAG, "Closing");

        if (mThread != null) {
            mThread.interrupt();
        }
        if (mInterface != null){
            try {
                mInterface.close();
            } catch (IOException e) { /* Ignore a close error her
    e */ }
        }
    }

    @Override
    public void onRevoke() {
        // If vpn was revoked, let's close and destroy everything
        this.onDestroy();
        Util.reportRevoked(this);
    }

    @Override
    public void run() {
        //MyLog.i(TAG, "Starting");
        if(protect_tunnel())
        {
            configure();
            as3fService.vpn_ready = true; // Notify vpn is ready now
            MyLog.e(Util.TAG, "Conectado");

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("traffic0", Util.getTraffic());
            editor.commit();
        }
        else {
            //MyLog.i(TAG, "Failed to protect the tunnel");
            this.onDestroy();
            Util.reportDisconnection(this);
            stopSelf();
        }
    }

    private boolean protect_tunnel()
    {
        int fd;
        //MyLog.i(TAG, "Protecting SSH fd");
        fd = Util.getSshfd();
        if(fd==0) return false;
        protect(fd);
        return true;
    }

    private void configure()
    {
        //MyLog.i(TAG, "Configure");
        if (mInterface != null) {
            //MyLog.i(TAG, "Using the previous interface");
            return;
        }

        Builder builder = new Builder();
        builder.setSession("AnonymousVPN");
        builder.setMtu(Util.tunVPN_MTU);
        builder.addAddress(Util.tunVPN_IP, Util.tunVPN_mask_num);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("dns_switch", true)) builder.addDnsServer(preferences.getString("dns_server", "8.8.8.8"));
        //builder.addRoute("8.8.0.0", 16); //Redirect DNS stuff

        if(preferences.getBoolean("route_switch", true)) {
            //MyLog.d(Util.TAG,"Routing all traffic");
            builder.addRoute("0.0.0.0", 0); //Redirect all traffic
        }
        else {
            List<RouteInfo> infoList = RouteList.getRoutes(this);
            for (RouteInfo info : infoList) {
                try {
                    if (!info.getRoute_address().isEmpty() && !info.getPrefix_length().isEmpty()) {
                        //MyLog.d(Util.TAG,"Adding route " + info.getRoute_address() + "/" + info.getPrefix_length());
                        builder.addRoute(info.getRoute_address(), Integer.parseInt(info.getPrefix_length())); //Redirect all traffic
                    }
                } catch (Exception e) {
                    //MyLog.e(Util.TAG,"Fail to Add route " + info.getRoute_address() + "/" + info.getPrefix_length() + " - Skipping");
                    //MyLog.e(Util.TAG,e.getMessage());
                }
            }
            if(preferences.getBoolean("dns_switch", true)) {
                try {
                    //MyLog.d(Util.TAG, "Adding DNS route " + preferences.getString("dns_server", "8.8.8.8") + "/32");
                    builder.addRoute(preferences.getString("dns_server", "8.8.8.8"), 32); //Redirect DNS address
                } catch (Exception e) {
                    //MyLog.e(Util.TAG,"Fail to Add DNS route " +preferences.getString("dns_server", "8.8.8.8") + "/32 - Skipping");
                    //MyLog.e(Util.TAG,e.getMessage());
                }
            }
        }


        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            builder.allowFamily(android.system.OsConstants.AF_INET6);

        mInterface = builder.establish();

        if (mInterface == null || !Util.run_tun2socks(mInterface.getFileDescriptor(), PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dns_switch", true))) {
            // If we can not connect, let's close and destroy everything
            this.onDestroy();
            Util.reportDisconnection(this);
        }
    }
}
