package com.cl.modules_home.adapter

import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeItemEditPopBinding
import com.bbgo.module_home.databinding.HomeItemPopBinding
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.ext.logI
import com.cl.modules_home.response.GuideInfoData

/**
 * 种植周期，多type布局
 */
class PlantInitMultiplePopAdapter(data: MutableList<GuideInfoData.PlantInfo>?) :
    BaseMultiItemQuickAdapter<GuideInfoData.PlantInfo, BaseViewHolder>(data) {

    init {
        addItemType(GuideInfoData.VALUE_STATUS_NORMAL, R.layout.home_item_pop)
        addItemType(GuideInfoData.VALUE_STATUS_DRYING, R.layout.home_item_edit_pop)
        addItemType(GuideInfoData.VALUE_STATUS_CURING, R.layout.home_item_edit_pop)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            GuideInfoData.VALUE_STATUS_NORMAL -> {
                val binding = DataBindingUtil.bind<HomeItemPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            GuideInfoData.VALUE_STATUS_DRYING -> {
                val binding = DataBindingUtil.bind<HomeItemEditPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            GuideInfoData.VALUE_STATUS_CURING -> {
                val binding = DataBindingUtil.bind<HomeItemEditPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: GuideInfoData.PlantInfo) {
        // 获取 Binding
        when (item.isCurrentStatus) {
            GuideInfoData.VALUE_STATUS_NORMAL -> {

            }
            GuideInfoData.VALUE_STATUS_DRYING -> {
                val binding: HomeItemEditPopBinding? = DataBindingUtil.getBinding(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = item
                    binding.executePendingBindings()
                }
            }
            GuideInfoData.VALUE_STATUS_CURING -> {
                val binding: HomeItemEditPopBinding? = DataBindingUtil.getBinding(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = item
                    binding.executePendingBindings()
                }
            }
        }
    }
}