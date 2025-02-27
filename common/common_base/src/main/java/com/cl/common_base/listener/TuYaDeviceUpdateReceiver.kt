package com.cl.common_base.listener

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IDeviceListener
import com.thingclips.smart.sdk.bean.DeviceBean

/**
 * 涂鸦设备给APP发的信息监听器
 * 涂鸦服务
 *
 * @author 李志军 2022-08-10 18:12
 */
class TuYaDeviceUpdateReceiver : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 用户信息
        val userInfo by lazy {
            val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
            GSON.parseObject(homeData, DeviceBean::class.java)
        }
        val devId = userInfo?.devId
        logI("TuYaDeviceUpdateReceiver devId : $devId")
        ThingHomeSdk.newDeviceInstance(devId).registerDeviceListener(object : IDeviceListener {
            /**
             * DP 数据更新
             *
             * @param devId 设备 ID
             * @param dpStr 设备发生变动的功能点，为 JSON 字符串，数据格式：{"101": true}
             */
            override fun onDpUpdate(devId: String?, dpStr: MutableMap<String, Any>?) {


                GSON.toJsonInBackground(dpStr) { json->
                    logI(
                        """
                    tuYaOnDpUpdate: 
                    json: $json
                """.trimIndent()
                    )
                    // 直接下发状态
                    LiveEventBus.get()
                        .with(Constants.Tuya.KEY_THING_DEVICE_TO_APP, String::class.java)
                        .postEvent(json)
                }


            }

            /**
             * 设备移除回调
             *
             * @param devId 设备id
             */
            override fun onRemoved(devId: String?) {
                logI(
                    """
                    tuYaOnRemoved:
                    devId: $devId
                """.trimIndent()
                )
                LiveEventBus.get()
                    .with(Constants.Device.KEY_DEVICE_TO_APP, String::class.java)
                    .postEvent(Constants.Device.KEY_DEVICE_REMOVE)
            }

            /**
             * 设备上下线回调。如果设备断电或断网，服务端将会在3分钟后回调到此方法。
             *
             * @param devId  设备 ID
             * @param online 是否在线，在线为 true
             */
            override fun onStatusChanged(devId: String?, online: Boolean) {
                logI(
                    """
                    tuYaOnStatusChanged
                    devId: $devId
                    online: $online
                """.trimIndent()
                )
                LiveEventBus.get()
                    .with(Constants.Device.KEY_DEVICE_TO_APP, String::class.java)
                    .postEvent(if (online) Constants.Device.KEY_DEVICE_ONLINE else Constants.Device.KEY_DEVICE_OFFLINE)
            }

            /**
             * 网络状态发生变动时的回调
             *
             * @param devId  设备 ID
             *  @param status 网络状态是否可用，可用为 true
             */
            override fun onNetworkStatusChanged(devId: String?, status: Boolean) {
            }

            /**
             * 设备信息更新回调
             *
             * @param devId  设备 ID
             */
            override fun onDevInfoUpdate(devId: String?) {
            }
        })
        return super.onStartCommand(intent, flags, startId)
    }
}