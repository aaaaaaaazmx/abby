package com.cl.common_base.bean

import com.cl.common_base.BaseBean

/**
 * AppVersionData 版本检查
 * @author 李志军 2022-08-16 15:36
 */
data class AppVersionData(
    val createTime: String? = null,
    val forcedUpdate: String? = null,
    val id: String? = null,
    val isDeleted: String? = null,
    val osType: String? = null,
    val updateTime: String? = null,
    val version: String? = null,
    val versionNote: String? = null,
) : BaseBean()