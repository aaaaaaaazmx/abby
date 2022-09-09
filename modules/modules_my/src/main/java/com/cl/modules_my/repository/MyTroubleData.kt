package com.cl.modules_my.repository

import com.cl.common_base.BaseBean

/**
 * 获取疑问返回结果
 */
data class MyTroubleData(
    val Growing: MutableList<Bean>? = null,
    val abby: MutableList<Bean>? = null,
    val Connect: MutableList<Bean>? = null,
) : BaseBean() {
    data class Bean(
        val content: String? = null,
        val learnMoreId: String? = null,
        val title: String? = null,
    ) : BaseBean()
}