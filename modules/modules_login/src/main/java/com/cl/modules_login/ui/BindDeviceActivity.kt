package com.cl.modules_login.ui

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.Prefs
import com.cl.modules_login.databinding.HomeBindDevicesBinding
import com.thingclips.smart.android.user.api.ILogoutCallback
import com.thingclips.smart.home.sdk.ThingHomeSdk
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RouterPath.LoginRegister.PAGE_BIND)
@AndroidEntryPoint
class BindDeviceActivity : BaseActivity<HomeBindDevicesBinding>() {
    override fun initView() {
        binding.ftb.setLeftText("Log out").setLeftClickListener {
            // Logout
            ThingHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                override fun onSuccess() {
                    // Clear cache
                    Prefs.clear()

                    // Navigate to User Func Navigation Page
                    val intent = Intent(this@BindDeviceActivity, OffLineLoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

                override fun onError(code: String?, error: String?) {

                }

            })
        }

        // 跳转添加设备界面
        binding.tvScan.setSafeOnClickListener {
            checkPer()
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }

    private fun checkPer() {
        PermissionHelp().checkConnectForTuYaBle(this@BindDeviceActivity,
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    // 如果权限都已经同意了
                    ARouter.getInstance().build(RouterPath.PairConnect.PAGE_PLANT_SCAN)
                        .navigation()
                }
            })
    }
}