package com.cl.common_base.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.BuildConfig
import com.cl.common_base.constants.Constants
import com.cl.common_base.easeui.EaseUiHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import com.tuya.smart.home.sdk.TuyaHomeSdk


/**
 * 可以手动初始化，因为有些第三方库，需要同意隐私协议之后才可以初始化
 *
 */
class AppInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        MMKV.initialize(context)
        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog()     // 打印日志
            ARouter.openDebug()   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(context.applicationContext as? Application)
        initLogConfig()

        // 涂鸦，需要同意隐私协议
        TuyaHomeSdk.init(
            context.applicationContext as? Application,
            "awps95tphthfa4rs7drt",
            "tf5vqymy8d337hv97du4crerx5m73qac"
        )
        TuyaHomeSdk.setDebugMode(true)

        // bugly 初始化符合合规要求
        val strategy = UserStrategy(context.applicationContext as? Application)
        CrashReport.setIsDevelopmentDevice(context, BuildConfig.DEBUG) // 开发测试阶段设备为调试设备
        CrashReport.initCrashReport(
            context.applicationContext as? Application, "2d55fff670",
            false,
            strategy
        )
        return Unit
    }

    /**
     * 初始化日志
     */
    private fun initLogConfig() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
            .methodCount(2)         // (Optional) How many method line to show. Default 2
            .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
            .tag(Constants.APP_TAG)   // (Optional) Global tag for every log. Default PRETTY_LOGGER
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}