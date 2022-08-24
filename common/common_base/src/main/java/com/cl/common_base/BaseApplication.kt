package com.cl.common_base

import android.app.Application
import android.content.Context

open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        private var context: BaseApplication? = null
        fun getContext(): Context {
            return context!!
        }
    }
}