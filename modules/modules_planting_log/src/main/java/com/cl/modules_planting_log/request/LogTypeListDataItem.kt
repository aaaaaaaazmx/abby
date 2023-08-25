package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class LogTypeListDataItem(
    val logType: String,
    val showUiText: String,
    var isSelected: Boolean,
): BaseBean()