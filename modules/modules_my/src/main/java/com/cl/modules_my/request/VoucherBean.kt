package com.cl.modules_my.request

data class VoucherBean(
    val amount: String,
    val discountCode: String,
    val endTime: String,
    val icon: String,
    val startTime: String,
    val status: Int,
    val title: String,
    val type: String,
    val url: String
)