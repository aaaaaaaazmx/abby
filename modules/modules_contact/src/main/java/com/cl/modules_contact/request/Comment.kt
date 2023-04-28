package com.cl.modules_contact.request

data class Comment(
    val abbyId: String,
    val comment: String,
    val commentId: Int,
    val commentName: String,
    val createTime: String,
    val isPraise: Int,
    val isReward: Int,
    val parentComentId: String,
    val picture: String,
    val praise: Int,
    val replys: List<Reply>,
    val reward: Int,
    val userId: Int
)