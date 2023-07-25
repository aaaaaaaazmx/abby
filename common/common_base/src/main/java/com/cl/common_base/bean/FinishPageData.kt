package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class FinishPageData(
    val harvestComplete: String?,
    val imageUrl: String?,
    val thumbImageUrl: String?,
    val title: String?,
    val list: MutableList<ListBean>
) : BaseBean() {
    data class ListBean(
        val learnMoreId: String?,
        val title: String?,
        val articleId: String? = null,
    )
}