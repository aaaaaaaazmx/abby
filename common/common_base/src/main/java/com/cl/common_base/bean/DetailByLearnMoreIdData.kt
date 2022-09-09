package com.cl.common_base.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean

data class DetailByLearnMoreIdData(
    val dateTime: String?,
    val title: String?,
    val items: MutableList<ItemBean>?
) : BaseBean() {
    class ItemBean(
        val content: String?,
        val createTime: String?,
        val extend: BaseBean?,
        val messageId: String?,
        val title: String?,
        val type: String?,
    ) : MultiItemEntity {
        override val itemType: Int
            get() = when (type) {
                "string" -> KEY_TEXT_TYPE
                "picture" -> KEY_IMAGE_TYPE
                else -> KEY_TEXT_TYPE
            }
    }

    companion object {
        const val KEY_TEXT_TYPE = 1
        const val KEY_IMAGE_TYPE = 2
    }
}
