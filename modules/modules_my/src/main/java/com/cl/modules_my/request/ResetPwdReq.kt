package com.cl.modules_my.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

@Keep
data class ResetPwdReq(
    var newPassword: String? = null,
    var password: String? = null,
) : BaseBean()