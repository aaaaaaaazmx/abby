package com.cl.common_base.bean

data class SystemConfigBeanItem(
    val code: String,
    val description: String,
    val env: String,
    val id: Int,
    val isDeleted: Int,
    val `param`: String,
    val value: String
)