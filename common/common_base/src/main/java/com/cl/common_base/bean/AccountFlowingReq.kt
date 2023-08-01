package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class AccountFlowingReq(
    val current: Int? = null,
    val size: Int? = null,
):BaseBean()