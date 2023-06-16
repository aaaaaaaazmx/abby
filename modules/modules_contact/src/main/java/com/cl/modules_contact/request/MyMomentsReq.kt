package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class MyMomentsReq(val current: Int? = null, val size: Int? = null, val userId: String? = null) : BaseBean()