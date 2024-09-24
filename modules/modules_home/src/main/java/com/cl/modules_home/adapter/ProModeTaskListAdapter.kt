package com.cl.modules_home.adapter

import android.annotation.SuppressLint
import com.cl.modules_home.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.modules_home.databinding.HomeProModeTaskItemBinding
import com.cl.modules_home.request.Task


class ProModeTaskListAdapter(data: MutableList<Task>?) :
    BaseQuickAdapter<Task, BaseDataBindingHolder<HomeProModeTaskItemBinding>>(R.layout.home_pro_mode_task_item, data) {


    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun convert(holder: BaseDataBindingHolder<HomeProModeTaskItemBinding>, item: Task) {
        holder.dataBinding?.apply {
            taskData = item
            executePendingBindings()
        }
    }
}