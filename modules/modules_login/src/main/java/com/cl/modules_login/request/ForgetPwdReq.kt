package com.cl.modules_login.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

@Keep
data class ForgetPwdReq(
    var userName: String? = null,
) : BaseBean()