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
    var articleId: String? = null,
    var articleDetails: String? = null,
    var epochEndTime: String? = null,
    var step: String? = null,
    var epochExplain: String? = null,
    var epochStartTime: String? = null,
    var plantId: String? = null,
    var taskList: MutableList<TaskList>? = null
) : BaseBean() {
    data class TaskList(
        var deleted: Boolean? = null,
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
        var articleId: String? = null,
        var articleDetails: String? = null,
        var subTaskList: MutableList<SubTaskList>? = null,
        var packetCondition: PacketCondition? = null,
    ) : com.joketng.timelinestepview.bean.BaseBean(timeLineState = TimeLineState.INACTIVE) {
        data class SubTaskList(
            var jumpType: String? = null,
            var textId: String? = null,
            var name: String? = null,
            var taskNo: String? = null,
            var description: String? = null,
            var isDeleted: Boolean? = null,
            var jumpJson: String? = null,
            var id: Int? = null,
            var templateId: String? = null,
        ) : BaseBean()

        data class PacketCondition(
            var taskPackes: MutableList<TaskPackes>? = null,
            var conditionNo: String? = null,
            var content: String? = null,
        ) : BaseBean() {
            data class TaskPackes(
                var condition: String? = null,
                var packetNo: PacketNo? = null
            ) : BaseBean() {
                data class PacketNo(
                    var subTaskList: MutableList<SubTaskList>? = null,
                    var name: String? = null,
                    var packetNo: String? = null,
                ) : BaseBean()
            }
        }
    }


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
        const val TASK_TYPE_CHECK_CHECK_AUTOFLOWERING = "check_autoflowering"
        const val TASK_TYPE_TEST = "test" // 学院任务
        // drying任务完成
        const val TASK_TYPE_CHECK_CHECK_FLOWERING_DONE = "P016"


        // 富文本类型
        const val ABOUT_PAGE_NOT_PURCHASED_TASK = "page_not_purchased"
        const val ABOUT_RECORD_JOURNEY_TASK = "record_journey"
        const val ABOUT_HOW_TO_PICK_STRAIN_TASK = "how_to_pick_strain"
        const val ABOUT_CHECK_TRANSPLANT_TASK = "about_check_transplant"
        const val ABOUT_CHECK_FLOWERING_TASK = "about_check_flowering"
        const val ABOUT_CHECK_FLUSHING_TASK = "about_check_flushing"
        const val ABOUT_CHECK_DRYING_TASK = "about_check_drying"
        const val ABOUT_CHECK_CURING_TASK = "about_check_curing"
        const val ABOUT_CHECK_AUTO_FLOWERING_TASK = "about_check_auto_flowering"
        const val ABOUT_CHECK_FINISH_TASK = "about_check_finish"
        const val SEED_KIT_CUP_TYPE_TASK = "seed_kit_cup_type"


        // 跳转类型
        const val KEY_JUMP_TYPE_TO_RICH = "rich_text"
        const val KEY_JUMP_TYPE_TO_WATER = "drain_water"
        // 需要注意的是，taskNo- 当任务也就是日历界面pop_up交互时传 taskNo，也就是富文本界面textId-当页面交互时传两个值都需要传taskNo和textId
        const val KEY_JUMP_TYPE_POP_UP = "pop_up"

        // taskNo
        const val KEY_TASK_NO = "task_no"
    }
}