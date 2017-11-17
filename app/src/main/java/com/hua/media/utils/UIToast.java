package com.hua.media.utils;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

//Toast工具类
public class UIToast {
    public static final int NONE = 0;

    /**
     * 使用系统Toast 显示时间为Toast.LENGTH_SHOW
     * @param context
     * @param text
     */
    public static void showToast(Context context, CharSequence text) {
        showToast(context, text, false);
    }


    public static void showToast(Context context, CharSequence text, boolean longTime) {
        showBaseToast(context, text, longTime, NONE, NONE, NONE);
    }

    public static void showBaseToast(Context context,
                                     CharSequence text,
                                     int animationStyleId){
        showBaseToast(context, text, false, NONE, NONE, animationStyleId);
    }

    public static void showBaseToast(Context context,
                                     CharSequence text,
                                     boolean longTime,
                                     int animationStyleId){
        showBaseToast(context, text, longTime, NONE, NONE, animationStyleId);
    }

    /**
     *
     * @param context  上下文
     * @param text  展示文字
     * @param longTime  是否是Toast.LENGTH_LONG
     * @param toastTextColor  Toast字体颜色
     * @param toastBackgroundColor   Toast背景色
     * @param animationStyleId  是否带动画
     */
    public static void showBaseToast(Context context, CharSequence text,
                                     boolean longTime,
                                     int toastTextColor,
                                     int toastBackgroundColor,
                                     int animationStyleId){
        int duration = longTime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context,text,duration);
        View toastView = toast.getView();

        // toastTextColor
        if (toastTextColor != 0) {
            ((TextView) toastView.findViewById(android.R.id.message)).setTextColor(toastTextColor);
        }

        // toastBackgroundColor
        if (toastBackgroundColor != 0) {
            Drawable toastBackgroundDrawable = tintDrawable(toastView
                    .getBackground().mutate(), ColorStateList.valueOf(toastBackgroundColor));
            toastView.setBackgroundDrawable(toastBackgroundDrawable);
        }

        // animation
        if (animationStyleId != 0) {
            try {
                Object mTN;
                mTN = getField(toast, "mTN");
                if (mTN != null) {
                    Object mParams = getField(mTN, "mParams");
                    if (mParams != null && mParams instanceof WindowManager.LayoutParams) {
                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
                        params.windowAnimations = animationStyleId;
                    }
                }
            } catch (Exception e) {
                Log.d("UIToast", "Toast windowAnimations setting failed");
            }
        }

        // show
        toast.setView(toastView);
        toast.show();

    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    /**
     * 反射字段
     * @param object 要反射的对象
     * @param fieldName 要反射的字段名称
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @return obj
     */
    private static Object getField(Object object, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(object);
        }
        return null;
    }
}
