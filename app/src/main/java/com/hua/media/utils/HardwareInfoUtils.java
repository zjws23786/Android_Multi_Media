package com.hua.media.utils;

import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.hua.media.MyApplication;
import com.hua.media.common.Env;
import java.lang.reflect.Field;

/**
 * @author hjz
 * @date 2017/12/4 0004
 * 手机硬件信息
 */

public class HardwareInfoUtils {

    public static void initDisplayMetrics() {
        WindowManager wm = (WindowManager) MyApplication.getInstance().getSystemService("window");
        int rotation = wm.getDefaultDisplay().getRotation();
        DisplayMetrics metrics = MyApplication.getInstance().getResources().getDisplayMetrics();
        Env.screenWidth = rotation == 0 ? metrics.widthPixels:metrics.heightPixels;
        Env.screenHeight = rotation == 0 ? metrics.heightPixels:metrics.heightPixels;
        Env.density = metrics.density;
        Env.statusBarHeight = getStatusBarHeight();
    }

    /***
     * 状态栏高度（像素）
     * @return
     */
    public static int getStatusBarHeight() {
        Class c = null;
        Object obj = null;
        Field field = null;
        boolean x = false;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            int x1 = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = MyApplication.getInstance().getResources().getDimensionPixelSize(x1);
        } catch (Exception var7) {
            var7.printStackTrace();
        }
        return statusBarHeight;
    }
}
