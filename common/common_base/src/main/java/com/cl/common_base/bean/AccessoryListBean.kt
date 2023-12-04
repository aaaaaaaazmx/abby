package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class AccessoryListBean(
    val accessoryId: Int? = null,
    val accessoryName: String? = null,
    val accessoryType: String? = null, // 用于判断是哪个配件
    val image: String? = null,
    val textId: String? = null,
    val buyLink: String? = null,
    val isAdd: Boolean? = null, // 是否允许被添加
): BaseBean() {
    // fan、humidifier、camera、phb
    companion object {
        const val KEY_FAN = "fan"
        const val KEY_HUMIDIFIER = "humidifier"
        const val KEY_CAMERA = "camera"
        const val KEY_PHB = "phb"

        // - smart_outlets  智能插座
        const val KEY_OUTLETS = "smart_outlets"
        //- monitor_inner  内部温湿度器（tent）
        const val KEY_INNER = "monitor_inner"
        //- monitor_outer  外部温湿度器（tent）
        const val KEY_OUTER = "monitor_outer"
        //- monitor_view   温湿度器（带显示屏）
        const val KEY_VIEW = "monitor_view"
        //- monitor_box     温湿度器（box）
        const val KEY_BOX = "monitor_box"
    }
}