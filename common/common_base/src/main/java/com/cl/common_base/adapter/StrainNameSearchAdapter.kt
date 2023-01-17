package com.cl.common_base.adapter

import android.content.Intent
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseItemStrainSearchBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 搜索StrainName适配器
 * @author 李志军 2022-08-10 15:32
 */
class StrainNameSearchAdapter(data: MutableList<String>?) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.base_item_strain_search, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<BaseItemStrainSearchBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: String) {
        // 获取 Binding
        val binding: BaseItemStrainSearchBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}
