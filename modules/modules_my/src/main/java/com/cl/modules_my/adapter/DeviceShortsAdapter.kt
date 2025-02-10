package com.cl.modules_my.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceItemShortBinding
import com.cl.modules_my.request.DeviceShortBean

class DeviceShortsAdapter (data: MutableList<DeviceShortBean>?) :
    BaseQuickAdapter<DeviceShortBean, BaseDataBindingHolder<MyDeviceItemShortBinding>>(R.layout.my_device_item_short, data) {
    override fun convert(holder: BaseDataBindingHolder<MyDeviceItemShortBinding>, item: DeviceShortBean) {
        holder.getView<CheckBox>(R.id.check_period).apply {
            isChecked = item.isSelected
            text = item.period
            if (item.isSelected) {
                setTextColor(resources.getColor(com.cl.common_base.R.color.mainColor))
            }  else {
                setTextColor(resources.getColor(com.cl.common_base.R.color.black))
            }
        }
    }
}