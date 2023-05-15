package com.cl.modules_contact.response

data class CommentDetailsData(
    val abbyId: String,
    val avatarPicture: String,
    val comment: Int,
    val comments: List<Comment>,
    val content: String,
    val createTime: String,
    val day: Int,
    val environment: String,
    val healthStatus: String,
    val height: Int,
    val hot: Int,
    val id: Int,
    val imageUrls: List<ImageUrl>,
    val isComment: Int,
    val isPraise: Int,
    val isReward: Int,
    val isTop: Int,
    val journeyName: String,
    val learnMoreId: String,
    val link: String,
    val mentions: MutableList<Mention>,
    val nickName: String,
    val openData: Int,
    val praise: Int,
    val proMode: String,
    val reward: Int,
    val syncTrend: Int,
    val userId: String,
    val week: Int
)