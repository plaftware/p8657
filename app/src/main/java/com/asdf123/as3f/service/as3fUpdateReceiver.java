package com.asdf123.as3f.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class as3fUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.DeleteAssets();
        Util.CopyAssets(context.getAssets(), context.getPackageName());
    }
}
