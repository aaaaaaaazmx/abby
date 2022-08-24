package com.cl.modules_login.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

/**
 * 用户注册实体类
 */
@Keep
data class UserRegisterReq(
    val consentAgreement: Int = 1,
    var country: String? = null,
    var countryCode: String? = null,
    var password: String? = null,
    var userName: String? = null,
) : BaseBean()