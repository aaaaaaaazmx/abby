package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class TrickData(
    val deviceId: String? = null,
    val everyEndTime: String? = null,
    val everyMinute: String? = null,
    val everyStartTime: String? = null,
    val status: String? = null,
    val turnOnSecond: String? = null,
): BaseBean()