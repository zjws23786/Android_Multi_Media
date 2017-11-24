package com.hua.media;

import android.app.Application;

/**
 * Created by hjz on 2017/11/23 0023.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);
    }

    public static void setInstance(MyApplication instance) {
        MyApplication.instance = instance;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
