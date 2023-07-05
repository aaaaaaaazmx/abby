package com.cl.common_base.util.device

import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.cl.common_base.ext.logI
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IDevListener
import com.thingclips.smart.sdk.api.IResultCallback

/**
 * 摄像头的下发指令、以及查询指令和指令监听是否成等
 */
class TuyaCameraUtils {
    fun queryValueByDPID(devId: String, dpId: String): Any? {
        ThingHomeSdk.getDataInstance().getDeviceBean(devId)?.also {
            return it.getDps()?.get(dpId)
        }
        return null
    }

    // 查询abby设备的Dp点值
    fun queryAbbyValueByDPID(devId: String, dpId: String): Any? {
        ThingHomeSdk.newDeviceInstance(devId)?.also {
            return it.getDp(dpId, object : IResultCallback {
                override fun onError(code: String?, error: String?) {
                    logI("queryAbbyValueByDPID error $error, dpId $dpId")
                }

                override fun onSuccess() {
                    logI("queryAbbyValueByDPID success, dpId $dpId")
                }
            })
        }
        return null
    }

    fun publishDps(devId: String, dpId: String, value: Any) {
        kotlin.runCatching {
            val jsonObject = JSONObject()
            jsonObject[dpId] = value
            val dps = jsonObject.toString()
            ThingHomeSdk.newDeviceInstance(devId).publishDps(dps, object : IResultCallback {
                override fun onError(code: String, error: String) {
                    Log.e("TuyaCameraUtils", "publishDps err $dps")
                }

                override fun onSuccess() {
                    Log.i("TuyaCameraUtils", "publishDps suc $dps")
                }
            })
        }
    }

    /**
     * 监听dp指令
     * @param devId 设备id
     * @param dpId dp指令id
     * @param callback dp指令回调
     * @param onStatusChangedAction 设备在线状态回调
     */
    fun listenDPUpdate(devId: String, dpId: String, callback: DPCallback? = null, onStatusChangedAction: ((online: Boolean) -> Unit)? = null) {
        ThingHomeSdk.newDeviceInstance(devId).registerDevListener(object : IDevListener {
            override fun onDpUpdate(devId: String, dpStr: String) {
                callback?.let {
                    val dps: Map<String, Any> =
                        JSONObject.parseObject<Map<String, Any>>(dpStr, MutableMap::class.java)
                    if (dps.containsKey(dpId)) {
                        dps[dpId]?.let { it1 -> callback.callback(it1) }
                    }
                }
            }

            override fun onRemoved(devId: String) {}
            override fun onStatusChanged(devId: String, online: Boolean) {
                onStatusChangedAction?.invoke(online)
            }

            override fun onNetworkStatusChanged(devId: String, status: Boolean) {}
            override fun onDevInfoUpdate(devId: String) {}
        })
    }

    fun unBindCamera(cameraId: String, onErrorAction: (errorMsg: String) -> Unit, onSuccessAction: () -> Unit) {
        ThingHomeSdk.newDeviceInstance(cameraId).removeDevice(object : IResultCallback {
            override fun onError(s: String, s1: String) {
                onErrorAction.invoke(s1)
            }

            override fun onSuccess() {
                onSuccessAction.invoke()
            }
        })
    }

    interface DPCallback {
        fun callback(obj: Any)
    }
}