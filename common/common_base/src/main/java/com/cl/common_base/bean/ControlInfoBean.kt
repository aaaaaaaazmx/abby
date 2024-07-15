package com.cl.common_base.bean

data class ControlInfoBean(
    val deviceModel: String,
    val humidityCurrent: String,
    val period: String,
    val plantId: String,
    val plantName: String,
    val plantPeriod: String,
    val strainName: String,
    val taskCurrent: String,
    val taskTime: String,
    val temperatureCurrent: String,
    val waterCurrent: String
)