package com.cl.modules_my.adapter

import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.util.calendar.Calendar
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyCalendarItemBinding
import com.cl.modules_my.databinding.MyTroubleItemBinding
import com.cl.modules_my.repository.MyTroubleData


class MyTroubleAdapter(data: MutableList<MyTroubleData.Bean>?) :
    BaseQuickAdapter<MyTroubleData.Bean, BaseViewHolder>(R.layout.my_trouble_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyTroubleItemBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: MyTroubleData.Bean) {
        // 获取 Binding
        val binding: MyTroubleItemBinding? = holder.getBinding()
        binding?.apply {
            data = item
            executePendingBindings()
        }
    }
}