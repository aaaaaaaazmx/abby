package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class DripListData(
    var dripIrrigationTimerStatus: Boolean? = null,
    var list: MutableList<DripData>? = null,

    ) : BaseBean() {
    data class DripData(
        val name: String? = null,
        val deviceId: String,
        val everyEndTime: Int,
        val everyMinute: Int,
        val everyStartTime: Int,
        val irrigationId: Int,
        val status: Boolean,
        val turnOnSecond: Int,
        var turnOnHour: Int? = null,
        var turnOffHour: Int? = null,
    ) : BaseBean()
}