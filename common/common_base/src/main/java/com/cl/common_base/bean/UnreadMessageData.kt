package com.cl.common_base.bean

import com.cl.common_base.BaseBean
import com.google.gson.annotations.SerializedName

/**
 *
 * 未读消息
 *
 * @author 李志军 2022-08-15 13:36
 */
data class UnreadMessageData(
    val abbyId: String? = null,
    val content: String? = null,
    var extension: String? = null,
    val jumpType: String? = null,
    val notifyTitle: String? = null,
    val sendTime: String? = null,
    val title: String? = null,
    @SerializedName("taskType")
    var type: String? = null,
    var taskTime: String? = null,
    var taskId: String? = null,
    val category: Int? = null,
    val messageId: Int? = null,
    val sort: Int? = null,
    val time: Int? = null,
    val articleId: String? = null,
    val LocalReceiveTime: String? = null
) : BaseBean()