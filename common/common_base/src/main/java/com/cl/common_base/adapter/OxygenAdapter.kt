package com.cl.common_base.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.bean.RunnerWater
import com.cl.common_base.databinding.MyItemOxyDetailBinding

class OxygenAdapter (
    data: MutableList<RunnerWater>?,
) :
    BaseQuickAdapter<RunnerWater, BaseDataBindingHolder<MyItemOxyDetailBinding>>(R.layout.my_item_oxy_detail, data) {

    override fun convert(holder: BaseDataBindingHolder<MyItemOxyDetailBinding>, item: RunnerWater) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }



    }
}