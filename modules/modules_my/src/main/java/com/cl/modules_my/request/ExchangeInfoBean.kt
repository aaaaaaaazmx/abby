package com.cl.modules_my.request

data class ExchangeInfoBean(
    val description: String,
    val exchangeAmounts: List<String>,
    val exchangeRatio: String,
    val oxygen: Int
)