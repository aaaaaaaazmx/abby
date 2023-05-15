package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class LikeReq(
    val learnMoreId: String? = null,
    val likeId: String? = null,
    val type: String? = null,
): BaseBean()