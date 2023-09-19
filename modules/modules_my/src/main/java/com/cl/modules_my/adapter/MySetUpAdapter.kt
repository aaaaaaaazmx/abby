package com.cl.modules_my.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyItemPeriodBinding
import com.cl.modules_my.databinding.MyItemPeriodBindingImpl
import com.cl.modules_my.request.MyPlantInfoData

class MySetUpAdapter (data: MutableList<MyPlantInfoData>?) :
    BaseQuickAdapter<MyPlantInfoData, BaseDataBindingHolder<MyItemPeriodBinding>>(R.layout.my_item_period, data) {
    override fun convert(holder: BaseDataBindingHolder<MyItemPeriodBinding>, item: MyPlantInfoData) {
        holder.getView<CheckBox>(R.id.check_period).apply {
            isChecked = item.isSelected
            text = item.plantName ?: item.plantName.toString()
            if (item.isSelected) {
                setTextColor(resources.getColor(com.cl.common_base.R.color.mainColor))
            }  else {
                setTextColor(resources.getColor(com.cl.common_base.R.color.black))
            }
        }
    }
}