package com.cl.modules_my.request

data class AchievementBean(
    val achDescribe: String,
    val achievementId: Int,
    val isGain: Boolean,
    val name: String,
    val picture: String,
    val type: String,
    var selectedStatus: Boolean = false,
    val goodsCode: String,
    val goodsName: String,
    val goodsType: String,
    val goodsId: Int,
    val title: String,
    val backgroundPicture: String,
)