package com.cl.modules_contact.request

data class AddTrendReq(
    val content: String? = null,
    val imageUrls: List<ImageUrl>? = null,
    val link: String? = null,
    val mentions: List<Mention>? = null,
    val openData: Int? = null,
    val ph: String? = null,
    val syncTrend: Int? = null,
    val taskId: Int? = null,
    val tds: String? = null,
)