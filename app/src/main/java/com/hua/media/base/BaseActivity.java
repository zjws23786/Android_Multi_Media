package com.hua.media.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.hua.librarytools.utils.UIToast;
import com.hua.media.R;
import com.hua.media.utils.PermissionUtils;
import com.hua.media.utils.ProviderUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/10/9 0009.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    protected String photoImgPath; //拍照图片存储路径

    protected final int SDK_LOCAPERMISSION_REQUEST = 127;
    protected final int SDK_STROPERMISSION_REQUEST = 128;

    /**
     * 请求CAMERA权限码
     */
    public static final int REQUEST_CAMERA_PERM = 101;
    private String permissionInfo;

    //共同控件
    private ImageView arrow_left_iv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    /**
     * 6.0获取Camra权限
     */

    @TargetApi(23)
    protected void getCamearPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 储存权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CAMERA_PERM);
            } else {
            }
        } else {
        }
    }

    /**
     * 6.0获取存储卡的权限
     */
    @TargetApi(23)
    protected void getStoragePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 储存权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            //           读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_STROPERMISSION_REQUEST);
            } else {
            }
        } else {
        }
    }


    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {  // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }


    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
//        grantResults   0是通过，-1是拒绝
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //储存权限回调
        if (requestCode == SDK_STROPERMISSION_REQUEST) {
            if (PermissionUtils.verifyPermissions(grantResults)) {
//                GLog.e(TAG, "存储权限已经获得");
            } else {
                UIToast.showBaseToast(this, "权限不允许，将会影响APP的使用", R.style.AnimationToast);
            }
        }

        //相机权限回调
        if (requestCode == REQUEST_CAMERA_PERM) {
            if (PermissionUtils.verifyPermissions(grantResults)) {
//                GLog.e(TAG, "相机权限已经获得");
            } else {
                UIToast.showBaseToast(this, "权限不允许，将会影响APP的使用", R.style.AnimationToast);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void initView() {
        setLayout();
        arrow_left_iv = (ImageView) findViewById(R.id.arrow_left_iv);
        if (arrow_left_iv != null) {
            arrow_left_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
        findViewById();
        setListener();
        init();
    }


    /***
     * 设置界面布局
     */
    protected abstract void setLayout();

    /***
     * 绑定界面UI
     */
    protected abstract void findViewById();

    /***
     *界面控件监听事件
     */
    protected abstract void setListener();

    /***
     *数据初始化
     */
    protected abstract void init();

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    public String getHandSetInfo() {
        String handSetInfo =
                "手机型号:" + Build.MODEL +
                        ",SDK版本:" + Build.VERSION.SDK +
                        ",系统版本:" + Build.VERSION.RELEASE +
                        ",软件版本:" + getAppVersionName(this);
//        GLog.e(TAG,handSetInfo);
        return handSetInfo;
    }

    //获取当前版本号
    private String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(ProviderUtil.getPackageName(this), 0);
            versionName = packageInfo.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

}
