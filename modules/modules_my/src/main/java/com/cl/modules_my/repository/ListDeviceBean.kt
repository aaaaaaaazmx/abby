package com.cl.modules_my.repository

import com.cl.common_base.BaseBean

data class ListDeviceBean(
    var deviceId: String? = null,
    var deviceName: String? = null,
    var onlineStatus: String? = null,
    var period: String? = null,
    var subscription: String? = null,
) : BaseBean() {
}