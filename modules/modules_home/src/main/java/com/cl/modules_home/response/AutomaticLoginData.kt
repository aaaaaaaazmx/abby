package com.cl.modules_home.response

import androidx.annotation.Keep
import com.cl.common_base.ext.DateHelper
import java.io.Serializable

@Keep
data class AutomaticLoginData(
    val abbyId: String? = null,
    val avatarPicture: String? = null,
    val childLock: String? = null,
    val deviceOnlineStatus: String? = null,
    val deviceStatus: String? = null,
    val easemobId: String? = null,
    val easemobPassword: String? = null,
    val easemobUserName: String? = null,
    val email: String? = null,
    val eventCount: String? = null,
    val isVip: Int? = null,
    val nickName: String? = null,
    val subscriptionTime: String? = null,
    val token: String? = null,
    val tuyaCountryCode: String? = null,
    val tuyaPassword: String? = null,
    val tuyaUserId: String? = null,
    val tuyaUserType: String? = null,
    var timeZone: String? = DateHelper.getTimeZOneNumber().toString(),
    ) : Serializable