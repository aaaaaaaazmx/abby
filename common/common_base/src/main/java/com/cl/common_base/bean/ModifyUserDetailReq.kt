package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.bean.BaseBean


@Keep
data class ModifyUserDetailReq(
    var abbyId: String? = null,
    var avatarPicture: String? = null,
    var childLock: String? = null,
    var email: String? = null,
    var newMessage: String? = null,
    var nickName: String? = null,
    var nightMode: String? = null,
    var nightTimer: String? = null,
    var personSign: String? = null,
    var openNotify: String? = null,
    var userName: String? = null,
    var wallAddress: String? = null,
    var wallId: String? = null,
    var inchMetricMode: String? = null,
    var smartUsbPowder: String? = null, // USB开关：1-开启、0-关闭
): BaseBean()