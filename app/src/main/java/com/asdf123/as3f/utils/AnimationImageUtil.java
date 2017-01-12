package com.asdf123.as3f.utils;

import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;

import com.asdf123.as3f.R;

/**
 * Created by l2 on 11/01/17.
 */

public class AnimationImageUtil implements Runnable{

    private FloatingActionButton floatingActionButton;
    private int time;
    private int[] img;

    private Handler mHandler;
    private Activity activity;
    private int counter;

    private boolean starting;

    public AnimationImageUtil(Activity activity, FloatingActionButton floatingActionButton, int time, int... img) {
        this.activity = activity;
        this.floatingActionButton = floatingActionButton;
        this.time = time;
        this.img = img;
        mHandler = new Handler();
    }

    public void start(){
        if(!starting){
            run();
            starting = true;
        }
    }

    public void stop(){
        if(starting){
            mHandler.removeCallbacks(this);
            counter = 0;
            starting = false;
        }
    }

    @Override
    public void run() {
       activity.runOnUiThread(new Runnable() {
           @Override
           public void run() {
               try {
                   floatingActionButton.setImageResource(img[counter++]);
                   if(counter >= img.length){
                       counter = 0;
                   }
               } finally {
                   mHandler.postDelayed(AnimationImageUtil.this, time);
               }
           }
       });
    }
}
