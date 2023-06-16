package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class CommentByMomentReq(
    val current: Int? = null,
    val learnMoreId: Int? = null,
    val momentId: Int? = null,
    val size: Int? = null,
): BaseBean()