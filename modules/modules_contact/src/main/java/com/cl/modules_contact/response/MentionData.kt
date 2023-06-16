package com.cl.modules_contact.response

import com.cl.common_base.BaseBean

data class MentionData(
    val abbyId: String? = null,
    val nickName: String? = null,
    val picture: String? = null,
    val userId: String? = null,
    var isSelect: Boolean? = false
):BaseBean()