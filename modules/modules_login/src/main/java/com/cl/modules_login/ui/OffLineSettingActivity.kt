package com.cl.modules_login.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.Prefs
import com.cl.common_base.web.WebActivity
import com.cl.modules_login.databinding.LoginOfflineSetBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OffLineSettingActivity: BaseActivity<LoginOfflineSetBinding>() {
    private val devId = {
        Prefs.getString(Constants.Tuya.KEY_DEVICE_ID)
    }

    override fun initView() {
    }

    override fun observe() {
    }

    override fun onResume() {
        super.onResume()
        // 重量单位
        val weightUnit = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        binding.ftWeight.itemValue =
            if (weightUnit) getString(com.cl.common_base.R.string.my_metric) else getString(com.cl.common_base.R.string.my_us)
    }

    override fun initData() {
        binding.featureItemView.setSafeOnClickListener {
            // 跳转到隐私协议H5
            val localLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
            val intent = Intent(this@OffLineSettingActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://www.seedsupreme.com/?a_aid=heyabby")
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
            startActivity(intent)
        }

        binding.ftWeight.setSafeOnClickListener {
            ARouter.getInstance().build(RouterPath.My.PAGE_WEIGHT).navigation()
        }

        binding.test.setSafeOnClickListener {
            Prefs.clear()
            // 跳转到Login页面
            ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation()
        }

        binding.ftPsd.setSafeOnClickListener {
            // 改密码。
            val account = Prefs.getString(Constants.Login.KEY_LOGIN_ACCOUNT)
            val intent = Intent(this, OffLineForgetPasswordActivity::class.java)
            intent.putExtra(ForgetPasswordActivity.KEY_FORGET_NAME, account)
            startActivity(intent)
        }

        binding.ftDevice.setSafeOnClickListener {
            startActivity(Intent(this@OffLineSettingActivity, OffLineDeviceActivity::class.java).apply {
                putExtra("devId", devId())
            })
        }
    }
}