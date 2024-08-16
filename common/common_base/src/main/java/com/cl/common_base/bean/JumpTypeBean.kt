package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class JumpTypeBean(
    val subscribe: Boolean? = null ,
    val pleaseSubscribe: String? = null ,
    val onOnOne: String? = null ,
    val subscribeNow: String? = null,
) : BaseBean()