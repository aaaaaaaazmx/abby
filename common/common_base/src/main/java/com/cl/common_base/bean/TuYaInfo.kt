package com.cl.common_base.bean

data class TuYaInfo(
    val tuyaCountryCode: String? = null,
    val tuyaPassword: String? = null,
    val tuyaUserId: String? = null,
    val tuyaUserType: String? = null,
) : BaseBean()