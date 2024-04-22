package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class PlantIdByDeviceIdData(
    val deviceId: String,
    val plantId: Int,
    val plantName: String? = null,
    var isSelected: Boolean = false
) : BaseBean()