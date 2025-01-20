package com.cl.modules_my.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.CalendarData
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyCalendarSubPlantListItemBinding
import com.cl.modules_my.databinding.MyCalendarTaskListItemBinding

class SubChooseAdapter(data: MutableList<CalendarData.TaskList.SubPlantList>?) :
    BaseQuickAdapter<CalendarData.TaskList.SubPlantList, BaseDataBindingHolder<MyCalendarSubPlantListItemBinding>>(R.layout.my_calendar_sub_plant_list_item, data) {
    override fun convert(holder: BaseDataBindingHolder<MyCalendarSubPlantListItemBinding>, item: CalendarData.TaskList.SubPlantList) {
        holder.dataBinding?.apply {
            taskData = item
            position = holder.layoutPosition
            executePendingBindings()
        }
    }
}