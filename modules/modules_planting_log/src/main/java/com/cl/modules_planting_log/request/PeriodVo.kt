package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class PeriodVo(
    val period: String,
    val startTime: Long,
    val optional: Boolean,
    var isSelect: Boolean = false,
): BaseBean()