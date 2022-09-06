package com.cl.common_base.help

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
                .show()
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
                        "Bluetooth connection requires you to enable both Bluetooth Scan and Bluetooth Connect permissions",
                        "To set",
                        "Cancel"
                    )
                }
                .explainReasonBeforeRequest()
                .onExplainRequestReason { scope, deniedList ->
                    // 用户单次拒绝权限时,回调
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Bluetooth connection requires you to enable both Bluetooth Scan and Bluetooth Connect permissions",
                        "Allow",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        logI("These permissions are Granted: $deniedList")
                        // todo 相关事项
                        listener?.onResult(true)
                    } else {
                        // 权限拒绝
                        listener?.onResult(false)
                    }
                }

        } else {
            // todo 打开定位开关
            if (!LocationUtil.isLocationEnabled(activity)) {
                XPopup.Builder(activity)
                    .isDestroyOnDismiss(false)
                    .enableDrag(false)
                    .dismissOnTouchOutside(true)
                    .asCustom(
                        PairLocationPop(activity) {
                            val intent =
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            activity.startActivity(intent)
                        }
                    ).show()
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
                        "Granting Hey abby access to your phone's location will be used for device automation, Wi-Fi listings, and locating nearby devices—even when the app is closed or not in use.",
                        "To set",
                        "Cancel"
                    )
                }
                .explainReasonBeforeRequest()
                .onExplainRequestReason { scope, deniedList ->
                    // 用户单次拒绝权限时,回调
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Granting Hey abby access to your phone's location will be used for device automation, Wi-Fi listings, and locating nearby devices—even when the app is closed or not in use.",
                        "Allow",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        logI("These permissions are Granted: $deniedList")

                        listener?.onResult(true)
                        // 这个需要用到定位权限
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


    fun checkConnectForTuYaBle(activity: FragmentActivity, listener: OnCheckResultListener?) {
        if (!BleUtil.isBleEnabled()) {
            // 蓝牙开关未开启,那么直接弹窗
            XPopup.Builder(activity)
                .isDestroyOnDismiss(false)
                .asCustom(GuideBlePop(activity))
                .show()
            return
        }

        // todo 打开定位开关
        if (!LocationUtil.isLocationEnabled(activity)) {
            XPopup.Builder(activity)
                .isDestroyOnDismiss(false)
                .enableDrag(false)
                .dismissOnTouchOutside(true)
                .asCustom(
                    PairLocationPop(activity) {
                        val intent =
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        activity.startActivity(intent)
                    }
                ).show()
            return
        }

        /**
         * 先检查定位是否拥有权限
         */
        if (PermissionChecker.hasPermissions(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        ) {
            logI(
                "checkConnect ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION grant"
            )
            // 如果拥有定位权限，那么直接检查蓝牙扫描权限，
            applyBleScanPer(activity, listener)
        } else {
            // todo 申请定位权限
            applyBleLocation(activity, listener)
        }
    }


    /**
     * 适配android12蓝牙权限
     */
    private fun applyBleScanPer(
        activity: FragmentActivity,
        listener: OnCheckResultListener?
    ): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (PermissionChecker.hasPermissions(activity, Manifest.permission.BLUETOOTH_SCAN)
                && PermissionChecker.hasPermissions(
                    activity, Manifest.permission.BLUETOOTH_CONNECT
                )
            ) {
                logI(
                    "checkConnect BLUETOOTH_SCAN BLUETOOTH_CONNECT grant"
                )
                listener?.onResult(true)
            } else {
                // 申请蓝牙连接权限
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
                            "Bluetooth connection requires you to enable both Bluetooth Scan and Bluetooth Connect permissions",
                            "To set",
                            "Cancel"
                        )
                    }
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason { scope, deniedList ->
                        // 用户单次拒绝权限时,回调
                        scope.showRequestReasonDialog(
                            deniedList,
                            "Bluetooth connection requires you to enable both Bluetooth Scan and Bluetooth Connect permissions",
                            "Allow",
                            "Cancel"
                        )
                    }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            logI("These permissions are Granted: $deniedList")
                            // 权限同意
                            listener?.onResult(true)
                        } else {
                            // 权限拒绝
                            listener?.onResult(false)
                        }
                    }
            }
        } else {
            listener?.onResult(true)
        }
        return result
    }


    /**
     * 申请并检查定位权限
     */
    private fun applyBleLocation(
        activity: FragmentActivity,
        listener: OnCheckResultListener?
    ): Boolean {
        var result = false
        // todo 打开定位开关
        if (!LocationUtil.isLocationEnabled(activity)) {
            XPopup.Builder(activity)
                .isDestroyOnDismiss(false)
                .enableDrag(false)
                .dismissOnTouchOutside(true)
                .asCustom(
                    PairLocationPop(activity) {
                        val intent =
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        activity.startActivity(intent)
                    }
                ).show()
            listener?.onResult(false)
        } else {
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
            } else {
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
                            "Granting Hey abby access to your phone's location will be used for device automation, Wi-Fi listings, and locating nearby devices—even when the app is closed or not in use.",
                            "To set",
                            "Cancel"
                        )
                    }
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason { scope, deniedList ->
                        // 用户单次拒绝权限时,回调
                        scope.showRequestReasonDialog(
                            deniedList,
                            "Granting Hey abby access to your phone's location will be used for device automation, Wi-Fi listings, and locating nearby devices—even when the app is closed or not in use.",
                            "Allow",
                            "Cancel"
                        )
                    }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            logI("These permissions are Granted: $deniedList")
                            listener?.onResult(true)
                        } else {
                            listener?.onResult(false)
                        }
                    }
            }
        }

        return result
    }

    interface OnCheckResultListener {
        /**
         * 权限检查结果
         *
         * @param result true 有权限 false 无权限
         */
        fun onResult(result: Boolean)
    }


    /**
     * 统一权限调用
     */
    fun applyPermissionHelp(
        activity: FragmentActivity,
        message: String,
        listener: OnCheckResultListener,
        vararg permissions: String
    ) {
        if (PermissionChecker.hasPermissions(activity, *permissions)) {
            listener.onResult(true)
        } else {
            // todo 申请权限
            PermissionX.init(activity)
                .permissions(
                    *permissions
                )
                .onForwardToSettings { scope, deniedList ->
                    // 用户点击不再询问时,回调
                    // 或者点击肯定时,也会回调此方法
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        message,
                        "To set",
                        "Cancel"
                    )
                }
                .explainReasonBeforeRequest()
                .onExplainRequestReason { scope, deniedList ->
                    // 用户单次拒绝权限时,回调
                    scope.showRequestReasonDialog(
                        deniedList,
                        message,
                        "Allow",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        logI("These permissions are Granted: $deniedList")
                        listener.onResult(true)
                    } else {
                        listener.onResult(false)
                    }
                }
        }
    }

}