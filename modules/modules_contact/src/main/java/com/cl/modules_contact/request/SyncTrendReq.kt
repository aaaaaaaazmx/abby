package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class SyncTrendReq(
    val syncTrend: Int? = null,
    val momentId: String? = null,
):BaseBean()