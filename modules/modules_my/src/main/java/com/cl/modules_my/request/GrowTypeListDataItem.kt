package com.cl.modules_my.request

import com.cl.common_base.BaseBean

class GrowTypeListDataItem(
    val logType: String,
    val showUiText: String,
    var isSelected: Boolean,
): BaseBean()