package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class PlantInfoByPlantIdData(
    val periodInfo: String,
    val periodVoList: List<PeriodVo>,
    val plantId: Int,
    val plantName: String,
    val strainName: String,
    val period: String
): BaseBean()