package com.cl.modules_my.adapter

import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceListItemBinding
import com.cl.common_base.bean.ListDeviceBean
import dagger.Reusable

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
            adapter = this@DeviceListAdapter
            executePendingBindings()
        }
    }

    // 显示名字。
    fun showName(deviceName: String?, planeName: String?, strainName: String?): String {
        if (planeName.isNullOrEmpty() && strainName.isNullOrEmpty()) {
            return deviceName.toString()
        } else if (planeName.isNullOrEmpty() && !strainName.isNullOrEmpty()) {
            return strainName
        } else if (strainName.isNullOrEmpty() && !planeName.isNullOrEmpty()) {
            return planeName
        }
        return deviceName.toString()
    }
}