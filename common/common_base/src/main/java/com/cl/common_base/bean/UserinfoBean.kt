package com.cl.common_base.bean

import androidx.annotation.Keep

@Keep
data class UserinfoBean(
    val abbyId: String? = null,
    val avatarPicture: String? = null,
    val childLock: String? = null,
    val deviceOnlineStatus: String? = null,  // 0-不在线，1-在线
    val deviceStatus: String? = null, // 1 绑定状态、其他是设备未绑定
    val easemobId: String? = null,
    val easemobPassword: String? = null,
    val easemobUserName: String? = null,
    val email: String? = null,
    val eventCount: String? = null,
    val isVip: Int? = 0,
    val nickName: String? = null,
    val subscriptionTime: String? = null,
    val token: String? = null,
    val tuyaCountryCode: String? = null,
    val tuyaPassword: String? = null,
    val tuyaUserId: String? = null,
    val tuyaUserType: String? = null,
    var userDetailData: BasicUserBean? = null,
    val notBound: Int? = null, // 1 是绑定过、其他都是未绑定过
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
        val subscriptionTime: String? = null,
        val isVip: Int? = 0,
    ) : BaseBean()
}