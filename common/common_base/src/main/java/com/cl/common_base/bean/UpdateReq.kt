package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class UpdateReq(
    val createTime: String? = null,
    val finishTime: String? = null,
    val isDeleted: String? = null,
    val taskId: String? = null,
    val taskName: String? = null,
    val taskStatus: String? = null,
    val taskTime: String? = null,
    val taskType: String? = null,
    val updateTime: String? = null,
): BaseBean()