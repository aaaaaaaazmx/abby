package com.cl.modules_contact.response

data class Record(
    val id: Int,
    val imageUrl: String,
    val isDeleted: Int,
    val md5: String,
    val momentsId: Int,
    val sequence: Int,
    val thumbImageUrl: String
)