package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class CycleListBean(
    val periodId: Int,
    val step: String,
    var stepDay: Int,
    val stepShow: String,
    var isSelect: Boolean,
): BaseBean()