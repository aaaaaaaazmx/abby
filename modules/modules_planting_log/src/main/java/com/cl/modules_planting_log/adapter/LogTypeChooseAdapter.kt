package com.cl.modules_planting_log.adapter

import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.databinding.PlantingPlantLogTypeChooseItemBinding
import com.cl.modules_planting_log.request.LogTypeListDataItem

class LogTypeChooseAdapter (data: MutableList<LogTypeListDataItem>?) :
    BaseQuickAdapter<LogTypeListDataItem, BaseDataBindingHolder<PlantingPlantLogTypeChooseItemBinding>>(R.layout.planting_plant_log_type_choose_item, data) {
    override fun convert(holder: BaseDataBindingHolder<PlantingPlantLogTypeChooseItemBinding>, item: LogTypeListDataItem) {
        holder.getView<CheckBox>(R.id.check_period_chooser).apply {
            isChecked = item.isSelected
            text = item.showUiText
            if (item.isSelected) {
                setTextColor(resources.getColor(com.cl.common_base.R.color.white))
            }  else {
                setTextColor(resources.getColor(com.cl.common_base.R.color.mainColor))
            }
        }
    }
}