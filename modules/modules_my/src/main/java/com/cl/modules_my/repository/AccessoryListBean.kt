package com.cl.modules_my.repository

import com.cl.common_base.BaseBean

data class AccessoryListBean(
    val accessoryId: Int? = null,
    val accessoryName: String? = null,
    val image: String? = null,
    val textId: String? = null,
    val buyLink: String? = null,
): BaseBean()