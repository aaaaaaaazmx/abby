package com.cl.modules_login.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

@Keep
data class UpdatePwdReq(
    var password: String? = null,
    var userEmail: String? = null,
    var autoCode: String? = null
) : BaseBean()