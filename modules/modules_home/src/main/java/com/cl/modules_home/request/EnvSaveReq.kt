package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class EnvSaveReq(
    val list: MutableList<EnvParamListBeanItem>? = null,
    val step: String? = null,
    val templateId: String? = null,
    val useRecommend: Boolean? = false,
): BaseBean()