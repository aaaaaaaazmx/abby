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
): BaseBean()