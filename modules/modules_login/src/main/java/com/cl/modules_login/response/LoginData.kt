package com.cl.modules_login.response

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class LoginData(
    val abbyId: String? = null,
    val avatarPicture: String? = null,
    val childLock: String? = null,
    val deviceOnlineStatus: String? = null,
    val deviceStatus: String? = null,
    val easemobId: String? = null,
    // val easemobPassword: String? = null,
    // val easemobUserName: String? = null,
    val email: String? = null,
    val eventCount: String? = null,
    val isVip: String? = null,
    val nickName: String? = null,
    val userName: String? = null,
    val subscriptionTime: String? = null,
    val token: String? = null,
    val tuyaCountryCode: String? = null,
    val tuyaPassword: String? = null,
    val tuyaUserId: String? = null,
    val tuyaUserType: String? = null,
    val notBound: Int? = null,
    val deviceId: String? = null,
    val userId: String? = null
) : Serializable