package com.cl.common_base

import android.app.Application
import android.content.Context
import android.os.Build
import com.bhm.demo.util.JavaAirBagConfig
import com.cl.common_base.util.crash.StabilityOptimize
import io.intercom.android.sdk.Intercom

open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Intercom.initialize(this, "android_sdk-a07e4cfb1b930b257bd3598e8a8051c3f9090f22", "h6h9xbuv")
        context = this
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // crash兜底
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            if (!BuildConfig.DEBUG) {
                StabilityOptimize.setUpJavaAirBag(mutableListOf<JavaAirBagConfig>().toList())
            }
        }
    }

    companion object {
        private var context: BaseApplication? = null
        @JvmStatic
        fun getContext(): Context {
            return context!!
        }
    }
}