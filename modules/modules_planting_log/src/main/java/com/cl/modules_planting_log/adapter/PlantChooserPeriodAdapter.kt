package com.cl.modules_planting_log.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.databinding.PlantingChooserItemPeriodBinding
import com.cl.modules_planting_log.request.PeriodVo

class PlantChooserPeriodAdapter(data: MutableList<PeriodVo>?) :
    BaseQuickAdapter<PeriodVo, BaseDataBindingHolder<PlantingChooserItemPeriodBinding>>(R.layout.planting_chooser_item_period, data) {
    override fun convert(holder: BaseDataBindingHolder<PlantingChooserItemPeriodBinding>, item: PeriodVo) {
        holder.dataBinding?.apply {
            adapter = this@PlantChooserPeriodAdapter
            bean = item
            executePendingBindings()
        }
    }
}