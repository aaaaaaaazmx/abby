package com.cl.modules_login.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean
@Keep
 data class BindSourceEmailReq(
    val email: String? = null,
    val source: String? = null,
    val sourceUserId: String? = null
): BaseBean()
