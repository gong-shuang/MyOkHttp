package com.gs.app.util;

import android.util.Log;

public class Logger {
    public static final String TAG = "Logger";
    public static void info(String str){
        Log.d(TAG,str);
    }
}
