package com.cl.modules_my.adapter

import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyMedialListItemBinding
import com.cl.modules_my.request.UserFlag

class MedialAdapter(data: MutableList<UserFlag>?) :
    BaseQuickAdapter<UserFlag, BaseDataBindingHolder<MyMedialListItemBinding>>(R.layout.my_medial_list_item, data) {


    override fun convert(holder: BaseDataBindingHolder<MyMedialListItemBinding>, item: UserFlag) {
        holder.dataBinding?.apply {
            info = item
            executePendingBindings()
        }
    }
}