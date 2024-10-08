package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class UpdateFanModelReq(
    val fanModel: String? = null,
    val fanModelShow: String? = null,
): BaseBean()