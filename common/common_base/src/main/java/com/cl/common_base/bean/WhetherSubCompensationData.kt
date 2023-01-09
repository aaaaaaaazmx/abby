package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class WhetherSubCompensationData(
    val compensation: Boolean? = null,
    val subscriberTime: String? = null,
): BaseBean()