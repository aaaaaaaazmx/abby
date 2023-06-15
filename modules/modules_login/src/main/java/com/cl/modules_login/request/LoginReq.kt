package com.cl.modules_login.request

import androidx.annotation.Keep
import com.cl.common_base.ext.DateHelper
import java.io.Serializable

@Keep
data class LoginReq(
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
    var timeZone: String? = DateHelper.getTimeZOneNumber().toString(),
    var token: String? = null,
    var userName: String? = null,
    var version: String? = null,
    var autoToken: String? = null,
    var source: String? = null,
    var sourceUserId: String? = null,
) : Serializable