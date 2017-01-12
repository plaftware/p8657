package com.asdf123.as3f.log;

import android.util.Log;

/**
 * Created by l2 on 9/01/17.
 */

public class MyLog {

    protected static StringBuffer log = new StringBuffer();
    protected static final int MAX_LENGTH = 20480; // 20k text

    public static void d(String tag, String txt) {
        Log.d(tag, txt);
        append(txt + "<br>");
    }

    public static void e(String tag, String txt) {
        Log.e(tag, txt);
        append("<font color='#c57731'>" + txt + "</font><br>");
    }

    public static void e(String tag, String txt, Exception e) {
        Log.e(tag, txt,e);
        append("<font color='#c57731'>" + txt + "<br>Exception - " + e.toString() + "</font><br>");
    }

    public static void i(String tag, String txt) {
        Log.i(tag, txt);
        append("<i>" + txt + "</i><br>");
    }

    public static synchronized void append(String s) {
        s = s.replace("com.asdf123", "");
        s = s.replace("as3f", "anonymousVPN");
        if (log.length() + s.length() > MAX_LENGTH) {
            log.delete(0, log.length() + s.length() - MAX_LENGTH);
        }
        log.append(s, Math.max(0, s.length() - MAX_LENGTH), s.length());
    }

    public static String dump() {
        return log.toString();
    }

}
