package com.cl.abby.ui

import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.AnticipateInterpolator
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.abby.databinding.ActivitySplashBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.TuYaInfo
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.init.InitSdk
import com.cl.common_base.listener.BluetoothMonitorReceiver
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.BuildConfig
import com.cl.modules_login.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
@Route(path = RouterPath.Welcome.PAGE_SPLASH)
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    var splashScreen: SplashScreen? = null
    override fun initSplash() {
        splashScreen = installSplashScreen()
    }

    override fun initView() {
        // 加载为当初选择的url
        if (BuildConfig.DEBUG) {
            val url = Prefs.getString(Constants.DebugTest.KEY_TEST_URL)
            if (url.isNotEmpty()) {
                ServiceCreators.newBuilder(url)
            }
        }
        // 屏幕打开
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen?.setKeepOnScreenCondition { true }
        } else {
            splashScreen?.setKeepOnScreenCondition { false }
        }
        redirectTo()
    }

    private fun redirectTo() {
        startActivity(Intent(this@SplashActivity, CustomSplashActivity::class.java))
        finish()
    }

    override fun observe() {
    }

    override fun initData() {
    }
}