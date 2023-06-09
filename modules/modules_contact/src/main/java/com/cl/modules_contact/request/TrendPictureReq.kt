package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class TrendPictureReq(
    val current: Int? = null,
    val size: Int? = null,
): BaseBean()