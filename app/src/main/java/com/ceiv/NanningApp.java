package com.ceiv;

import android.app.Application;

import com.ceiv.communication.utils.CrashHandler;

/**
 * @auther wjt
 * @date 2019/4/25
 */
public class NanningApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler= CrashHandler.getInstance();
        crashHandler.init(this);
    }

}
