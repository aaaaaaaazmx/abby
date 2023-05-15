package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class ReplyReq(
    val comment: String? = null,
    val commentId: String? = null,
    val replyId: String? = null,
): BaseBean()
