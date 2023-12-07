package com.cl.common_base.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean

data class ListDeviceBean(
    var deviceId: String? = null,
    var deviceName: String? = null,
    var strainName: String? = null,
    var plantName: String? = null,
    var onlineStatus: String? = null,
    var period: String? = null,
    var subscription: String? = null,
    var isChooser: Boolean? = null,
    var plantId: Int? = null,
    var picture: String? = null,
    var isShared: Boolean? = null,
    var isSwitch: Int? = null,
    var childLock: Int? = null,
    var nightMode: Int? = null,
    var nightTimer: String? = null,
    var currentDevice: Int? = null,
    var proMode: String? = null, // 是否是手动、自动模式
    var fanAuto: Int? = null,
    val burnOutProof: Int? = null, // 是否开启防烧模式
    val isBurnOutProof: Int? = null, // 是否显示防烧模式
    var deviceType: String? = null, // 判断设备是OG还是O1
    var smartUsbPowder: Int? = null, // USB开关：1-开启、0-关闭
    var spaceType: String? = KEY_SPACE_TYPE_BOX, // 判断是abby还是帐篷
    var textDesc: String? = null, // 文字描述
    var accessoryList: MutableList<AccessoryList>? = null,
) : BaseBean(), MultiItemEntity {
    data class AccessoryList(
        val isAuto: Int? = null,
        val accessoryId: Int? = null,
        val accessoryName: String? = null,
        val accessoryDeviceId: String? = null,
        val accessoryType: String? = null,
        val image: String? = null,
        val textId: String? = null,
        val status: Int? = null,
    ) : BaseBean()

    override val itemType: Int
        get() = when(spaceType) {
            KEY_SPACE_TYPE_BOX -> KEY_TYPE_BOX
            KEY_SPACE_TYPE_TENT -> KEY_TYPE_BOX
            KEY_SPACE_TYPE_PH -> KEY_TYPE_PH
            KEY_SPACE_TYPE_TENT_INNER -> KEY_TYPE_TENT_INNER
            KEY_SPACE_TYPE_TENT_OUTER -> KEY_TYPE_TENT_OUTER
            KEY_SPACE_TYPE_ABBY_INNER -> KEY_TYPE_ABBY_INNER
            KEY_SPACE_TYPE_VIEW -> KEY_TYPE_VIEW
            KEY_SPACE_TYPE_TEXT -> KEY_TYPE_TEXT
            else -> KEY_TYPE_PH
        }

    companion object {
        const val KEY_SPACE_TYPE_BOX = "box"
        const val KEY_SPACE_TYPE_TENT = "tent"
        const val KEY_SPACE_TYPE_PH = "phb"
        // 帐篷内部使用的的温湿度传感器
        const val KEY_SPACE_TYPE_TENT_INNER = "monitor_inner"
        // 帐篷外部使用的温湿度传感器
        const val KEY_SPACE_TYPE_TENT_OUTER = "monitor_outer"
        // Abby室内使用的温湿度传感器
        const val KEY_SPACE_TYPE_ABBY_INNER = "monitor_box"
        // 带显示器的温湿度传感器
        const val KEY_SPACE_TYPE_VIEW = "monitor_view"
        // 文字描述类型，自己新增
        const val KEY_SPACE_TYPE_TEXT = "text"

        const val KEY_TYPE_BOX = 1
        const val KEY_TYPE_PH = 2
        // 新增的1个类型。文字描述
        const val KEY_TYPE_TEXT = 3
        // 内部温湿度传感器
        const val KEY_TYPE_TENT_INNER = 4
        // 外部温湿度传感器
        const val KEY_TYPE_TENT_OUTER = 5
        // Abby室内使用的温湿度传感器
        const val KEY_TYPE_ABBY_INNER = 6
        // 带显示器的温湿度传感器
        const val KEY_TYPE_VIEW = 7


    }
}