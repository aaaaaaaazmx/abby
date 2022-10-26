package com.cl.modules_home.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.util.AppUtil

@Keep
data class AutomaticLoginReq(
    var altitude: String? = null,
    var channel: String? = null,
    var city: String? = null,
    var imei: String? = null,
    var latitude: String? = null,
    var longitude: String? = null,
    var mobileBrand: String? = AppUtil.deviceBrand,
    var mobileModel: String? = AppUtil.deviceModel,
    var osType: String? = "1",
    var password: String? = null,
    var timeZone: String? = DateHelper.getTimeZOneNumber().toString(),
    var token: String? = null,
    var userName: String? = null,
    var version: String? = AppUtil.appVersionName,
) : BaseBean()