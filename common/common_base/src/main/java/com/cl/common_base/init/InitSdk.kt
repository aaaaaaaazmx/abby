package com.cl.common_base.init

import androidx.startup.AppInitializer
import com.cl.common_base.BaseApplication
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs

/**
 * 手动初始化SDK
 *
 * @author 李志军 2022-08-22 15:49
 */
class InitSdk {
    companion object {
        fun init() {
            val boolean = Prefs.getBoolean(
                Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE,
                false
            )
            if (!boolean) return
            val initializer = AppInitializer.getInstance(BaseApplication.getContext())
            initializer.initializeComponent(com.cl.common_base.init.PolicyInitializer::class.java)
        }
    }
}