package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class CalendarNewData(val templateId: String? = null, val proMode: Boolean ? = null, val startRunning: Boolean? = null, var list: MutableList<CalendarData>): BaseBean()