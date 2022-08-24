package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

/**
 * 广告图文
 * @author 李志军 2022-08-10 15:24
 */
@Keep
data class AdvertisingData(
    val description: String? = null,
    val picture: String? = null,
    val id: Int? = null,
    val isPraise: Int? = null,
    val isReward: Int? = null,
    val praise: Int? = null,
    val reward: String? = null,
    val title: String? = null,
) : BaseBean()