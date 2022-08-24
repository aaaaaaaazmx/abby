package com.cl.modules_home.adapter

import androidx.databinding.DataBindingUtil
import com.cl.modules_home.response.GuideInfoData
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeItemPopBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * pop初始化数据
 *
 * @author 李志军 2022-08-06 18:44
 */
class PlantInitPopAdapter(data: MutableList<GuideInfoData.PlantInfo>?) :
    BaseQuickAdapter<GuideInfoData.PlantInfo, BaseViewHolder>(R.layout.home_item_pop, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<HomeItemPopBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: GuideInfoData.PlantInfo) {
        // 获取 Binding
        val binding: HomeItemPopBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}
