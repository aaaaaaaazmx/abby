package com.cl.common_base.help

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON

/**
 * 种植检查帮助类
 * @author 李志军 2022-08-12 12:12
 */
class PlantCheckHelp {
    private val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    /**
     * 检查种植统一跳转
     */
    fun plantStatusCheck(activity: Activity? = null, data: CheckPlantData, isClearTask: Boolean = false, isLeftSwapAnim: Boolean = false, isNoAnim: Boolean = true, context: Context? = null) {
        val intAnim = if (isNoAnim) {
            0
        } else if (isLeftSwapAnim) {
            R.anim.left_fade_in
        } else {
            R.anim.fade_in
        }

        val outAnim = if (isNoAnim) {
            0
        } else if (isLeftSwapAnim) {
            R.anim.left_fade_out
        } else {
            R.anim.fade_out
        }

        // 如果是没绑定过设备的 2
        if (userinfoBean?.deviceStatus == "2") {
            // 跳转未种植引导页面
            // 附带引导flag过去
            ARouter.getInstance()
                    .build(RouterPath.Main.PAGE_MAIN)
                    .withString(
                            Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG,
                            data.plantGuideFlag
                    )
                    .withString(
                            Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE,
                            data.plantExistingStatus
                    )
                    .withString(
                            Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE,
                            userinfoBean?.deviceOnlineStatus
                    )
                    .withBoolean(
                            // 是否是第一次登录注册、并且是从未绑定过设备
                            Constants.Global.KEY_GLOBAL_PLANT_FIRST_LOGIN_AND_NO_DEVICE,
                            userinfoBean?.notBound == 0
                    )
                    .withTransition(intAnim, outAnim)
                    .withFlags(if (isClearTask) Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK else 0)
                    .navigation(activity ?: context)
            return
        }

        // 表示是自动模式 && 不是未种植状态
        if (data.proMode == "On" && data.plantExistingStatus != KEY_NOT_PLANTED) {
            // 跳转未种植引导页面
            // 附带引导flag过去
            ARouter.getInstance()
                .build(RouterPath.Main.PAGE_MAIN)
                .withString(
                    Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG,
                    data.plantGuideFlag
                )
                .withString(
                    Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE,
                    data.plantExistingStatus
                )
                .withString(
                    Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE,
                    userinfoBean?.deviceOnlineStatus
                )
                .withBoolean(
                    // 是否是第一次登录注册、并且是从未绑定过设备
                    Constants.Global.KEY_MANUAL_MODE,
                    data.proMode == "On"
                )
                .withString(
                    Constants.Global.KEY_DEVICE_TYPE,
                    data.deviceType
                )
                .withTransition(intAnim, outAnim)
                .withFlags(if (isClearTask) Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK else 0)
                .navigation(activity ?: context)
            return
        }

        // 是否种植过
        when (data.plantExistingStatus) {
            KEY_NOT_PLANTED -> {
                // 跳转未种植引导页面
                // 附带引导flag过去
                ARouter.getInstance()
                        .build(RouterPath.Main.PAGE_MAIN)
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG,
                                data.plantGuideFlag
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE,
                                data.plantExistingStatus
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE,
                                userinfoBean?.deviceOnlineStatus
                        )
                        .withTransition(intAnim, outAnim)
                        .withFlags(if (isClearTask) Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK else 0)
                        .navigation(activity ?: context)
            }
            KEY_PLANTED -> {
                // 跳转回主页
                // 已种植
                ARouter.getInstance().build(RouterPath.Main.PAGE_MAIN)
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG,
                                data.plantGuideFlag
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE,
                                data.plantExistingStatus
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE,
                                userinfoBean?.deviceOnlineStatus
                        )
                        .withTransition(intAnim, outAnim)
                        .withFlags(if (isClearTask) Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK else 0)
                        .navigation(activity ?: context)
            }
            KEY_PLANTING_RECORDS -> {
                // 跳转未种植引导页面
                // 附带引导flag过去
                ARouter.getInstance()
                        .build(RouterPath.Main.PAGE_MAIN)
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG,
                                data.plantGuideFlag
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE,
                                data.plantExistingStatus
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE,
                                userinfoBean?.deviceOnlineStatus
                        )
                        .withTransition(intAnim, outAnim)
                        .withFlags(if (isClearTask) Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK else 0)
                        .navigation(activity ?: context)
            }
            KEY_PLANTING_COMPLETED -> {
                //  种植完成
                // 跳转回主页
                // 已种植
                ARouter.getInstance().build(RouterPath.Main.PAGE_MAIN)
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG,
                                data.plantGuideFlag
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE,
                                data.plantExistingStatus
                        )
                        .withString(
                                Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE,
                                userinfoBean?.deviceOnlineStatus
                        )
                        .withTransition(intAnim, outAnim)
                        .withFlags(if (isClearTask) Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK else 0)
                        .navigation(activity ?: context)
            }
        }
    }


    companion object {
        // 植物存在状态（0-未种植、1-已种植、2-未种植，且存在旧种植记录、3-种植完成过）
        const val KEY_NOT_PLANTED = "0"
        const val KEY_PLANTED = "1"
        const val KEY_PLANTING_RECORDS = "2"
        const val KEY_PLANTING_COMPLETED = "3"
    }
}