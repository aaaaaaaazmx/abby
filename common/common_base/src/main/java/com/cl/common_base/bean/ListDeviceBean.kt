package com.cl.common_base.bean

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
    var isSwitch: Int? = null,
    var childLock: Int? = null,
    var nightMode: Int? = null,
    var nightTimer: String? = null,
    var currentDevice: Int? = null,
    var proMode: String? = null, // 是否是手动、自动模式
    var fanAuto: Int? = null,
    var accessoryList: MutableList<AccessoryList>? = null,
) : BaseBean() {
    data class AccessoryList(
        val isAuto: Int? = null,
        val accessoryId: Int? = null,
        val accessoryName: String? = null,
        val accessoryDeviceId: String? = null,
        val image: String? = null,
        val textId: String? = null,
        val status: Int? = null,
    ) : BaseBean()
}