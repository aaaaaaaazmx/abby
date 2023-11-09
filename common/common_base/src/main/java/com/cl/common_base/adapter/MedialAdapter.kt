package com.cl.common_base.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.bean.UserFlag
import com.cl.common_base.databinding.MyMedialListItemBinding

class MedialAdapter(data: MutableList<UserFlag>?) :
    BaseQuickAdapter<UserFlag, BaseDataBindingHolder<MyMedialListItemBinding>>(R.layout.my_medial_list_item, data) {


    override fun convert(holder: BaseDataBindingHolder<MyMedialListItemBinding>, item: UserFlag) {
        holder.dataBinding?.apply {
            info = item
            executePendingBindings()
        }
    }
}