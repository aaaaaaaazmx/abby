package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

@Keep
data class UpdateInfoReq(
    val binding: Boolean? = null,
    val deviceId: String? = null,
    val storageModel: Int? = null,
    val privateModel: Boolean? = null,
): BaseBean()