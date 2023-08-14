package com.cl.modules_planting_log.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.databinding.PlantingCardItemBinding
import com.cl.modules_planting_log.request.CardInfo

class PlantLogCardAdapter(data: MutableList<CardInfo>?) :
    BaseQuickAdapter<CardInfo, BaseDataBindingHolder<PlantingCardItemBinding>>(R.layout.planting_card_item, data) {
    override fun convert(holder: BaseDataBindingHolder<PlantingCardItemBinding>, item: CardInfo) {
        holder.dataBinding?.apply {
            adapter = this@PlantLogCardAdapter
            bean = item
            executePendingBindings()
        }
    }
}