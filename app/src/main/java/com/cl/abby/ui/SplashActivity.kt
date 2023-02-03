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
import com.cl.abby.BuildConfig
import com.cl.abby.databinding.ActivitySplashBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.init.InitSdk
import com.cl.common_base.listener.BluetoothMonitorReceiver
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.viewmodel.LoginViewModel
import com.cl.modules_my.ui.CalendarActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
@Route(path = RouterPath.Welcome.PAGE_SPLASH)
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private var alphaAnimation: AlphaAnimation? = null

    private val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    private val borad by lazy {
        BluetoothMonitorReceiver()
    }

    @Inject
    lateinit var mViewModel: LoginViewModel

    var splashScreen: SplashScreen? = null
    override fun initSplash() {
        splashScreen = installSplashScreen()
    }

    override fun initView() {
        // 初始化SDK、需要同意隐私协议
        InitSdk.init()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen?.setKeepOnScreenCondition { true }
        } else {
            splashScreen?.setKeepOnScreenCondition { false }
        }
        redirectTo()

        //渐变展示启动屏
//        alphaAnimation = AlphaAnimation(0.3f, 1.0f)
//        alphaAnimation?.duration = 500
//        alphaAnimation?.setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationEnd(arg0: Animation) {
//                redirectTo()
//            }
//
//            override fun onAnimationRepeat(animation: Animation) {
//            }
//
//            override fun onAnimationStart(animation: Animation) {
//            }
//        })
//        binding.ivLogo.startAnimation(alphaAnimation)
    }

    private fun redirectTo() {
        val data = Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN)
        if (data.isNullOrEmpty()) {
            // 直接跳转登录界面
            ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN).navigation()
            finish()
        } else {
            // 登录涂鸦 & 跳转到哪个界面
            val email = userinfoBean?.email
            val tuyaCountryCode = userinfoBean?.tuyaCountryCode
            val tuyaPassword = userinfoBean?.tuyaPassword
            mViewModel.tuYaLogin(
                userinfoBean?.deviceId,
                tuyaCountryCode,
                email,
                AESCipher.aesDecryptString(tuyaPassword, AESCipher.KEY),
                onRegisterReceiver = { devId ->
                    val intent = Intent(this@SplashActivity, TuYaDeviceUpdateReceiver::class.java)
                    startService(intent)
                },
                onError = { code, error ->
                    hideProgressLoading()
                    error?.let { ToastUtil.shortShow(it) }
                }
            )
        }
    }

    override fun observe() {
        /**
         * 检查是否种植过
         */
        mViewModel.checkPlant.observe(this@SplashActivity, resourceObserver {
            loading { }
            error { errorMsg, code ->
                errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                // 如果接口抛出异常，那么直接跳转登录页面。不能卡在这
                if (code == -1) {
                    ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN).navigation()
                }
            }
            success {
                data?.let { PlantCheckHelp().plantStatusCheck(this@SplashActivity, it) }
//                when (userinfoBean?.deviceStatus) {
//                    // 设备状态(1-绑定，2-已解绑)
//                    "1" -> {
//                        // 是否种植过
//                    }
//                    "2" -> {
//                        // 跳转绑定界面
//                        ARouter.getInstance()
//                            .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
//                            .navigation()
//                    }
//                }
                finish()
            }
        })
    }

    override fun initData() {
        // 开启蓝牙广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF")
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON")
        registerReceiver(borad, intentFilter)
    }
}