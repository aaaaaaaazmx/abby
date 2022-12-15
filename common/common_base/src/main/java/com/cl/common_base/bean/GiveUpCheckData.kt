package com.cl.common_base.bean

data class GiveUpCheckData(
    val giveUp: Boolean? = false,
    val url: String? = null
) : BaseBean()