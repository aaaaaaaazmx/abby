package com.cl.modules_my.request

import com.cl.common_base.BaseBean

data class AccountFlowingReq(
    val current: Int? = null,
    val size: Int? = null,
):BaseBean()