package com.asdf123.as3f.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.asdf123.as3f.BuildConfig;
import com.asdf123.as3f.ui.activity.MainActivity;

/**
 * Created by G on 12/30/2016.
 */

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Intent newintent = new Intent(MainActivity.myMainActivity, as3fService.class);
                if (as3fService.current_status == Util.STATUS_CONNECTING || as3fService.current_status == Util.STATUS_SOCKS) {
                    if(!BuildConfig.TEST_MODE) {
                        as3fService.toState = Util.STATUS_DISCONNECT;
                        MainActivity.myMainActivity.startService(newintent);
                        Log.d("WifiReceiver", "Now Have Wifi Connection");
                    }
                }
            }
        }catch (Exception e){

        }
    }
}