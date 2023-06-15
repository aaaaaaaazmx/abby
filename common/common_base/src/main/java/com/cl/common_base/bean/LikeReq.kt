package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class LikeReq(
    val learnMoreId: String? = null,
    val likeId: String? = null,
    val type: String? = null,
): BaseBean()