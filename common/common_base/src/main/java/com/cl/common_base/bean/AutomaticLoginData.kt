package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.ext.DateHelper
import java.io.Serializable

@Keep
data class AutomaticLoginData(
    val abbyId: String? = null,
    val deviceId: String? = null,
    val avatarPicture: String? = null,
    val childLock: String? = null,
    val deviceOnlineStatus: String? = null, // 设备在线状态(0-不在线，1-在线)
    val deviceStatus: String? = null,    // 设备状态(1-绑定，2-已解绑)
    val easemobId: String? = null,
    // val easemobPassword: String? = null,
    // val easemobUserName: String? = null,
    val email: String? = null,
    val eventCount: Int? = null,
    val isVip: Int? = 0,
    val nickName: String? = null,
    val subscriptionTime: String? = null,
    val token: String? = null,
    val tuyaCountryCode: String? = null,
    val tuyaPassword: String? = null,
    val tuyaUserId: String? = null,
    val tuyaUserType: String? = null,
    val notBound: Int? = null, // 是否绑定过
    var timeZone: String? = DateHelper.getTimeZOneNumber().toString(),
    val userId: String? = null,
    val externalId: String? = null,
    ) : Serializable