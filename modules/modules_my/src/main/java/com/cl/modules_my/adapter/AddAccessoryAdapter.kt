package com.cl.modules_my.adapter

import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyAddAccessoryListItemBinding
import com.cl.common_base.bean.AccessoryListBean

class AddAccessoryAdapter(data: MutableList<AccessoryListBean>?) :
    BaseQuickAdapter<AccessoryListBean, BaseViewHolder>(R.layout.my_add_accessory_list_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyAddAccessoryListItemBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: AccessoryListBean) {
        // 获取 Binding
        val binding: MyAddAccessoryListItemBinding? = holder.getBinding()
        binding?.apply {
            data = item
            adapter = this@AddAccessoryAdapter
            executePendingBindings()
        }
    }
}