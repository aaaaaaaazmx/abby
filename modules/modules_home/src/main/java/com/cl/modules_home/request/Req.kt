package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class Req(
    val periodId: String? = null,
    val step: String,
    val stepDay: Int,
    val stepShow: String,
    var currentStep: Boolean? = false
): BaseBean()