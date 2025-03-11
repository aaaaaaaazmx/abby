package com.cl.modules_login.response

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_MONITOR_OUT
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_SPACE_TYPE_BOX
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_SPACE_TYPE_TEXT
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_TYPE_BOX
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_TYPE_PH
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_TYPE_TEXT
import com.cl.common_base.bean.ListDeviceBean.Companion.MONITOR_VIEW_OUT

data class OffLineDeviceBean(
    var spaceType: String? = null,
    var background: Int? = null,
    var name: String? = null,
    var devId: String? = null,
    var wendu: String? = null,
    var shidu: String? = null,
    var unit: String? = null,
    var strainName: String? = null,
    var type: String? = null,
    var productId: String? = null,
    var isChoose: Boolean? = null,
    var dps: Map<String?, Any?>? = null,
    var accessoryList: MutableList<AccessoryListBean>? = null,
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
        const val DEVICE_VERSION_O1_TH = "ijfxt5wvgjoze4xr"

        // WIFI温湿度传感器
        const val DEVICE_VERSION_O1_TH_CN = "kerdtowolkik1r6m"

        const val DEVICE_VERSION_O1 = "wvyxyxi1f9sb4hai"

        // 设备是OG
        const val DEVICE_VERSION_OG = "wya36i5mrtrgsgtj"

        // 设备是OG_black
        const val DEVICE_VERSION_OG_BLACK = "gfcowzvoda19f1cy"

        // 设备是OG_Pro
        const val DEVICE_VERSION_OG_PRO = "5hbrveeiys7w7de8"

        // 设备是O1_Pro
        const val DEVICE_VERSION_O1_PRO = "x7ujyj9ua97aotao"

        // 设备是O1_Soil
        const val DEVICE_VERSION_O1_SOIL = "uzdrnfk2rmuy2xjy"

        // 设备是O1-SE
        const val DEVICE_VERSION_O1_SE = "gtes0hlvuixt5mu1"

        // 智能摄像机
        const val DEVICE_VERSION_CAMERA = "usop44mvrvrbirme"
    }
}