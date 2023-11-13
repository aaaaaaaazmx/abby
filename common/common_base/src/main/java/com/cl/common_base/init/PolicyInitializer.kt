package com.cl.common_base.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.BuildConfig
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.LogcatLogStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import com.thingclips.smart.home.sdk.ThingHomeSdk
import io.intercom.android.sdk.Intercom


/**
 * 需要同意隐私协议才能初始化的库
 *
 */
class PolicyInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val privacyPolicy = Prefs.getBoolean(
            Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE,
            false
        )
        logI("privacyPolicy: $privacyPolicy")
        val strategy = UserStrategy(context.applicationContext as? Application)
        if (privacyPolicy) {
            // bugly
            strategy.deviceID = AppUtil.getDeviceSerial()
            strategy.deviceModel = AppUtil.deviceModel

            // 极光，需要同意隐私协议
            JPushInterface.setDebugMode(true)
            JPushInterface.init(context.applicationContext as? Application)
        }

        return Unit
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}