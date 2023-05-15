package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class ContactEnvData(
    val detectionValue: String? = null,
    val healthStatus: String? = null,
    val value: String? = null,
): BaseBean()