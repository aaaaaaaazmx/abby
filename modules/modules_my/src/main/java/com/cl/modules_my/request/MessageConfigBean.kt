package com.cl.modules_my.request

data class MessageConfigBean(
    val alert: Boolean,
    val calendar: Boolean,
    val community: Boolean,
    val promotion: Boolean,
    val userId: Int
)