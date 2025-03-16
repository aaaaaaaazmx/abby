package com.cl.common_base.bean

import com.cl.common_base.BaseBean
import com.google.firebase.annotations.concurrent.Background

data class AccessoryListBean(
    val accessoryId: Int? = null,
    val accessoryIds: String? = null,
    val deviceId: String? = null,
    val accessoryName: String? = null,
    val accessoryType: String? = null, // 用于判断是哪个配件
    val image: String? = null,
    val textId: String? = null,
    val buyLink: String? = null,
    val isShared: Boolean? = null,
    val desString: String? = null,
    val background: Int? = null,
    val canAdd: Boolean? = null,
    val usbPort: Int? = null,
    var isCheck: Boolean? = true,
    val cannotMsg: String? = null,
    val cameraId: String? = null,
    val isAdd: Boolean? = null, // 是否允许被添加
) : BaseBean() {
    // fan、humidifier、camera、phb
    companion object {
        const val KEY_FAN = "fan"
        const val KEY_HUMIDIFIER = "humidifier"
        const val KEY_CAMERA = "camera"
        const val KEY_PHB = "phb"

        // 补光灯 fill_light
        const val KEY_FILL_LIGHT = "fill_light"

        // - smart_outlets  智能插座
        const val KEY_OUTLETS = "smart_outlets"

        // - monitor_view_out  带屏外部
        const val KEY_MONITOR_VIEW_OUT = "monitor_view_out"

        //- monitor_out 不带屏外部
        const val KEY_MONITOR_OUT = "monitor_out"

        //- monitor_view_in 带屏内部,tent用
        const val KEY_MONITOR_VIEW_IN = "monitor_view_in"

        //- monitor_in 不带屏内部，tent用
        const val KEY_MONITOR_IN = "monitor_in"

    }
}