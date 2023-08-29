package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class LogListDataItem(
    val dateFormat: String,
    val list: MutableList<CardInfo>,
    val period: String,
    val periodInfo: String,
): BaseBean()