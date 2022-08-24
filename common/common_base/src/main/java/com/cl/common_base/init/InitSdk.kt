package com.cl.common_base.init

import androidx.startup.AppInitializer
import com.cl.common_base.BaseApplication
import com.cl.common_base.ext.logI

/**
 * 手动初始化SDK
 *
 * @author 李志军 2022-08-22 15:49
 */
class InitSdk {
    companion object {
        fun init(): Boolean {
            val initializer = AppInitializer.getInstance(BaseApplication.getContext())
            val isInit = initializer.isEagerlyInitialized(com.cl.common_base.init.AppInitializer::class.java)
            logI(":AppInitializer: $isInit")
            return isInit
        }
    }
}