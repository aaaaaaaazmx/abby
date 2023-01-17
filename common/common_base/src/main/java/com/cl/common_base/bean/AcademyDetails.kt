package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class AcademyDetails(
    val describe: String? = null,
    val extend: String? = null,
    var isRead: Int? = null,
    val picture: String? = null,
    val title: String? = null,
    val txtId: String? = null,
    val createTime: Long? = null,
    val id: String? = null,
    val academyId: String? = null,
): BaseBean()