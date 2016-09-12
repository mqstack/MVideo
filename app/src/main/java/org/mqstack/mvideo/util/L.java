package org.mqstack.mvideo.util;

import android.util.Log;

import org.mqstack.mvideo.BuildConfig;

/**
 * Created by mq on 16/6/12.
 */

public class L {

    private static String getClassName() {
        String result;
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[2];
        result = thisMethodStack.getClassName();
        int lastIndex = result.lastIndexOf(".");
        result = result.substring(lastIndex + 1, result.length());
        return result;
    }

    public static void d(String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            d(getClassName(), msg, args);
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            if (args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.d(tag, msg != null ? msg : "");
        }
    }
}
