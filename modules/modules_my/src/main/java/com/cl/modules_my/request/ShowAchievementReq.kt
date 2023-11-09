package com.cl.modules_my.request

import com.cl.common_base.BaseBean

data class ShowAchievementReq(
    var list: MutableList<Int>? = null
):BaseBean()