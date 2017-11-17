package com.hua.media.utils;

import android.content.Context;

/**
 * 用于解决provider冲突的util
 * Created by Administrator on 2017/9/1 0001.
 */

public class ProviderUtil {

    /**
     * 得到应用程序的包名
     *
     * @return
     */
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static String getFileProviderName(Context context){
        //注意 .provider要和AndroidManifest.xml节点中provider中android:authorities的包+后缀名保持一致
        return context.getPackageName()+".fileprovider";
    }
}
