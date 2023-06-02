package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

/**
 * 广告图文
 * @author 李志军 2022-08-10 15:24
 */
@Keep
data class AdvertisingData(
    val avatarPicture: String?,
    val description: String?,
    val id: Int?,
    val isPraise: Int?,
    val isReward: Int?,
    val nickName: String?,
    val picture: String?,
    val praise: Int?,
    val reward: Int?,
    val title: String?,
    val video: String?
) : BaseBean()