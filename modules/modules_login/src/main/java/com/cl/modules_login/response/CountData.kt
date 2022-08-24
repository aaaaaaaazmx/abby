package com.cl.modules_login.response

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

@Keep
data class CountData(
    var countryCode: String? = null,
    var countryName: String? = null,
    var tuyaCountryCode: String? = null,
):  BaseBean()