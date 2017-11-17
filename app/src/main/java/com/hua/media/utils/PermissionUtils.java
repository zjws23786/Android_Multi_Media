package com.hua.media.utils;

import android.content.pm.PackageManager;

/**
 * Created by Administrator on 2017/10/9 0009.
 */

public class PermissionUtils {
    private static final String TAG="PermissionUtils";
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
