package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class TrickData(
    val deviceId: String? = null,
    val everyEndTime: Int? = null,
    val everyMinute: String? = null,
    val everyStartTime: Int? = null,
    val status: String? = null,
    val turnOnSecond: String? = null,
): BaseBean()