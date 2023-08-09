package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class LogListReq(
    val current: Int,
    val period: String,
    val plantId: Int,
    val size: Int
): BaseBean()