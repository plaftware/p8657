package com.asdf123.as3f.resource.token;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import com.asdf123.as3f.BuildConfig;
import com.asdf123.as3f.di.ActivityContext;
import com.asdf123.as3f.resource.IndexIP;

import javax.inject.Inject;

/**
 * Created by l2 on 7/01/17.
 */

public class VPNManager {

    private final Context activityContext;

    private VPNListener vpnListener;

    public interface VPNListener{

        void startVPNService();

        boolean requiredPermission();

    }

    @Inject
    public VPNManager(@ActivityContext Context activityContext) {
        this.activityContext = activityContext;
    }

    public String nextSerial(){
        String devcieId = Settings.Secure.getString(this.activityContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        if(devcieId != null){
            devcieId = devcieId.replaceAll("[^a-zA-Z0-9]", "");
        }else{
            return null;
        }

        int hash = 97 * 7 + devcieId.hashCode();
        boolean m = hash < 0;
        return String.format("%s%010d", m ? "m" : "p", m ? hash * -1 : hash);
    }

    public void connect(String networkKey){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activityContext);

        if(!IndexIP.hasIP(networkKey)){
            Toast.makeText(this.activityContext, "Invalid network release", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("server_serialip", networkKey);
        editor.putString("server_text", IndexIP.getIP(networkKey));
        editor.commit();

        if(vpnListener != null && !vpnListener.requiredPermission()){
            vpnListener.startVPNService();
        }
    }

    public VPNListener getVpnListener() {
        return vpnListener;
    }

    public void setVpnListener(VPNListener vpnListener) {
        this.vpnListener = vpnListener;
    }
}
