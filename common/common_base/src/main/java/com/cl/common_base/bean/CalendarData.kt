package com.cl.common_base.bean

import com.cl.common_base.BaseBean
import com.joketng.timelinestepview.TimeLineState

/**
 * 日历后台数据返回接受类
 */
data class CalendarData(
    var date: String? = null,
    var day: String? = null,
    var week: String? = null,
    var epoch: String? = null,
    var epochEndTime: String? = null,
    var epochExplain: String? = null,
    var epochStartTime: String? = null,
    var plantId: String? = null,
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
        var taskCategory: String? = null,
    ) : com.joketng.timelinestepview.bean.BaseBean(timeLineState = TimeLineState.INACTIVE)


    // 主要任务
    companion object {
        // 决定日历上任务点的颜色
        const val TYPE_CHANGE_WATER = "change_water_task"
        const val TYPE_TRAIN = "train_task"
        const val TYPE_PERIOD_CHECK = "period_check_task"
        const val TYPE_ACADEMY_TASK = "academy_task" // 学院任务

        // taskType
        // 任务类型
        const val TASK_TYPE_CHANGE_WATER = "change_water"
        const val TASK_TYPE_CHANGE_CUP_WATER = "change_cup_water"
        const val TASK_TYPE_LST = "lst"
        const val TASK_TYPE_TOPPING = "topping"
        const val TASK_TYPE_TRIM = "trim"
        const val TASK_TYPE_CHECK_TRANSPLANT = "check_transplant"
        const val TASK_TYPE_CHECK_CHECK_FLOWERING = "check_flowering"
        const val TASK_TYPE_CHECK_CHECK_FLUSHING = "check_flushing"
        const val TASK_TYPE_CHECK_CHECK_HARVEST = "check_harvest"
        const val TASK_TYPE_CHECK_CHECK_DRYING = "check_drying"
        const val TASK_TYPE_CHECK_CHECK_CURING = "check_curing"
        const val TASK_TYPE_CHECK_CHECK_FINISH = "check_finish"
        const val TASK_TYPE_TEST = "test" // 学院任务
    }
}