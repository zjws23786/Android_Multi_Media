package com.hua.media.utils;

import android.content.Context;
import android.net.TrafficStats;

/**
 * @author hjz
 * @date 2017/12/1 0001
 * 网速工具
 */
public class NetSpeedUtils {
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    /**
     * 得到网络速度
     * 每隔两秒调用一次
     * @param context
     * @return
     */
    public String getNetSpeed(Context context) {
        String netSpeed = "0 kb/s";
        //转为KB
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid)==TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes()/1024);
        long nowTimeStamp = System.currentTimeMillis();
        //毫秒转换
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        netSpeed  = String.valueOf(speed) + " kb/s";
        return  netSpeed;
    }
}
