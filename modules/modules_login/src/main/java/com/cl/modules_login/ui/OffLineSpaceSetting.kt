package com.cl.modules_login.ui

import android.content.Intent
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AllDpBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.containsIgnoreCase
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.databinding.LoginSpaceSettingBinding
import com.cl.modules_login.response.OffLineDeviceBean
import com.thingclips.smart.android.device.bean.UpgradeInfoBean
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IGetOtaInfoCallback
import com.thingclips.smart.sdk.api.IResultCallback
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OffLineSpaceSetting : BaseActivity<LoginSpaceSettingBinding>() {
    private val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    private val deviceId by lazy {
        intent.getStringExtra(Constants.Tuya.KEY_DEVICE_ID)
    }

    private val device by lazy {
        intent.getSerializableExtra("bean") as? OffLineDeviceBean
    }

    /**
     * 涂鸦摄像头帮助类
     */
    private val tuyaUtils by lazy {
        TuyaCameraUtils()
    }

    override fun initView() {
        getSn()
    }

    override fun observe() {
    }

    override fun initData() {
        binding.dtDeleteDevice.setSafeOnClickListener {

            // 判断设备是否离线，離線了就直接刪除
            if (device?.isOnline == false) {
                val accessoryList = device?.accessoryList?.firstOrNull { it.accessoryType == "Camera" }
                if (null != accessoryList) {
                    // 移除摄像头
                    tuyaUtils.unBindCamera(accessoryList.cameraId.toString(), onSuccessAction = {
                        queryDevice()
                    }, onErrorAction = {
                        ToastUtil.shortShow(it)
                        queryDevice()
                    })
                    return@setSafeOnClickListener
                }
                queryDevice()
                return@setSafeOnClickListener
            }

            // 发送dp点
            val dpBean = AllDpBean(
                cmd = "6",  gl = "0", `in` = "0", ex = "0", ap = "false", al = "false", gls = "0", gle = "0"
            )
            GSON.toJsonInBackground(dpBean) { it1 ->
                DeviceControl.get().success {
                    logI("dp to success")

                    // 关闭设备。
                    val deviceInstance = ThingHomeSdk.newDeviceInstance(deviceId)
                    deviceInstance.removeDevice(object : IResultCallback {
                        override fun onError(code: String?, error: String?) {
                            ToastUtil.shortShow(error)
                        }

                        override fun onSuccess() {
                            //   如果当前设备绑定了摄像头，那么就需要把摄像头一并解绑。
                            val accessoryList = device?.accessoryList?.firstOrNull { it.accessoryType == "Camera" }
                            if (null != accessoryList) {
                                // 移除摄像头
                                tuyaUtils.unBindCamera(accessoryList.cameraId.toString(), onSuccessAction = {
                                    queryDevice()
                                }, onErrorAction = {
                                    ToastUtil.shortShow(it)
                                    queryDevice()
                                })
                                return
                            }
                            queryDevice()
                        }
                    })
                }.error { code, error -> ToastUtil.shortShow(error) }
                    .sendDps(it1)
            }
        }
        checkFirmwareUpdateInfo { bean, isShow ->
            bean?.firstOrNull { it.type == 9 }?.let { data ->
                binding.ftCurrentFir.itemValue = data.currentVersion
                binding.ftCurrentFir.setShowUpdateRedDot(isShow)
            }
        }

        binding.ftDeviceId.setItemValue(deviceId)
    }


    private fun queryDevice() {
        // 删除。
        // 跳转到绑定机器界面
        Prefs.getObjects()?.indexOfFirst { it.id == deviceId }?.let {
            Prefs.removeObjectForDevice(it)
        }

        // 需要判断当前还剩下多少台机器
        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                if (bean?.deviceList?.size == 0) {
                    Prefs.removeKey(Constants.Global.KEY_GLOBAL_PRO_MODEL)
                    // 跳转到绑定设备界面
                    val intent = Intent(
                        this@OffLineSpaceSetting, BindDeviceActivity::class.java
                    )
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // 超找当前是box的机器，然后选择第一台作为当前设备。
                    val devList = bean?.deviceList?.filter {
                        it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1) || it.productId.containsIgnoreCase(
                            OffLineDeviceBean.DEVICE_VERSION_OG
                        ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG_BLACK) || it.productId.containsIgnoreCase(
                            OffLineDeviceBean.DEVICE_VERSION_OG_PRO
                        ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_PRO) || it.productId.containsIgnoreCase(
                            OffLineDeviceBean.DEVICE_VERSION_O1_SOIL
                        )
                    }

                    if ((devList?.size ?: 0) >= 1) {
                        // 如果当前设备失效了.就选择机器, 反之就直接跳转
                        if (null == devList?.firstOrNull { it.devId == deviceId }) {
                            val currentDevice = bean?.deviceList?.firstOrNull {
                                it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1) || it.productId.containsIgnoreCase(
                                    OffLineDeviceBean.DEVICE_VERSION_OG
                                ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG_BLACK) || it.productId.containsIgnoreCase(
                                    OffLineDeviceBean.DEVICE_VERSION_OG_PRO
                                ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_PRO) || it.productId.containsIgnoreCase(
                                    OffLineDeviceBean.DEVICE_VERSION_O1_SOIL
                                )
                            }
                            Prefs.putStringAsync(
                                Constants.Tuya.KEY_DEVICE_ID,
                                currentDevice?.devId.toString()
                            )
                        }
                        val intent = Intent(this@OffLineSpaceSetting, OffLineMainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // 跳转到添加设备界面
                        // 跳转到绑定设备界面
                        val intent = Intent(this@OffLineSpaceSetting, BindDeviceActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
            }
        })
    }

    override fun onTuYaToAppDataChange(status: String) {
        super.onTuYaToAppDataChange(status)
        GSON.parseObjectInBackground(status, Map::class.java) { map ->
            map?.forEach { (key, value) ->
                when (key) {
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_REST_STATUS_INSTRUCTION -> {
                        if (value.toString().isEmpty()) return@forEach
                        logI(
                            """
                        KEY_DEVICE_REPAIR_REST_STATUS: 
                        value: ${value.toString()}
                    """.trimIndent()
                        )
                        // 修改:mcu:Abby-1.1.01-230313-T-B#abbyAAYA2234130142#1.8.1#flash:Abby-1.1.01-230313-T-B#1.8.1#OG#A0001#B0001#C0001#D0001
                        //mcu:Abby-1.1.01-220519-T-B#abbyAAYA2021730021#1.4.0#flash:Abby-1.1.01-220519-T-B#1.4.0
                        // 截取, 并且需要置灰
                        kotlin.runCatching {
                            binding.ftSN.setItemValueWithColor(
                                value.toString().split("#")[1], "#000000"
                            )
                        }
                    }
                }
            }
        }

    }


    /**
     * 检查固件是否可以升级
     */
    private fun hasHardwareUpdate(list: MutableList<UpgradeInfoBean>?): Boolean {
        if (null == list || list.size == 0) return false
        return list.firstOrNull { it.type == 9 }?.upgradeStatus == 1
    }

    // 获取版本号
    private fun checkFirmwareUpdateInfo(
        onOtaInfo: ((upgradeInfoBeans: MutableList<UpgradeInfoBean>?, isShow: Boolean) -> Unit)? = null,
    ) {
        deviceId?.let {
            ThingHomeSdk.newOTAInstance(it).getOtaInfo(object : IGetOtaInfoCallback {
                override fun onSuccess(upgradeInfoBeans: MutableList<UpgradeInfoBean>?) {
                    logI("getOtaInfo:  ${GSON.toJson(upgradeInfoBeans?.firstOrNull { it.type == 9 })}")
                    // 如果可以升级
                    if (hasHardwareUpdate(upgradeInfoBeans)) {
                        onOtaInfo?.invoke(upgradeInfoBeans, true)
                    } else {
                        // 如果不可以升级过
                        onOtaInfo?.invoke(upgradeInfoBeans, false)
                    }
                }

                override fun onFailure(code: String?, error: String?) {
                    logI(
                        """
                        getOtaInfo:
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    Reporter.reportTuYaError("newOTAInstance", error, code)
                }
            })
        }

    }

    // 获取SN
    private fun getSn() {
        ThingHomeSdk.newDeviceInstance(deviceId).getDp(TuYaDeviceConstants.KEY_DEVICE_REPAIR_REST_STATUS, object : IResultCallback {
            override fun onError(code: String?, error: String?) {
                logI(
                    """
                    KEY_DEVICE_REPAIR_REST_STATUS: error
                    code: $code
                    error: $error
                """.trimIndent()
                )
                ToastUtil.shortShow(error)
                Reporter.reportTuYaError("newDeviceInstance", error, code)
            }

            override fun onSuccess() {
                logI("sdasdas")
            }
        })
    }
}