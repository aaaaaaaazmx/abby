package com.cl.modules_login.response

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_MONITOR_OUT
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_SPACE_TYPE_BOX
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_SPACE_TYPE_PH
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_SPACE_TYPE_TENT
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_SPACE_TYPE_TEXT
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_TYPE_BOX
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_TYPE_PH
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_TYPE_TEXT
import com.cl.common_base.bean.ListDeviceBean.Companion.MONITOR_VIEW_OUT
import com.thingclips.smart.sdk.bean.DeviceBean

data class OffLineDeviceBean(
    var spaceType: String? = null,
    var name: String? = null,
    var devId: String? = null,
    var strainName: String? = null,
    var type: String? = null,
    var isChoose: Boolean? = null,
    var textDesc: String? = null, // 文字描述
) : BaseBean(), MultiItemEntity {
    override val itemType: Int
        get() = when (spaceType) {
            KEY_SPACE_TYPE_BOX -> KEY_TYPE_BOX
            AccessoryListBean.KEY_MONITOR_VIEW_OUT -> MONITOR_VIEW_OUT
            AccessoryListBean.KEY_MONITOR_OUT -> KEY_MONITOR_OUT
            KEY_SPACE_TYPE_TEXT -> KEY_TYPE_TEXT
            else -> KEY_TYPE_PH
        }


    companion object {
        // WiFi Temperature & Humidity
        const val DEVICE_VERSION_O1_TH = "O1_TH"

        // WIFI温湿度传感器
        const val DEVICE_VERSION_O1_TH_CN = "WIFI温湿度传感器"

        const val DEVICE_VERSION_O1 = "O1"

        // 设备是OG
        const val DEVICE_VERSION_OG = "OG"

        // 设备是OG_black
        const val DEVICE_VERSION_OG_BLACK = "OG_black"

        // 设备是OG_Pro
        const val DEVICE_VERSION_OG_PRO = "OG_Pro"

        // 设备是O1_Pro
        const val DEVICE_VERSION_O1_PRO = "O1_Pro"

        // 设备是O1_Soil
        const val DEVICE_VERSION_O1_SOIL = "O1_Soil"

        // 设备是O1-SE
        const val DEVICE_VERSION_O1_SE = "O1_SE"
    }
}