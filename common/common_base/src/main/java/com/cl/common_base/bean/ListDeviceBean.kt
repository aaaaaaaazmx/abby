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
) : BaseBean() {
}