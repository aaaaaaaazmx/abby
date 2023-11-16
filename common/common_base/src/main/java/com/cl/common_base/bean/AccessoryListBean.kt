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
    }
}