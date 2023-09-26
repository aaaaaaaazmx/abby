package com.cl.modules_my.request

import com.cl.common_base.BaseBean

data class MyPlantInfoData(
    val plantName: String? = null,
    var isSelected: Boolean = false
) : BaseBean()