package com.cl.common_base.util.device

import android.util.Log
import com.alibaba.fastjson.JSONObject
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

    fun publishDps(devId: String, dpId: String, value: Any) {
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

    fun listenDPUpdate(devId: String, dpId: String, callback: DPCallback?) {
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
            override fun onStatusChanged(devId: String, online: Boolean) {}
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