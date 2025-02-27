package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class EnvironmentInfoReq(
    var brightValue: Int? = null,
    var deviceId: String? = null,
    var humidityCurrent: Int? = null,
    var inputAirFlow: Int? = null,
    var tempCurrent: Int? = null,
    var ventilation: Int? = null,
    var waterLevel: String? = null,
    var waterTemperature: Int? = null,
    var airpump: Boolean? = null, // 气泵
    var proMode: String? = null, // 专业模式
    var fanAuto: Int? = null, // 风扇是否自动
): BaseBean()