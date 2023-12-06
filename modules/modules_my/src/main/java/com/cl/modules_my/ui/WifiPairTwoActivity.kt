package com.cl.modules_my.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.network.NetWorkUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyWifiPairTwoActivityBinding
import com.cl.modules_my.viewmodel.BlePairViewModel
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder
import com.thingclips.smart.sdk.api.IThingActivator
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.enums.ActivatorModelEnum
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 通用wifi two 配对界面
 */
@AndroidEntryPoint
class WifiPairTwoActivity : BaseActivity<MyWifiPairTwoActivityBinding>() {

    @Inject
    lateinit var mViewMode: BlePairViewModel

    private val wifiName by lazy {
        intent.getStringExtra("wifiName")
    }

    private val wifiPassWord by lazy {
        intent.getStringExtra("wifiPassWord")
    }


    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }
    private val accessoryId by lazy {
        intent.getStringExtra("accessoryId")
    }

    private val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    override fun initView() {
        binding.tvNextThree.setOnClickListener {
            searchDevice()
        }
        binding.ftbTitle.setLeftClickListener { finish() }
    }

    override fun observe() {
        mViewMode.apply {
            accessoryAdd.observe(this@WifiPairTwoActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.show(errorMsg)
                }
                success {
                    ToastUtil.shortShow("Activate success")
                    val accessId = data?.accessoryId.safeToInt()
                    val accessDeviceId = data?.accessoryDeviceId
                    //  跳转到排插设置界面
                    startActivity(Intent(this@WifiPairTwoActivity, OutletsSettingActivity::class.java).apply {
                        putExtra("accessoryId", accessId)
                        putExtra("accessoryDeviceId", accessDeviceId)
                        putExtra("deviceId", deviceId)
                    })
                    finish()
                }
            })
        }
    }

    override fun initData() {
    }


    lateinit var mToken: String
    private fun searchDevice() {
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId, object : IThingActivatorGetToken {
            override fun onSuccess(token: String?) {
                mToken = token ?: ""
                onClickSetting()
            }

            override fun onFailure(errorCode: String?, errorMsg: String?) {
                ToastUtil.show(errorMsg)
            }
        })
    }

    // 定义一个全局变量来保持对Animator的引用
    var animator: ObjectAnimator? = null
    private fun rotateImageView(imageView: ImageView) {
        animator?.cancel() // 如果已有动画在运行，先取消它
        animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f).apply {
            duration = 2000
            interpolator = LinearInterpolator()
            repeatCount = Animation.INFINITE
        }
        animator?.start()
    }

    private fun destroyAnimator() {
        animator?.cancel() // 取消动画
        animator?.removeAllListeners() // 移除所有的监听器，防止内存泄漏
        animator = null // 帮助垃圾回收器回收这个对象
    }

    private var mTuyaActivator: IThingActivator? = null
    private var currentWifiName: String = ""
    private fun getWifiName() {
        if (NetWorkUtil.isWifi(this@WifiPairTwoActivity)) {
            PermissionHelp().applyPermissionHelp(
                this@WifiPairTwoActivity,
                "Granting Hey abby access to your phone's location will be used to generate a Wi-Fi network list.",
                object : PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        if (!result) return
                        // 直接获取wifi名字
                        val wifiName = NetWorkUtil.getConnectWifiSsid(this@WifiPairTwoActivity)
                        currentWifiName = wifiName

                        if (currentWifiName.startsWith("Smart") || currentWifiName.startsWith("SL")) {
                            goSearch()
                        } else {
                            ToastUtil.shortShow("Please switch to the device's Wi-Fi")
                        }
                    }
                },
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }
    }
    override fun onRestart() {
        super.onRestart()
        // 获取Wi-Fi名字。
        getWifiName()
    }

    private fun goSearch() {
        binding.tvSearch.text = "Connecting.."
        ViewUtils.setGone(binding.llWifiPairTwo)
        ViewUtils.setVisible(binding.llWifiPairThree)
        // showProgressLoading()
        rotateImageView(binding.ivLeida)
        //Show loading progress, disable btnSearch clickable
        val builder = ActivatorBuilder()
            .setSsid(wifiName)
            .setContext(this)
            .setPassword(wifiPassWord)
            .setActivatorModel(ActivatorModelEnum.THING_AP)
            .setTimeOut(100)
            .setToken(mToken)
            .setListener(object : IThingSmartActivatorListener {

                @Override
                override fun onStep(step: String?, data: Any?) {
                    Log.i(TAG, "$step --> $data")
                }

                override fun onActiveSuccess(devResp: DeviceBean?) {
                    hideProgressLoading()

                    Log.i(TAG, "Activate success")

                    // 调用接口添加配件、以及设备ID
                    letMultiple(accessoryId, deviceId) { a, b ->
                        mViewMode.accessoryAdd(a, b, accessoryDeviceId = devResp?.devId)
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onError(
                    errorCode: String?,
                    errorMsg: String?
                ) {
                    hideProgressLoading()
                    ToastUtil.shortShow("Activate error-->$errorMsg")
                    binding.tvSearch.text = "Activate error-->$errorMsg"
                }
            }
            )
        mTuyaActivator =
            ThingHomeSdk.getActivatorInstance().newActivator(builder)
        //Start configuration
        mTuyaActivator?.start()
    }

    /**
     *
     * wifi setting
     */
    private fun onClickSetting() {
        kotlin.runCatching {
            val wifiSettingsIntent = Intent("android.settings.WIFI_SETTINGS")
            startActivity(wifiSettingsIntent)
        }.onFailure {
            ToastUtil.shortShow("Please switch wifi manually")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTuyaActivator?.onDestroy()
        destroyAnimator()
    }
}