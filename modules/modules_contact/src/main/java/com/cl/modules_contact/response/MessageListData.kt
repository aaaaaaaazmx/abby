package com.cl.modules_contact.response

import com.cl.common_base.BaseBean

data class MessageListData(
    val avatarPictures: MutableList<String>? = null,
    val contentBlod: MutableList<String>? = null,
    val content: String? = null,
    val createTime: String? = null,
    val picture: String? = null,
    val id: Int? = null,
    val learnMoreId: Int? = null,
    val momentId: Int? = null,
): BaseBean()