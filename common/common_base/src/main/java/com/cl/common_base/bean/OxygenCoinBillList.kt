package com.cl.common_base.bean

import com.cl.common_base.bean.Flowing

data class OxygenCoinBillList(
    val flowing: List<Flowing>,
    val total: Int,
    val yestodayIncome: String
)