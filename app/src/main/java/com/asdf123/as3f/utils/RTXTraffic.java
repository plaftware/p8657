package com.asdf123.as3f.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asdf123.as3f.di.ActivityContext;

import java.text.DecimalFormat;

import javax.inject.Inject;

/**
 * Created by l2 on 11/01/17.
 */

public class RTXTraffic implements Runnable {

    private Handler mHandler;

    private final Context context;

    private Activity activity;

    boolean isStaring;

    Long traffic0;
    long traffic, traffic1;

    public interface RTXTrafficListener {

        void trafficReport(long traffic);
    }

    private RTXTrafficListener rtxTrafficListener;

    @Inject
    public RTXTraffic(@ActivityContext Context activityContext) {
        this.context = activityContext;
        this.mHandler = new Handler();
    }

    @Override
    public void run() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (rtxTrafficListener != null) {
                        if(traffic0 == null){
                            traffic0 = getTraffic();
                        }

                        traffic1 = traffic  + (getTraffic() - traffic0);
                        rtxTrafficListener.trafficReport(traffic1);
                    }
                } finally {
                    mHandler.postDelayed(RTXTraffic.this, 300);
                }
            }
        });
    }

    private long getTraffic(){
        /*long mobileData = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
        return TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() - mobileData;
        */
        return TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
    }

    private void queryTraffic() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.traffic = sharedPref.getLong("traffic", 0);
    }

    private void saveTraffic() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("traffic", this.traffic1); // Bytes
        editor.commit();
        this.traffic0 = null;
    }

    public void start(Activity activity) {
        if (!isStaring) {
            this.activity = activity;
            this.queryTraffic();
            this.run();
            isStaring = true;
        }
    }

    public void stop() {
        if (isStaring) {
            mHandler.removeCallbacks(this);
            this.saveTraffic();
            isStaring = false;
        }
    }

    public String format(long bytes) {
        return android.text.format.Formatter.formatFileSize(activity, bytes);
    }

    public RTXTrafficListener getRtxTrafficListener() {
        return rtxTrafficListener;
    }

    public void setRtxTrafficListener(RTXTrafficListener rtxTrafficListener) {
        this.rtxTrafficListener = rtxTrafficListener;
    }
}
