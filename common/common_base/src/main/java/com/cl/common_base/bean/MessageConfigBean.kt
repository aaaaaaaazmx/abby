package com.cl.common_base.bean

data class MessageConfigBean(
    val alert: Boolean,
    val calendar: Boolean,
    val community: Boolean,
    val promotion: Boolean,
    val userId: Int
)