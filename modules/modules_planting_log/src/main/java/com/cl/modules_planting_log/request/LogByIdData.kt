package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class LogByIdData(
    val co2Concentration: Int,
    val driedWeight: Int,
    val humidity: Int,
    val lightingSchedule: String,
    val logId: Int,
    val logTime: Int,
    val logType: String,
    val notes: String,
    val period: String,
    val ph: String,
    val plantHeight: Int,
    val plantId: Int,
    val plantPhoto: List<Any>,
    val showType: String,
    val spaceTemp: Int,
    val tdsEc: String,
    val trainingAfterPhoto: String,
    val trainingBeforePhoto: String,
    val vpd: Int,
    val waterTemp: Int,
    val wetWeight: Int
): BaseBean()