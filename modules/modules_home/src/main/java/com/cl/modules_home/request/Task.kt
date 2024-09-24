package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class Task(
    var day: String? = null,
    var endTime: Long,
    var recurringDay: String? = "1",
    var recurringTask: Boolean,
    var taskId: String? = null,
    var taskTime: Long,
    var taskName: String? = null,
    var taskType: String? = null,
    var week: String? = null
) : BaseBean()