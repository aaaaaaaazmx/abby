package com.cl.common_base.util.ipc

import android.app.Application
import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.tuya.smart.android.demo.camera.CameraPanelActivity

class CameraUtils {
    companion object {
        fun init(application: Application) {
            FrescoManager.initFresco(application)
        }

        fun ipcProcess(context: Context, devId: String?, devIds: String? = null): Boolean {
            val cameraInstance = ThingIPCSdk.getCameraInstance()
            if (cameraInstance?.isIPCDevice(devId) == true) {
                ARouter
                    .getInstance()
                    .build(RouterPath.Home.PAGE_CAMERA)
                    .withString(Constants.Global.INTENT_DEV_ID, devId)
                    .withString(Constants.Tuya.KEY_DEVICE_ID, devIds)
                    .navigation(context)
                return true
            }
            return false
        }
    }
}