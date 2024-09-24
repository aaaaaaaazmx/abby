package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class CheckEnvData(
    val checkMeg: String? = null,
    val checkStatus: Int? = null,
    val errorEnvId: MutableList<String>? = null,
):BaseBean()