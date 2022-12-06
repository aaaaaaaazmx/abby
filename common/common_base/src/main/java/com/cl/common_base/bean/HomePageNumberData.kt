package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class HomePageNumberData(
    var academyMsgCount: Int? = null,
    var calendarMsgCount: Int? = null,
) : BaseBean()