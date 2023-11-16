package com.cl.common_base.bean

data class MedalPopData(
    val describe: String,
    val name: String,
    val picture: String,
    val popupType: String,
    val relationId: Int,
    val userId: Int,
    val backgroundPicture: String,
    val uuid: String
)