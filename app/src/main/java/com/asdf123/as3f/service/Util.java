package com.asdf123.as3f.service;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by l2 on 9/01/17.
 */

public class Util {

    public static final String TAG = "AnonymousVPN";
    public static final String BASE = "/data/data/com.asdf123.as3f/as3f";
    public static final String BASE_BIN = "/bin";
    public static final int STATUS_DISCONNECT = 0;
    public static final int STATUS_INIT = 1;
    public static final int STATUS_CONNECTING = 2;
    public static final int STATUS_SOCKS = 4;
    public static String tunVPN_IP = "26.26.26.1";
    public static String socksVPN_IP = "26.26.26.2";
    public static int tunVPN_mask_num = 24;
    public static String tunVPN_mask = "255.255.255.0";
    public static int localSocksPort = 7777;
    public static int tunVPN_MTU = 1500;

    public static boolean hasRoot()
    {
        final boolean[] hasRoot = new boolean[1];
        Thread t = new Thread(){
            @Override
            public void run(){
                hasRoot[0] = Shell.SU.available();
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return hasRoot[0];
    }

    public static int runChainFireRootCommand(String command, boolean fork) {
        return runChainFireCommand(command, true, fork, false);
    }

    public static int runChainFireCommand(String command, boolean fork, boolean ask4retval) {
        return runChainFireCommand(command, false, fork, ask4retval);
    }

    public static int runChainFireCommand(String command, boolean fork) {
        return runChainFireCommand(command, false, fork, false);
    }

    public static int runChainFireCommand(String command, boolean root, boolean fork, boolean ask4retval) {
        final int[] retval = new int[1];
        if(fork){
            final String myCommand = command;
            final boolean myRoot = root;
            final boolean myAsk4retval = ask4retval;
            Thread t = new Thread(){
                @Override
                public void run(){
                    if(myRoot) {
                        Shell.run("su", new String[]{myCommand}, new String[]{}, false);
                        retval[0] = 0;
                    }
                    else {
                        if(myAsk4retval) {
                            try {
                                retval[0] = Integer.parseInt(Shell.run("sh", new String[]{myCommand + "; echo $?"}, new String[]{}, false).get(0));
                            } catch (Exception e) {
                                retval[0] = 0;
                            }
                        } else {
                            Shell.run("sh", new String[]{myCommand}, new String[]{}, false);
                            retval[0] = 0;
                        }
                    }
                }
            };
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            if(root) {
                Shell.run("su", new String[]{command}, new String[]{}, false);
                retval[0] = 0;
            }
            else {
                if(ask4retval) {
                    try {
                        retval[0] = Integer.parseInt((Shell.run("sh", new String[]{command + "; echo $?"}, new String[]{}, false)).get(0));
                    } catch (Exception e) {
                        retval[0] = 0;
                    }
                } else {
                    Shell.run("sh", new String[]{command + "; echo $?"}, new String[]{}, false);
                    retval[0] = 0;
                }
            }
        }
        return retval[0];
    }

    protected static boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(2000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                }
            } catch (MalformedURLException e1) {
                //MyLog.i(TAG, "Couldn't connect [Malformed] " + e1.toString());
            } catch (IOException e) {
                //MyLog.i(TAG, "Couldn't connect " + e.toString());
            }
        }
        return false;
    }

    protected static void refreshMobileData()
    {
        Util.runChainFireRootCommand("settings put global airplane_mode_on 1;" +
                "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true", true);

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Util.runChainFireRootCommand("settings put global airplane_mode_on 0;" +
                "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", true);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static void prepareInterfaces(WifiManager wifiManager, TelephonyManager telephonyManager)
    {
        // If wifi is On, we need to turn it off first
        if(wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(as3fService.preferences.getBoolean("cellular_switch",true) &&
                as3fService.preferences.getBoolean("airplane_switch",true))
        {
            // If data connection is not connected, we'll try to bring it up
            if (telephonyManager.getDataState() != telephonyManager.DATA_CONNECTED &&
                    telephonyManager.getDataState() != telephonyManager.DATA_CONNECTING)
                Util.refreshMobileData();
        }
    }

    protected static void copyFileOrDir(String path, AssetManager assetManager, String packageName) {
        String assets[];
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path, assetManager, packageName);
            } else {
                String fullPath = "/data/data/" + packageName + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i], assetManager, packageName);
                }
            }
        } catch (IOException ex) {
            //MyLog.e(TAG, "I/O Exception", ex);
        }
    }

    protected static void copyFile(String filename, AssetManager assetManager, String packageName) {
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + packageName + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            //MyLog.e(TAG, "Exception when copying asset", e);
        }
    }

    protected static void DeleteAssets()
    {
        File as3f_folder = new File(BASE);
        if(as3f_folder.exists()) {
            String[] children = as3f_folder.list();
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals("id_rsa") ||
                        children[i].equals("header_file"))
                    continue;
                //MyLog.d(TAG, "Deleting = " + children[i]);
                deleteDir(new File(as3f_folder, children[i]));
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    public static void CopyAssets(AssetManager assetManager, String packageName)
    {
        String arch = System.getProperty("os.arch");
        String bin_path = "/arm";
        if(arch.contains("arm"))
        {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
                bin_path = "/arm_nopie";
            else
                bin_path = "/arm";
        }
        else if(arch.contains("86"))
        {
            bin_path = "/x86";
        }
        //else bin_path remains on the default value "/arm"

        //Check if as3f folder exist and create it if not
        File as3f_folder = new File(BASE);
        if(!as3f_folder.exists()) as3f_folder.mkdir();

        Util.copyFileOrDir("as3f/redsocks.conf", assetManager, packageName);
        Util.copyFileOrDir("as3f/pdnsd.conf", assetManager, packageName);
        Util.copyFileOrDir("as3f" + bin_path, assetManager, packageName);
        new File(BASE + bin_path).renameTo(new File(BASE + BASE_BIN)); //Rename bin folder

        Util.runChainFireCommand("chmod 755 " + BASE + BASE_BIN + "/busybox;" +
                "chmod 755 " + BASE + BASE_BIN + "/korkscrew;" +
                "chmod 755 " + BASE + BASE_BIN + "/redsocks;" +
                "chmod 755 " + BASE + BASE_BIN + "/iptables;" +
                "chmod 755 " + BASE + BASE_BIN + "/ssh;" +
                "chmod 755 " + BASE + BASE_BIN + "/sshpass;" +
                "chmod 755 " + BASE + BASE_BIN + "/tun2socks;" +
                "chmod 755 " + BASE + BASE_BIN + "/pdnsd",true);
    }

    protected static boolean run_tun2socks(FileDescriptor tunfd, boolean forward_dns)
    {
        String localSocksFile = BASE + "/tunfd_file";
        Util.runChainFireCommand(BASE + BASE_BIN + "/tun2socks --netif-ipaddr " + socksVPN_IP +
                " --netif-netmask " + tunVPN_mask + " --socks-server-addr 127.0.0.1:" + localSocksPort +
                " --tunmtu " + tunVPN_MTU + (forward_dns? " --dnsgw " + tunVPN_IP + ":8153 ":"") + " --pid " + BASE + "/tun2socks.pid", false);
        //--dnsgw " + tunVPN_IP + ":8153
        //--loglevel 5

        //MyLog.d(TAG, "Let's send FD to tun2socks");
        LocalSocket clientSocket = new LocalSocket();
        try {
            Thread.sleep(500); //Let's wait just a little bit before looking for file
            // At this point, localSocksFile should be already created and waiting for us to send tunfd
            clientSocket.connect(new LocalSocketAddress(localSocksFile, LocalSocketAddress.Namespace.FILESYSTEM));
            if(!clientSocket.isConnected())
            {
                //MyLog.e(TAG,"Unable to connect to localSocksFile ["+localSocksFile+"]");
                return false;
            }

            OutputStream clientOutStream = clientSocket.getOutputStream();
            clientSocket.setFileDescriptorsForSend(new FileDescriptor[]{ tunfd });
            clientOutStream.write(32);

            clientSocket.setFileDescriptorsForSend(null);
            clientSocket.shutdownOutput();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
            //MyLog.e(TAG,"Unable to connect to localSocksFile ["+localSocksFile+"]");
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        //MyLog.d(TAG, "Done tun2socks RUN");
        return true;
    }

    protected static int getSshfd()
    {
        String localSSHfd = BASE + "/sshfd_file";
        int sshfd = 0;

        //MyLog.d(TAG, "Let's get sshfd");
        LocalSocket clientSocket = new LocalSocket();
        try {
            LocalSocketAddress localAdd = new LocalSocketAddress(localSSHfd, LocalSocketAddress.Namespace.FILESYSTEM);

            // localSSHfd should be created soon, let's wait a little bit to read sshfd
            for (int i=0; i<20; i++) {
                Thread.sleep(250); //Let's wait just a little bit before looking for file
                try {
                    clientSocket.connect(localAdd);
                }
                catch (IOException ioe) {} //Let's ignore it a couple of times

                if(clientSocket.isConnected()) break;
            }

            if(!clientSocket.isConnected())
            {
                //MyLog.e(TAG,"Unable to connect to localSSHfd [" + localSSHfd + "]");
                //Return an invalid sshfd
                return sshfd;
            }

            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();

            is.read(); //Read some data to get AncillaryFDs

            FileDescriptor[] fds = clientSocket.getAncillaryFileDescriptors();

            if(fds!=null && fds.length>0) {
                Method getInt = FileDescriptor.class.getDeclaredMethod("getInt$");
                sshfd = (int) getInt.invoke(fds[0]);

                os.write(0x49); //Ack
                is.close();
                os.close();
            }

            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
            //MyLog.e(TAG, "Unable to connect to localSSHfd [" + localSSHfd + "]");
            return sshfd;
        }

        //MyLog.d(TAG,"Got SSHfd ["+sshfd+"]");
        return sshfd;
    }

    protected static void startKi4aVPN(Context context, String prefix)
    {
        //MyLog.d(TAG, "Util.startVPN");
        Intent intentVpn = new Intent(context,as3fVPNService.class);
        intentVpn.putExtra(prefix + ".ACTION",as3fVPNService.FLAG_VPN_START);

        context.startService(intentVpn);
    }

    protected static void stopKi4aVPN(Context context, String prefix)
    {
        //MyLog.d(TAG, "Util.stopVPN");
        Intent intentVpn = new Intent(context,as3fVPNService.class);
        intentVpn.putExtra(prefix + ".ACTION",as3fVPNService.FLAG_VPN_STOP);

        context.startService(intentVpn);

        Util.runChainFireCommand(BASE + BASE_BIN + "/busybox killall -9 tun2socks", true); //kill everything using tun2socks
    }

    protected static void reportDisconnection(Context context) {
        // Notify Service about the connection failed
        // SSH may be hanged waiting for us, so let's kill it (and wait for reconnect if necessary)
        // tun2socks may need cleanup
        Util.runChainFireCommand(BASE + BASE_BIN + "/busybox killall -9 korkscrew;" + // Stop korkscrew
                BASE + BASE_BIN + "/busybox killall -9 ssh;" + // Stop SSH
                BASE + BASE_BIN + "/busybox killall -9 tun2socks" + //Stop tun2socks
                BASE + BASE_BIN + "/busybox killall pdnsd", true); // Stop DNS redirect
    }

    protected static void reportRevoked(Context context) {
        if(as3fService.current_status != Util.STATUS_DISCONNECT) {
            // Notify Service that we need to close the connection and change status to disconnect
            as3fService.toState = Util.STATUS_DISCONNECT;
            Intent intent = new Intent(context, as3fService.class);
            context.startService(intent);
        }
    }

    protected static boolean isKeyEncrypted(String key)
    {
        return key.contains("ENCRYPTED");
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "not found";
    }

    public static long getTraffic(){
        return TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
    }
}
