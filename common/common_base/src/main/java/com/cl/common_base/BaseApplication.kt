package com.cl.common_base

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import io.intercom.android.sdk.Intercom

open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Intercom.initialize(this, "android_sdk-a07e4cfb1b930b257bd3598e8a8051c3f9090f22", "h6h9xbuv")
        context = this
    }

    companion object {
        private var context: BaseApplication? = null
        @JvmStatic
        fun getContext(): Context {
            return context!!
        }
    }
}