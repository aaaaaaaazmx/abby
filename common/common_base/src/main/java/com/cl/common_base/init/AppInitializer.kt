package com.cl.common_base.init

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.startup.Initializer
import com.alibaba.android.arouter.launcher.ARouter
import com.bhm.ble.BleManager
import com.bhm.ble.attribute.BleOptions
import com.bhm.demo.util.JavaAirBagConfig
import com.cl.common_base.util.crash.StabilityOptimize
import com.cl.common_base.BuildConfig
import com.cl.common_base.constants.Constants
import com.cl.common_base.help.BleConnectHandler
import com.cl.common_base.util.ipc.FrescoManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import com.thingclips.smart.home.sdk.ThingHomeSdk


/**
 * 可以手动初始化，因为有些第三方库，需要同意隐私协议之后才可以初始化
 */
class AppInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // 初始化 Multidex
        // MultiDex.install(context);
        MMKV.initialize(context)
        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog()     // 打印日志
            ARouter.openDebug()   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(context.applicationContext as? Application)
        initLogConfig()

        // 初始化Fresco
        FrescoManager.initFresco(context.applicationContext)

        // 涂鸦，需要同意隐私协议
        ThingHomeSdk.init(
            context.applicationContext as? Application,
            "wxwdkkqchswudekgsrqv",
            "t77armdv4h7ncx97akumfkht7jtpm4xh"
        )
        ThingHomeSdk.setDebugMode(BuildConfig.DEBUG)

        // bugly 初始化符合合规要求
        val strategy = UserStrategy(context.applicationContext as? Application)
        CrashReport.setIsDevelopmentDevice(context, BuildConfig.DEBUG) // 开发测试阶段设备为调试设备
        CrashReport.initCrashReport(
            context.applicationContext as? Application, "2d55fff670",
            false,
            strategy
        )

        // 蓝牙SDK初始化
        (context.applicationContext as? Application)?.let {
            BleManager.get().init(
                it,
                BleOptions.Builder()
                    .setBleConnectCallback(BleConnectHandler.connectCallBack)
                    .setScanMillisTimeOut(5000)
                    .setConnectMillisTimeOut(5000)
                    .setScanDeviceName(Constants.Ble.KEY_PH_DEVICE_NAME)
                    //一般不推荐autoSetMtu，因为如果设置的等待时间会影响其他操作
                    .setMtu(100, true)
                    .setAutoConnect(false) // 不自动重连
                    .setMaxConnectNum(Constants.Ble.KEY_BLE_MAX_CONNECT)
                    // .setConnectRetryCountAndInterval(2, 1000) // 掉线不重连
                    .build()
            )
        }
        return Unit
    }

    /**
     * 初始化日志
     */
    private fun initLogConfig() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
            .methodCount(1)         // (Optional) How many method line to show. Default 2
            .methodOffset(2)        // (Optional) Hides internal method calls up to offset. Default 5
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