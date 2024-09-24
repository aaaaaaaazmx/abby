package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class DeleteTaskReq(
    val taskId: String? = null,
    val templateId: String? = null
): BaseBean()