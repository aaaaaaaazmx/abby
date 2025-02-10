package com.cl.modules_my.request

import com.cl.common_base.BaseBean

data class DeviceShortBean(val period: String, var isSelected: Boolean = false) : BaseBean() {
}