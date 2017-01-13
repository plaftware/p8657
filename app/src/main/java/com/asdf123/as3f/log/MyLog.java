package com.asdf123.as3f.log;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by l2 on 9/01/17.
 */

public class MyLog {

    protected static LinkedList<String> log = new LinkedList<>();
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");

    public static void d(String tag, String txt) {
        Log.d(tag, txt);
        log.add("[ " + simpleDateFormat.format(new Date()) + " ]" + " " +txt);
    }

    public static void e(String tag, String txt) {
        Log.e(tag, txt);
        log.add("[ " + simpleDateFormat.format(new Date()) + " ]" + " " +txt);
    }

    public static void e(String tag, String txt, Exception e) {
        Log.e(tag, txt,e);
        log.add("[ " + simpleDateFormat.format(new Date()) + " ]" + " " +txt);
        log.add("[ " + simpleDateFormat.format(new Date()) + " ]" + " " +"|Exception - " + e.toString());
    }

    public static void i(String tag, String txt) {
        Log.i(tag, txt);
        log.add("[ " + simpleDateFormat.format(new Date()) + " ]" + " " +txt);
    }

/*    public static synchronized void append(String s) {
        s = s.replace("com.asdf123", "");
        s = s.replace("as3f", "anonymousVPN");
        if (log.length() + s.length() > MAX_LENGTH) {
            log.delete(0, log.length() + s.length() - MAX_LENGTH);
        }
        log.append(s, Math.max(0, s.length() - MAX_LENGTH), s.length());
    }*/

    public static LinkedList<String> dump() {
        return log;
    }

}
