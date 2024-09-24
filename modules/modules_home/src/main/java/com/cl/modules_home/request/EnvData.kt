package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class EnvData(
    val stepStart: String? = null,
    val stepEnd: String? = null,
    val step: String? = null,
    var list: MutableList<EnvParamListBeanItem>? = null
):BaseBean()