package com.cl.common_base.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.bean.PlantIdByDeviceIdData
import com.cl.common_base.databinding.PlantingItemPeriodBinding

class PlantPeriodAdapter(data: MutableList<PlantIdByDeviceIdData>?) :
    BaseQuickAdapter<PlantIdByDeviceIdData, BaseDataBindingHolder<PlantingItemPeriodBinding>>(R.layout.planting_item_period, data) {
    override fun convert(holder: BaseDataBindingHolder<PlantingItemPeriodBinding>, item: PlantIdByDeviceIdData) {
        holder.getView<CheckBox>(R.id.check_period).apply {
            isChecked = item.isSelected
            text = item.plantName ?: item.plantId.toString()
            if (item.isSelected) {
                setTextColor(resources.getColor(com.cl.common_base.R.color.mainColor))
            }  else {
                setTextColor(resources.getColor(com.cl.common_base.R.color.black))
            }
        }
    }
}