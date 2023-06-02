package com.cl.modules_my.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.CalendarData
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyTaskListItemBinding

/**
 * 右边布局adapter
 */
class TaskListAdapter(
    data: MutableList<CalendarData.TaskList.SubTaskList>?,
) :
    BaseQuickAdapter<CalendarData.TaskList.SubTaskList, BaseDataBindingHolder<MyTaskListItemBinding>>(R.layout.my_task_list_item, data) {

    override fun convert(holder: BaseDataBindingHolder<MyTaskListItemBinding>, item: CalendarData.TaskList.SubTaskList) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }
    }
}