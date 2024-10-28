package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class Req(
    val periodId: Int,
    val step: String,
    val stepDay: Int,
    val stepShow: String,
    var currentStep: Boolean? = false
): BaseBean()