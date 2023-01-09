package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class SyncDeviceInfoReq(
    var airPump: Boolean? = null,
    var startPlant: Boolean? = null,
    var sensorHeightFault: Boolean? = null,
    var door: Boolean? = null,
    var fanEnable: Boolean? = null,
    var brightValue: Int? = null,
    var height: Int? = null,
    var tempCurrent: Int? = null,
    var waterTemperature: Int? = null,
    var turnOffLight: Int? = null,
    var turnOnTheLight: Int? = null,
    var ventilation: Int? = null,
    var humidityCurrent: Int? = null,
    var inputAirFlow: Int? = null,
    var deviceId: String? = null,
    var deviceStatus: String? = null,
    var silentMode: String? = null,
    var waterLevel: String? = null,
): BaseBean()