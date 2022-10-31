package com.cl.common_base.bean

import androidx.annotation.Keep

@Keep
data class UserinfoBean(
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
    var userDetailData: BasicUserBean? = null
) : BaseBean() {
    // 用户基本信息Bean
    // 从用户详情接口返回
    data class BasicUserBean(
        val abbyId: String? = null,
        val avatarPicture: String? = null,
        val childLock: String? = null,
        val email: String? = null,
        val newMessage: Boolean? = false,
        val nickName: String? = null,
        val nightMode: Int? = null,
        val nightTimer: String? = null,
        val openNotify: Int? = null,
        val personSign: String? = null,
        val userName: String? = null,
        val wallAddress: String? = null,
        val wallId: String? = null,
    ) : BaseBean()
}