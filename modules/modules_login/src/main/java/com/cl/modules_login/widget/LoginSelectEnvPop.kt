package com.cl.modules_login.widget

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.BuildConfig
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.widget.toast.ToastUtil
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
                    Prefs.putStringAsync(Constants.DebugTest.KEY_TEST_URL, ServiceCreators.HttpsUrl.PRODUCTION_URL)
                    ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
                tvDevelopment.setOnClickListener {
                    ServiceCreators.newBuilder(ServiceCreators.HttpsUrl.OUTER_ANG_URL)
                    Prefs.clear()
                    dismiss()
                    Prefs.putStringAsync(Constants.DebugTest.KEY_TEST_URL, ServiceCreators.HttpsUrl.OUTER_ANG_URL)
                    ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
                tvTest.setOnClickListener {
                    ServiceCreators.newBuilder(ServiceCreators.HttpsUrl.TEST_URL)
                    Prefs.clear()
                    dismiss()
                    Prefs.putStringAsync(Constants.DebugTest.KEY_TEST_URL, ServiceCreators.HttpsUrl.TEST_URL)
                    ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
                tvBd.setOnClickListener {
                    ServiceCreators.newBuilder(ServiceCreators.HttpsUrl.BD_URL)
                    Prefs.clear()
                    dismiss()
                    Prefs.putStringAsync(Constants.DebugTest.KEY_TEST_URL, ServiceCreators.HttpsUrl.BD_URL)
                    ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
                // 移除任何配件设备，输入配件ID即可
                tvUnbind.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        TuyaCameraUtils().unBindCamera(v.text.toString(), {ToastUtil.shortShow(it)}, {ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_1701))})
                    }
                    false
                }
            }
        }
    }
}