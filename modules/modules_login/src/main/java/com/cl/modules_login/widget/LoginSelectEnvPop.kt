package com.cl.modules_login.widget

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.BuildConfig
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.util.Prefs
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginSelectEnvBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 选择环境弹窗
 *
 * @author 李志军 2022-08-13 21:14
 */
class LoginSelectEnvPop(context: Context) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.login_select_env
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<LoginSelectEnvBinding>(popupImplView)?.apply {
            if (BuildConfig.DEBUG) {
                tvSc.setOnClickListener {
                    ServiceCreators.newBuilder(ServiceCreators.HttpsUrl.PRODUCTION_URL)
                    Prefs.clear()
                    dismiss()
                    ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
                tvDevelopment.setOnClickListener {
                    ServiceCreators.newBuilder(ServiceCreators.HttpsUrl.OUTER_ANG_URL)
                    Prefs.clear()
                    dismiss()
                    ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
                tvTest.setOnClickListener {
                    ServiceCreators.newBuilder(ServiceCreators.HttpsUrl.TEST_URL)
                    Prefs.clear()
                    dismiss()
                    ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
            }
        }
    }
}