package com.cl.modules_login.repository

import com.cl.common_base.BaseBean

data class BindSourceEmailReq(
    val email: String? = null,
    val source: String? = null,
    val sourceUserId: String? = null
): BaseBean()
