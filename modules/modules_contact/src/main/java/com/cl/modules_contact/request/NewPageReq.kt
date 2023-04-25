package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class NewPageReq(var current: Int? = null, var size: Int? = null, val period: String? = null, val tags: String? = null,) : BaseBean()