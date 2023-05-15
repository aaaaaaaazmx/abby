package com.cl.modules_contact.response

data class ImageUrl(
    val createTime: String,
    val extend: Extend,
    val imageUrl: String,
    val isDeleted: Int,
    val md5: String,
    val momentsId: Int,
    val sequence: Int,
    val thumbImageUrl: String,
    val updateTime: String
)