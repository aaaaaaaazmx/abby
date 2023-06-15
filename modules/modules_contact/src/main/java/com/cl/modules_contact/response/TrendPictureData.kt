package com.cl.modules_contact.response

data class TrendPictureData(
    val countId: String,
    val current: Int,
    val hitCount: Boolean,
    val maxLimit: Int,
    val optimizeCountSql: Boolean,
    val orders: List<Order>,
    val pages: Int,
    val records: List<Record>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)