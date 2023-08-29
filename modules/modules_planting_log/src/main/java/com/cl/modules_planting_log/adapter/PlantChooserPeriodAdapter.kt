package com.cl.modules_planting_log.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.databinding.PlantingChooserItemPeriodBinding
import com.cl.modules_planting_log.request.PeriodVo
import java.math.BigDecimal

class PlantChooserPeriodAdapter(data: MutableList<PeriodVo>?) :
    BaseQuickAdapter<PeriodVo, BaseDataBindingHolder<PlantingChooserItemPeriodBinding>>(R.layout.planting_chooser_item_period, data) {
    override fun convert(holder: BaseDataBindingHolder<PlantingChooserItemPeriodBinding>, item: PeriodVo) {
        holder.dataBinding?.apply {
            adapter = this@PlantChooserPeriodAdapter
            bean = item
            executePendingBindings()
        }
    }

    fun formatTime(startTime: String?): String {
        return if (startTime == BigDecimal.ZERO.toString()) "" else DateHelper.formatTime(startTime?.toLongOrNull() ?: 0L, "MM/dd/yy")
    }
}