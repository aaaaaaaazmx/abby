package com.cl.common_base.util.device

import com.cl.common_base.constants.Constants
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.api.ITuyaDevice
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.smart.sdk.enums.TYDevicePublishModeEnum

/**
 * 设备控制中心
 *
 * @author 李志军 2022-08-09 11:16
 */
class DeviceControlImpl : DeviceControl, IResultCallback {
    // 获取当前设备信息
    private val tuyaHomeBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    // 成功和失败回调
    private var onSuccessAction: (DeviceControlImpl.() -> Unit)? = null
    private var onErrorAction: (DeviceControlImpl.(code: String?, error: String?) -> Unit)? = null

    override fun success(action: DeviceControlImpl.() -> Unit): DeviceControlImpl {
        onSuccessAction = action
        return this@DeviceControlImpl
    }

    override fun error(action: DeviceControlImpl.(code: String?, error: String?) -> Unit): DeviceControlImpl {
        onErrorAction = action
        return this@DeviceControlImpl
    }

    /**
     * 获取当前设备
     */
    override fun getCurrentDevice(): ITuyaDevice? {
        return TuyaHomeSdk.newDeviceInstance(tuyaHomeBean?.devId)
    }

    /**
     * 排水
     */
    override fun pumpWater(startOrStop: Boolean) {
        val map = hashMapOf<String, Any>()
        map[TuYaDeviceConstants.KAY_PUMP_WATER] = startOrStop
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            TYDevicePublishModeEnum.TYDevicePublishModeAuto,
            this
        )
    }

    /**
     * 添加肥料、喂食
     */
    override fun feedAbby(startOrStop: Boolean): DeviceControlImpl {
        val map = hashMapOf<String, Any>()
        map[TuYaDeviceConstants.KAY_FEED_ABBY] = startOrStop
        getCurrentDevice()?.publishDps(GSON.toJson(map), this)
        return this@DeviceControlImpl
    }

    /**
     * 修复SN上报
     */
    override fun repairSN(isRepair: String): DeviceControlImpl {
        val map = hashMapOf<String, Any>()
        map[TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN] = isRepair
        getCurrentDevice()?.publishDps(GSON.toJson(map), this)
        return this@DeviceControlImpl
    }

    override fun onError(code: String?, error: String?) {
        onErrorAction?.invoke(this@DeviceControlImpl, code, error)

    }

    override fun onSuccess() {
        onSuccessAction?.invoke(this@DeviceControlImpl)
    }
}