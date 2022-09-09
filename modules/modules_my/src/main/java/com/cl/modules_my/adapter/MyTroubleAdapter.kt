package com.cl.modules_my.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyTroubleItemBinding
import com.cl.modules_my.repository.MyTroubleData


class MyTroubleAdapter(data: MutableList<MyTroubleData.Bean>?) :
    BaseQuickAdapter<MyTroubleData.Bean, BaseDataBindingHolder<MyTroubleItemBinding>>(R.layout.my_trouble_item, data) {

    override fun convert(
        holder: BaseDataBindingHolder<MyTroubleItemBinding>,
        item: MyTroubleData.Bean
    ) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }
    }
}