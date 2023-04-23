package com.cl.modules_contact.response

import com.cl.common_base.BaseBean

data class CommentByMomentData(
    val abbyId: String? = null,
    val comment: String? = null,
    val commentId: String? = null,
    val commentName: String? = null,
    val createTime: String? = null,
    val isPraise: Int? = null,
    val isReward: Int? = null,
    val parentComentId: String? = null,
    val picture: String? = null,
    val praise: Int? = null,
    val reward: Int? = null,
    val userId: String? = null,
    var nickName: String? = null,
    val replys: MutableList<Replys>? = null,
) : BaseBean() {
    data class Replys(
        val abbyId: String? = null,
        val comment: String? = null,
        val commentId: String? = null,
        val commentName: String? = null,
        val createTime: String? = null,
        val isPraise: Int? = null,
        val isReward: Int? = null,
        val parentReplyId: String? = null,
        val parentComentId: String? = null,
        val picture: String? = null,
        val praise: Int? = null,
        val replyId: String? = null,
        val reward: Int? = null,
        val userId: String? = null,
        val userReplyId: String? = null,
        val atName: String? = null,
        var nickName: String? = null,
    ) : BaseBean()
}
