package com.cl.common_base.util.device

import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.IThingDevice
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.enums.ThingDevicePublishModeEnum
import kotlin.math.log

/**
 * 设备控制中心
 *
 * @author 李志军 2022-08-09 11:16
 */
class DeviceControlImpl : DeviceControl, IResultCallback {
    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    private val map by lazy {
        hashMapOf<String, Any>()
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
    override fun getCurrentDevice(): IThingDevice? {
        logI("12312313123L:${Prefs.getString(Constants.Login.KEY_LOGIN_DATA)}")
        return ThingHomeSdk.newDeviceInstance(userInfo?.deviceId)
    }

    /**
     * 排水
     */
    override fun pumpWater(startOrStop: Boolean) {
        map[TuYaDeviceConstants.KAY_PUMP_WATER] = startOrStop
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    override fun airPump(startOrStop: Boolean) {
        map[TuYaDeviceConstants.KEY_DEVICE_AIR_PUMP] = startOrStop
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    override fun fanIntake(gear: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_INTAKE] = gear
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    override fun fanExhaust(gear: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_EXHAUST] = gear
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    override fun lightIntensity(gear: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_GROW_LIGHT] = gear
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    /**
     * 旋钮开门
     */
    override fun doorLock(openOrClose: Boolean) {
        map[TuYaDeviceConstants.KEY_DEVICE_DOOR_LOOK] = openOrClose
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    override fun childLock(startOrStop: Boolean, devId: String?) {
        map[TuYaDeviceConstants.KEY_DEVICE_CHILD_LOCK] = startOrStop
        if (!devId.isNullOrEmpty()) {
            ThingHomeSdk.newDeviceInstance(devId)?.publishDps(
                GSON.toJson(map),
                ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
                this
            )
        }else {
            getCurrentDevice()?.publishDps(
                GSON.toJson(map),
                ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
                this
            )
        }
    }

    /**
     * 夜间模式
     */
    override fun nightMode(startOrStop: String, devId: String?) {
        map[TuYaDeviceConstants.KEY_DEVICE_NIGHT_MODE] = startOrStop
        if (!devId.isNullOrEmpty()) {
            ThingHomeSdk.newDeviceInstance(devId)?.publishDps(
                GSON.toJson(map),
                ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
                this
            )
        } else {
            getCurrentDevice()?.publishDps(
                GSON.toJson(map),
                ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
                this
            )
        }

    }

    override fun lightTime(time: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_LIGHT_TIME] = time
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    override fun closeLightTime(time: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_LIGHT_OFF_TIME] = time
        getCurrentDevice()?.publishDps(
            GSON.toJson(map),
            ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
            this
        )
    }

    /**
     * 添加肥料、喂食
     */
    override fun feedAbby(startOrStop: Boolean): DeviceControlImpl {
        map[TuYaDeviceConstants.KAY_FEED_ABBY] = startOrStop
        getCurrentDevice()?.publishDps(GSON.toJson(map), this)
        return this@DeviceControlImpl
    }

    /**
     * 修复SN上报
     */
    override fun repairSN(isRepair: String): DeviceControlImpl {
        map[TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN] = isRepair
        getCurrentDevice()?.publishDps(GSON.toJson(map), this)
        return this@DeviceControlImpl
    }

    override fun sendDps(dpsJson: String): DeviceControlImpl {
        map[TuYaDeviceConstants.KEY_DEVICE_MULTIPLE_DP] = dpsJson
        logI("sendDPs: $dpsJson,,,\n --> ${GSON.toJson(map)}")
        getCurrentDevice()?.publishDps(GSON.toJson(map), this)
        return this@DeviceControlImpl
    }

    override fun onError(code: String?, error: String?) {
        onErrorAction?.invoke(this@DeviceControlImpl, code, error)
        kotlin.runCatching {
            Reporter.reportTuYaError(
                map.keys.firstOrNull() ?: "DeviceControlImpl",
                map.values.firstOrNull().toString(),
                code
            )
            map.clear()
        }
    }

    override fun onSuccess() {
        kotlin.runCatching {
            onSuccessAction?.invoke(this@DeviceControlImpl)
            map.clear()
        }
    }
}