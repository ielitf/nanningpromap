package com.ceiv.communication.utils;

import android.content.Context;
import android.util.Log;

/**
 * @auther wjt
 * @date 2019/4/25
 */
public class CrashHandler  implements Thread.UncaughtExceptionHandler {
    private static Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private static CrashHandler mCrashHandler = new CrashHandler();
    private Context mContext;
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.i("test", "出错了");
    }
    public static CrashHandler getInstance() {
        return mCrashHandler;
    }
    public void init(Context context) {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();

    }
}