package com.cl.common_base.util.device

import android.os.Handler
import android.os.Looper
import com.cl.common_base.BaseApplication
import com.cl.common_base.R
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.IThingDevice
import com.thingclips.smart.sdk.enums.ThingDevicePublishModeEnum

// 设备控制类
class DeviceControlImpl : DeviceControl, IResultCallback {
    // 用户信息
    val userInfo = {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    private val map by lazy {
        hashMapOf<String, Any>()
    }

    private val handler = Handler(Looper.getMainLooper())

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
    override fun getCurrentDevice(devId: String?): IThingDevice? {
        logI("12312313123L:${Prefs.getString(Constants.Login.KEY_LOGIN_DATA)}")
        return ThingHomeSdk.newDeviceInstance(devId ?: userInfo()?.deviceId)
    }

    /**
     * 排水
     */
    override fun pumpWater(startOrStop: Boolean) {
        map[TuYaDeviceConstants.KAY_PUMP_WATER] = startOrStop
        publishDpsAsync(map)
    }

    override fun airPump(startOrStop: Boolean) {
        map[TuYaDeviceConstants.KEY_DEVICE_AIR_PUMP] = startOrStop
        publishDpsAsync(map)
    }

    override fun fanIntake(gear: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_INTAKE] = gear
        publishDpsAsync(map)
    }

    override fun fanExhaust(gear: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_EXHAUST] = gear
        publishDpsAsync(map)
    }

    override fun lightIntensity(gear: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_GROW_LIGHT] = gear
        publishDpsAsync(map)
    }

    /**
     * 旋钮开门
     */
    override fun doorLock(openOrClose: Boolean) {
        map[TuYaDeviceConstants.KEY_DEVICE_DOOR_LOOK] = openOrClose
        publishDpsAsync(map)
    }

    override fun childLock(startOrStop: Boolean, devId: String?) {
        map[TuYaDeviceConstants.KEY_DEVICE_CHILD_LOCK] = startOrStop
        publishDpsAsync(map, devId)
    }

    /**
     * 夜间模式
     */
    override fun nightMode(startOrStop: String, devId: String?) {
        map[TuYaDeviceConstants.KEY_DEVICE_NIGHT_MODE] = startOrStop
        publishDpsAsync(map, devId)
    }

    override fun lightTime(time: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_LIGHT_TIME] = time
        publishDpsAsync(map)
    }

    override fun closeLightTime(time: Int) {
        map[TuYaDeviceConstants.KEY_DEVICE_LIGHT_OFF_TIME] = time
        publishDpsAsync(map)
    }

    /**
     * 添加肥料、喂食
     */
    override fun feedAbby(startOrStop: Boolean): DeviceControlImpl {
        map[TuYaDeviceConstants.KAY_FEED_ABBY] = startOrStop
        publishDpsAsync(map)
        return this@DeviceControlImpl
    }

    /**
     * 修复SN上报
     */
    override fun repairSN(isRepair: String): DeviceControlImpl {
        map[TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN] = isRepair
        publishDpsAsync(map)
        return this@DeviceControlImpl
    }

    override fun sendDps(dpsJson: String, devId: String?): DeviceControlImpl {
        map[TuYaDeviceConstants.KEY_DEVICE_MULTIPLE_DP] = dpsJson
        // logI("sendDPs: $dpsJson,,,\n --> ${GSON.toJson(map)}")
        Thread {
            publishDpsSync(map, devId)
        }.start()
        return this@DeviceControlImpl
    }

    private fun publishDpsAsync(dataMap: HashMap<String, Any>, devId: String? = null) {
        Thread {
            publishDpsSync(dataMap, devId)
        }.start()
    }

    private fun publishDpsSync(dataMap: HashMap<String, Any>, devId: String? = null) {
        try {
            val device = getCurrentDevice(devId)
            if (device != null) {
                GSON.toJsonInBackground(dataMap) { info ->
                    device.publishDps(
                        info,
                        ThingDevicePublishModeEnum.ThingDevicePublishModeAuto,
                        object : IResultCallback {
                            override fun onSuccess() {
                                handler.post { this@DeviceControlImpl.onSuccess() }
                            }

                            override fun onError(code: String?, error: String?) {
                                handler.post { this@DeviceControlImpl.onError(code, error) }
                            }
                        }
                    )
                }

            } else {
                handler.post { onError("500", "Device not found") }
            }
        } catch (e: Exception) {
            handler.post { onError("500", e.localizedMessage) }
        }
    }


    // 统一判断code码
    // Android:
    // 11001, connection error, try to delete device and pair again
    // 10203, The device is offline, please pair again
    // 1508, The device has been removed, please pair again.
    // 50408, Connection timeout, please check your network connection.
    // 500, Server error, please contact support@heyabby.com
    private val errorCodeMessages = mapOf(
        "11001" to "Connection error, try to delete device and pair again",
        "10203" to "The device is offline, please pair again",
        "1508" to "The device has been removed, please pair again.",
        "50408" to "Connection timeout, please check your network connection.",
        "500" to "Server error, please contact support@heyabby.com"
    )

    override fun onError(code: String?, error: String?) {
        val message = errorCodeMessages[code] ?: error
        onErrorAction?.invoke(this@DeviceControlImpl, code, message)
        kotlin.runCatching {
            Reporter.reportTuYaError(
                map.keys.firstOrNull() ?: "DeviceControlImpl", map.values.firstOrNull().toString(), code
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
