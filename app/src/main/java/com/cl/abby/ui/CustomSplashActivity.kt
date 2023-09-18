package com.cl.abby.ui

import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import androidx.annotation.RestrictTo
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.abby.R
import com.cl.abby.databinding.CustomSplashActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AutomaticLoginReq
import com.cl.common_base.bean.TuYaInfo
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomSplashActivity : BaseActivity<CustomSplashActivityBinding>() {
    private val tuYaInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_TU_YA_INFO)
        GSON.parseObject(bean, TuYaInfo::class.java)
    }

    private val borad by lazy {
        BluetoothMonitorReceiver()
    }

    // 账号
    val account by lazy {
        Prefs.getString(Constants.Login.KEY_LOGIN_ACCOUNT)
    }

    // 密码
    val psd by lazy {
        Prefs.getString(Constants.Login.KEY_LOGIN_PSD)
    }


    @Inject
    lateinit var mViewModel: LoginViewModel

    private val images = intArrayOf(
        R.mipmap.plant_one,
        R.mipmap.plant_two,
        R.mipmap.plant_three,
        R.mipmap.plant_four,
        R.mipmap.plant_five,
        R.mipmap.plant_six,
        R.mipmap.plant_seven,
        R.mipmap.plant_eight,
        R.mipmap.plant_nine,
        R.mipmap.plant_ten,
        R.mipmap.plant_eleven,
        R.mipmap.plant_twelve
    )

    // 初始化动画
    private lateinit var animator: ValueAnimator
    override fun initView() {
        InitSdk.init()
        binding.ivAnimation.apply {
            animator = ValueAnimator.ofInt(0, images.size - 1)
            animator.duration = (images.size * 100).toLong()
            animator.repeatCount = ValueAnimator.INFINITE

            animator.addUpdateListener { animation ->
                val index = (animation.animatedValue as Int)
                setImageResource(images[index])
            }
            animator.start()
        }
        restrictTo()
    }

    private fun restrictTo() {
        val data = Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN)
        if (data.isEmpty()) {
            // 直接跳转登录界面
            ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN).navigation()
            finish()
        } else {
            // 主要是针对老用户，因为新增了一个key用于保存涂鸦的信息，老用户是没有的，所以会一直登录不上，如果是老用户，那么就直接跳转到登录页面，让其登录一遍。
            val tuyaCountryCode = tuYaInfo?.tuyaCountryCode
            val tuyaPassword = tuYaInfo?.tuyaPassword
            if (null == tuYaInfo || (tuyaCountryCode?.isEmpty() == true && tuyaPassword?.isEmpty() == true)) {
                // 直接跳转登录界面
                ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN).navigation()
                finish()
            } else {
                mViewModel.refreshToken(
                    AutomaticLoginReq(
                        userName = account,
                        password = psd,
                        token = data
                    )
                )
            }
        }
    }

    override fun observe() {
        mViewModel.refreshToken.observe(this@CustomSplashActivity, resourceObserver {
            error { errorMsg, code ->
                // 从设备列表当中获取当前选中设备
                mViewModel.userDetail()
            }
            success {
                // 保存涂鸦信息
                val tuYaInfo = TuYaInfo(
                    tuyaCountryCode = data?.tuyaCountryCode,
                    tuyaPassword = data?.tuyaPassword,
                    tuyaUserId = data?.tuyaUserId,
                    tuyaUserType = data?.tuyaUserType
                )
                GSON.toJson(tuYaInfo)?.let { tuyainfos ->
                    logI("tuYaInfoL: $tuyainfos")
                    Prefs.putStringAsync(Constants.Login.KEY_TU_YA_INFO, tuyainfos)
                }
                // 从设备列表当中获取当前选中设备
                mViewModel.userDetail()
            }
        })

        mViewModel.userDetail.observe(this@CustomSplashActivity, resourceObserver {
            error { errorMsg, code ->
                if (code == -1) {
                    ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN).navigation()
                    finish()
                }
            }
            success {
                // 获取InterCome信息
                mViewModel.getInterComeData()
            }
        })

        mViewModel.getInterComeData.observe(this@CustomSplashActivity, resourceObserver {
            error { errorMsg, code ->
                val email = mViewModel.userDetail.value?.data?.email
                val tuyaCountryCode = tuYaInfo?.tuyaCountryCode
                val tuyaPassword = tuYaInfo?.tuyaPassword
                mViewModel.tuYaLogin(
                    map = mapOf(),
                    mViewModel.userDetail.value?.data?.externalId,
                    mViewModel.userDetail.value?.data,
                    mViewModel.userDetail.value?.data?.deviceId,
                    tuyaCountryCode,
                    email,
                    AESCipher.aesDecryptString(tuyaPassword, AESCipher.KEY),
                    onRegisterReceiver = { devId ->
                        val intent = Intent(
                            this@CustomSplashActivity,
                            TuYaDeviceUpdateReceiver::class.java
                        )
                        startService(intent)
                    },
                    onError = { code, error ->
                        hideProgressLoading()
                        error?.let { ToastUtil.shortShow(it) }
                    }
                )
            }
            success {
                val email = mViewModel.userDetail.value?.data?.email
                val tuyaCountryCode = tuYaInfo?.tuyaCountryCode
                val tuyaPassword = tuYaInfo?.tuyaPassword
                mViewModel.tuYaLogin(
                    map = mapOf(),
                    mViewModel.userDetail.value?.data?.externalId,
                    mViewModel.userDetail.value?.data,
                    mViewModel.userDetail.value?.data?.deviceId,
                    tuyaCountryCode,
                    email,
                    AESCipher.aesDecryptString(tuyaPassword, AESCipher.KEY),
                    onRegisterReceiver = { devId ->
                        val intent = Intent(
                            this@CustomSplashActivity,
                            TuYaDeviceUpdateReceiver::class.java
                        )
                        startService(intent)
                    },
                    onError = { code, error ->
                        hideProgressLoading()
                        error?.let { ToastUtil.shortShow(it) }
                    }
                )
            }
        })


        /**
         * 检查是否种植过
         */
        mViewModel.checkPlant.observe(this@CustomSplashActivity, resourceObserver {
            loading { }
            error { errorMsg, code ->
                errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                // 如果接口抛出异常，那么直接跳转登录页面。不能卡在这
                if (code == -1) {
                    ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN).navigation()
                }
            }
            success {
                data?.let { PlantCheckHelp().plantStatusCheck(this@CustomSplashActivity, it) }
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

    override fun onDestroy() {
        super.onDestroy()
        animator.cancel()
    }
}