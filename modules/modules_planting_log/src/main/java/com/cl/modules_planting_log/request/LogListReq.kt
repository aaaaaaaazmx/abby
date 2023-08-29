package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class LogListReq(
    val current: Int? = null,
    val period: String? = null,
    val plantId: Int? = null,
    val size: Int
): BaseBean()