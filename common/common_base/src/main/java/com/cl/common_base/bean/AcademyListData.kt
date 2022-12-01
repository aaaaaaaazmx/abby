package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class AcademyListData(
    val describe: String? = null,
    val id: String? = null,
    var isRead: String? = null,
    val title: String? = null,
    val picture: String? = null,
): BaseBean()