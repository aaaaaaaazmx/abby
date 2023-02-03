package com.cl.modules_my.adapter

import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceListItemBinding
import com.cl.common_base.bean.ListDeviceBean

class DeviceListAdapter(data: MutableList<ListDeviceBean>?) :
    BaseQuickAdapter<ListDeviceBean, BaseViewHolder>(R.layout.my_device_list_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyDeviceListItemBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: ListDeviceBean) {
        // 获取 Binding
        val binding: MyDeviceListItemBinding? = holder.getBinding()
        binding?.apply {
            data = item
            executePendingBindings()
        }
    }
}