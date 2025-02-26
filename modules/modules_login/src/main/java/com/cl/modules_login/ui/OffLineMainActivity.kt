package com.cl.modules_login.ui

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.EnvironmentInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.modules_login.databinding.LoginOfflineMainBinding
import com.cl.modules_login.viewmodel.OffLineMainModel
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@Route(path = RouterPath.LoginRegister.PAGE_NEW_MAIN)
@AndroidEntryPoint
class OffLineMainActivity : BaseActivity<LoginOfflineMainBinding>() {

    @Inject
    lateinit var viewModel: OffLineMainModel

    // 涂鸦家庭ID
    private val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    override fun initView() {
        // 获取当前设备
        getCurrentDeviceData()
    }

    override fun observe() {
    }

    override fun initData() {
        binding.ivLightStatus.setSafeOnClickListener {
            // 跳转到灯光设置页面
            startActivity(Intent(this, OffLineHardSetActivity::class.java))
        }
    }

    // 获取当前设备的ID
    // 当前选中的是第几台设备
    // 每次只需要更改这个数字即可。
    private fun getCurrentDeviceData(currentIndex: Int = 0) {
        // 通过涂鸦读取当前选中的台数数据
        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                // 当前设备
                val currentDevice = bean?.deviceList?.get(currentIndex)
                if (currentDevice?.isOnline == false) {
                    // 跳转到设备离线页面
                    startActivity(Intent(this@OffLineMainActivity, OffLineActivity::class.java))
                    finish()
                    return
                }
                // 获取环境信息
                getEnvData(currentDevice)
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
            }
        })
    }

    // 获取当前设备环境信息
    private  fun getEnvData(device: DeviceBean?) {
        device?.let {
            val envReq = EnvironmentInfoReq(deviceId = it.devId)
            it.dps?.forEach { (key, value) ->
                when (key) {
                    TuYaDeviceConstants.KEY_DEVICE_WATER_TEMPERATURE -> {
                        envReq.waterTemperature = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_VENTILATION -> {
                        envReq.ventilation = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_TEMP_CURRENT -> {
                        envReq.tempCurrent = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_INPUT_AIR_FLOW -> {
                        envReq.inputAirFlow = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_HUMIDITY_CURRENT -> {
                        envReq.humidityCurrent = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE -> {
                        envReq.brightValue = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_WATER_LEVEL -> {
                        envReq.waterLevel = value.toString()
                    }
                }
            }
        }
    }
}