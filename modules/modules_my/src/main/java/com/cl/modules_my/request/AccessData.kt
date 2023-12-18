package com.cl.modules_my.request

data class AccessData(
    val accessoryDeviceId: String,
    val portId: String,
    val pricture: String,
    val sort: Int,
    val status: Boolean,
    val subName: String,
    val userId: Int
)