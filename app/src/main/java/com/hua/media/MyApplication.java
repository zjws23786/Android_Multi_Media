package com.hua.media;

import android.app.Application;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.hua.media.common.Env;
import com.hua.media.utils.HardwareInfoUtils;

import java.lang.reflect.Field;

/**
 * Created by hjz on 2017/11/23 0023.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);
        HardwareInfoUtils.initDisplayMetrics();
    }


    public static void setInstance(MyApplication instance) {
        MyApplication.instance = instance;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
