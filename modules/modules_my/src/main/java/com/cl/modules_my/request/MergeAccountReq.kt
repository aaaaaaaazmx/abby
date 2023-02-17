package com.cl.modules_my.request

import androidx.annotation.Keep
import com.cl.common_base.bean.BaseBean


@Keep
data class MergeAccountReq(
    var code: String? = null,
    var email: String? = null,
    var mergeEmail: String? = null,
): com.cl.common_base.BaseBean()