package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class PeriodListSaveReq(
    val list: List<Req>,
    val templateId: String
): BaseBean()