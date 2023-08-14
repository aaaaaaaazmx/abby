package com.cl.modules_planting_log.request

data class FieldAttributes(
    val description: String,
    val hintDescription: String,
    val unit: String, // 这是第四个参数
    val inputType: String,
)