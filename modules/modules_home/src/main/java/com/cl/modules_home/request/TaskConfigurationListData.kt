package com.cl.modules_home.request

import com.cl.common_base.BaseBean

data class TaskConfigurationListData(
    val list: List<Task>,
    val useOfficial: Boolean
): BaseBean()