package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

/**
 * 广告图文
 * @author 李志军 2022-08-10 15:24
 */
@Keep
data class AdvertisingData(
    var avatarPicture: String?,
    var description: String?,
    var id: Int?,
    @set:JvmName("setIsFoo")
    var isPraise: Int?,
    @set:JvmName("setIsFod")
    var isReward: Int?,
    var nickName: String?,
    var picture: String?,
    var praise: Int?,
    var reward: Int?,
    var title: String?,
    var video: String?
) : BaseBean()