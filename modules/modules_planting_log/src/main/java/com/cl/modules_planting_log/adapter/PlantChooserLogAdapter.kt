package com.cl.modules_planting_log.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.databinding.PlantingChooserItemPeriodBinding
import com.cl.modules_planting_log.databinding.PlantingPlantLogTypeItemBinding
import com.cl.modules_planting_log.request.PeriodVo
import com.cl.modules_planting_log.request.PlantLogTypeBean

class PlantChooserLogAdapter  (data: MutableList<PlantLogTypeBean>?) :
    BaseQuickAdapter<PlantLogTypeBean, BaseDataBindingHolder<PlantingPlantLogTypeItemBinding>>(R.layout.planting_plant_log_type_item, data) {
    override fun convert(holder: BaseDataBindingHolder<PlantingPlantLogTypeItemBinding>, item: PlantLogTypeBean) {
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