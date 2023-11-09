package com.cl.common_base.bean

import com.cl.common_base.BaseBean


data class UpdateFollowStatusReq(
    val followStatus: Boolean,
    val otherUserId: String
): BaseBean()