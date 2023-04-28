package com.cl.modules_contact.request

data class Reply(
    val abbyId: String,
    val comment: String,
    val commentId: Int,
    val commentName: String,
    val createTime: String,
    val isPraise: Int,
    val isReward: Int,
    val parentReplyId: Int,
    val picture: String,
    val praise: Int,
    val replyId: Int,
    val reward: Int,
    val userId: Int,
    val userReplyId: Int
)