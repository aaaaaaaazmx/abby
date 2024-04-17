package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class PlantData(
    val humidityList: MutableList<DataPoint>? = null,
    val termpertureList: MutableList<DataPoint>? = null,
    val phList: MutableList<DataPoint>? = null,
    val plantName: String? = null,
    val period:String? = null,
): BaseBean() {
    data class DataPoint(val codeValue: String? = null, val dateTime: String? = null): BaseBean()
}

