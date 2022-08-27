package com.cl.common_base.help

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.pop.GuideBlePop
import com.cl.common_base.pop.PairLocationPop
import com.cl.common_base.util.ble.BleUtil
import com.cl.common_base.util.lcoation.LocationUtil
import com.cl.common_base.util.permission.PermissionChecker
import com.lxj.xpopup.XPopup
import com.permissionx.guolindev.PermissionX

class PermissionHelp {

    /**
     * 检查蓝牙连接所需权限
     *
     * @param context
     * @param fragmentManager
     * @param discoveryDevice 是否需要发现附近蓝牙设备
     * @param listener
     */
    fun checkConnect(
        activity: FragmentActivity,
        fragmentManager: FragmentManager?,
        discoveryDevice: Boolean,
        listener: OnCheckResultListener?
    ) {
        if (!BleUtil.isBleEnabled()) {
            // 蓝牙开关未开启,那么直接弹窗
            XPopup.Builder(activity)
                .isDestroyOnDismiss(false)
                .asCustom(GuideBlePop(activity))
            return
        }

        //Android 12适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (PermissionChecker.hasPermissions(activity, Manifest.permission.BLUETOOTH_SCAN)
                && PermissionChecker.hasPermissions(activity, Manifest.permission.BLUETOOTH_CONNECT)
            ) {
                logI(
                    "checkConnect BLUETOOTH_SCAN BLUETOOTH_CONNECT grant"
                )
                listener?.onResult(true)
                return
            }
            logI(
                "checkConnect BLUETOOTH_SCAN BLUETOOTH_CONNECT request"
            )
            PermissionX.init(activity)
                .permissions(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                .onForwardToSettings { scope, deniedList ->
                    // 用户点击不再询问时,回调
                    // 或者点击肯定时,也会回调此方法
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "Bluetooth connection requires you to enable Bluetooth scanning and connection privileges",
                        "OK",
                        "Cancel"
                    )
                }
                .explainReasonBeforeRequest()
                .onExplainRequestReason { scope, deniedList ->
                    // 用户单次拒绝权限时,回调
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Bluetooth connection requires you to enable Bluetooth scanning and connection privileges",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        logI("These permissions are Granted: $deniedList")
                        // todo 相关事项
                        // android 12 只需要用来发现蓝牙,扫描附近的设备
                        // 并不需要用到定位相关的

                        // 定位是否可用,是否打开定位开关
//                        if (LocationUtil.isLocationEnabled(activity)) {
//                            // 判断蓝牙是否打开
//                            if (BleUtil.isBleEnabled()) {
//                                // 调用涂鸦开始扫描
//                                startScan()
//                            } else {
//                                // 没有打开蓝牙
//                                // 底部弹窗开启蓝牙
//                                guideBlePop.show()
//                            }
//                        } else {
//                            // todo 开启定位开关
//                            // Open location service
//                            locationPop.show()
//                        }
                        // 权限同意
                        listener?.onResult(true)
                    } else {
                        // 权限拒绝
                        listener?.onResult(false)
//
//                        // todo 拒绝的提示
//                        logE("These permissions are denied: $deniedList")
//                        // todo 弹出提示框, 然后跳转到设置界面
//                        var intent = Intent()
//                        try {
//                            logI(
//
//                                "checkConnect to ACTION_APPLICATION_DETAILS_SETTINGS"
//                            )
//                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                            intent.data =
//                                Uri.fromParts("package", activity.packageName, null)
//                            activity.startActivity(intent)
//                        } catch (e: Exception) { //抛出异常就直接打开设置页面
//                            logE("checkConnect to ACTION_SETTINGS")
//                            intent = Intent(Settings.ACTION_SETTINGS)
//                            activity.startActivity(intent)
//                        }

                    }
                }

        } else {
            // todo 打开定位开关
            if (!LocationUtil.isLocationEnabled(activity)) {
                XPopup.Builder(activity)
                    .isDestroyOnDismiss(false)
                    .enableDrag(false)
                    .maxHeight(dp2px(600f))
                    .dismissOnTouchOutside(true)
                    .asCustom(
                        PairLocationPop(activity) {
                            val intent =
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            activity.startActivity(intent)
                        }
                    )
                return
            }

            //如果不需要发现附近蓝牙设备，则不需要申请定位权限
            if (!discoveryDevice) {
                listener?.onResult(true)
                return
            }
            if (PermissionChecker.hasPermissions(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            ) {
                logI(
                    "checkConnect location grant"
                )
                listener?.onResult(true)
                return
            }
            logI(
                "checkConnect location request"
            )
            PermissionX.init(activity)
                .permissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .onForwardToSettings { scope, deniedList ->
                    // 用户点击不再询问时,回调
                    // 或者点击肯定时,也会回调此方法
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "Bluetooth connection requires you to enable location access",
                        "OK",
                        "Cancel"
                    )
                }
                .explainReasonBeforeRequest()
                .onExplainRequestReason { scope, deniedList ->
                    // 用户单次拒绝权限时,回调
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Bluetooth connection requires you to enable location access",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        logI("These permissions are Granted: $deniedList")

                        listener?.onResult(true)
                        // 这个需要用到定位权限

//                        // 定位是否可用,是否打开定位开关
//                        if (LocationUtil.isLocationEnabled(activity)) {
//                            // 判断蓝牙是否打开
//                            if (BleUtil.isBleEnabled()) {
//                                // 调用涂鸦开始扫描
//                                startScan()
//                            } else {
//                                // 没有打开蓝牙
//                                // 底部弹窗开启蓝牙
//                                guideBlePop.show()
//                            }
//                        } else {
//                            // todo 开启定位开关
//                            // Open location service
//                            locationPop.show()
//                        }
                    } else {
                        // todo 拒绝的提示
                        logE("These permissions are denied: $deniedList")
                        // todo 弹出提示框, 然后跳转到设置界面
                        var intent = Intent()
                        try {
                            logI(
                                "checkConnect to ACTION_APPLICATION_DETAILS_SETTINGS"
                            )
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            intent.data =
                                Uri.fromParts("package", activity.packageName, null)
                            activity.startActivity(intent)
                        } catch (e: Exception) { //抛出异常就直接打开设置页面
                            logE("checkConnect to ACTION_SETTINGS")
                            intent = Intent(Settings.ACTION_SETTINGS)
                            activity.startActivity(intent)
                        }
                    }
                }
        }
    }


    interface OnCheckResultListener {
        /**
         * 权限检查结果
         *
         * @param result true 有权限 false 无权限
         */
        fun onResult(result: Boolean)
    }

}