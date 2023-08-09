package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class PlantIdByDeviceIdData(
    val deviceId: String,
    val plantId: Int,
    val plantName: String? = null,
    var isSelected: Boolean = false
) : BaseBean()