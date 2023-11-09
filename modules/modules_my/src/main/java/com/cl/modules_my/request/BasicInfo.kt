package com.cl.modules_my.request

data class BasicInfo(
    val abbyId: String,
    val avatarPicture: String,
    val follower: Int,
    val following: Int,
    val framesHeads: String,
    val isVip: Int,
    val likes: Int,
    val personSign: String,
    val posts: Int,
    val signLink: String,
    val userId: Int,
    val wallAddress: String,
    val wallId: Int
)