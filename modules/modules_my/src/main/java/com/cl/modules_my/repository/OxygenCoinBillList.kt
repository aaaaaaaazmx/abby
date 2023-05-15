package com.cl.modules_my.repository

data class OxygenCoinBillList(
    val flowing: List<Flowing>,
    val total: Int,
    val yestodayIncome: String
)