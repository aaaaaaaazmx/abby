package com.cl.common_base.bean

import com.cl.common_base.BaseBean
import com.joketng.timelinestepview.TimeLineState

/**
 * 日历后台数据返回接受类
 */
data class CalendarData(
    var date: String? = null,
    var day: String? = null,
    var epoch: String? = null,
    var epochEndTime: String? = null,
    var epochStartTime: String? = null,
    var taskList: MutableList<TaskList>? = null
) : BaseBean() {
    data class TaskList(
        var createTime: String? = null,
        var finishTime: String? = null,
        var isDeleted: String? = null,
        var taskId: String? = null,
        var taskName: String? = null,
        var taskStatus: String? = null,
        var taskTime: String? = null,
        var taskType: String? = null,
        var updateTime: String? = null,
    ) : com.joketng.timelinestepview.bean.BaseBean(timeLineState = TimeLineState.INACTIVE)


    // 主要任务
    companion object {

    }
}