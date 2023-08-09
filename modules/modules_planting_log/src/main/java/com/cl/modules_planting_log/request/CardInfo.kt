package com.cl.modules_planting_log.request

data class CardInfo(
    val content: String,
    val icon: String,
    val isEdit: Boolean,
    val logId: Int
)