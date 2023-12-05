package com.cl.modules_my.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyOutletsListItemBinding
import com.cl.modules_my.request.AccessData

class OutletsAdapter(data: MutableList<AccessData>?) :
    BaseQuickAdapter<AccessData, BaseDataBindingHolder<MyOutletsListItemBinding>>(R.layout.my_outlets_list_item, data) {

    override fun convert(holder: BaseDataBindingHolder<MyOutletsListItemBinding>, item: AccessData) {
        holder.dataBinding?.apply {
            datas = item
            executePendingBindings()
        }
    }
}