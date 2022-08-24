package com.cl.modules_home.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

@Keep
data class AutomaticLoginReq(
    var altitude: String? = null,
    var channel: String? = null,
    var city: String? = null,
    var imei: String? = null,
    var latitude: String? = null,
    var longitude: String? = null,
    var mobileBrand: String? = null,
    var mobileModel: String? = null,
    var osType: String? = null,
    var password: String? = null,
    var timeZone: String? = null,
    var token: String? = null,
    var userName: String? = null,
    var version: String? = null,
) : BaseBean()