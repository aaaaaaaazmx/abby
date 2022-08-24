package com.cl.common_base.adapter

import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.databinding.BaseItemPumpWaterBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 排水适配器
 * @author 李志军 2022-08-10 15:32
 */
class PumpWaterAdapter(data: MutableList<AdvertisingData>?) :
    BaseQuickAdapter<AdvertisingData, BaseViewHolder>(R.layout.base_item_pump_water, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<BaseItemPumpWaterBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: AdvertisingData) {
        // 获取 Binding
        val binding: BaseItemPumpWaterBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}
