package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class SaveTaskReq(
    val step: String? = null,
    val templateId: String? = null,
    val useOfficialCalendar: Boolean? = null,
    val setupLater: Boolean? = false,
    val taskContent: MutableList<Task>? = null,
    val multiplants: MutableList<Task.PlantList>? = null,
): BaseBean()