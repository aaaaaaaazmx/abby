package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

/**
 * 检查种植
 */
@Keep
data class CheckPlantData(
    val plantExistingStatus: String? = null,
    val plantGuideFlag: String? = null,
    val deviceType: String? = null,
    var proMode: String? = null, // 是否是手动、自动模式
): BaseBean()