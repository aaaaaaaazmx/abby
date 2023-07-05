package com.cl.modules_login.adapter

import androidx.databinding.DataBindingUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.ItemSelectCountryBinding
import com.cl.modules_login.response.CountData
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder


/**
 * 国家列表选择
 */
class SelectCountryAdapter(data: MutableList<CountData>?) :
    BaseQuickAdapter<CountData, BaseViewHolder>(R.layout.item_select_country, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemSelectCountryBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: CountData) {
        // 获取 Binding
        val binding: ItemSelectCountryBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}