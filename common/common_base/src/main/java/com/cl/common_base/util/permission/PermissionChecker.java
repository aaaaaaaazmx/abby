package com.cl.common_base.util.permission;

import static com.cl.common_base.ext.LogKt.logI;

import android.Manifest;
import android.content.Context;
import android.os.Build;


/**
 * 权限管理
 *
 * @date on 2021/10/20
 */
public class PermissionChecker {

    private static final String TAG = "PermissionChecker";

    /**
     * 判断ble连接权限，只是连接，不包含发现设别权限（Android 12需要BLUETOOTH_SCAN权限）
     *
     * @param context
     * @return
     */
    public static boolean hasBleConnectPermissions(Context context) {
        //Android 12适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPermissions(context, Manifest.permission.BLUETOOTH_CONNECT);
        }
        return true;
    }

    /**
     * 判断权限是否授予
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            int result = androidx.core.content.PermissionChecker.checkSelfPermission(context, permission);
            //始终允许
            if (result == androidx.core.content.PermissionChecker.PERMISSION_GRANTED) {
                logI("hasPermissions " + permission + " Granted");
            }
            //拒绝
            else if (result == androidx.core.content.PermissionChecker.PERMISSION_DENIED) {
                logI("hasPermissions " + permission + " Denied");
                return false;
            }
            //使用中允许，如果定位服务关闭，判断定位权限也会返回这个
            else if (result == androidx.core.content.PermissionChecker.PERMISSION_DENIED_APP_OP) {
                logI("hasPermissions " + permission + " Denied_APP_OP");
                return false;
            } else {
                logI("hasPermissions " + permission + " result=" + result);
            }
        }
        return true;
    }

    /**
     * 定义定位权限
     *
     * @param hasBackLocation
     * @return
     */
    public static String[] getLocationPermission(boolean hasBackLocation) {
        String[] permissions;
        if (hasBackLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        return permissions;
    }

}
