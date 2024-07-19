package com.cl.modules_home.adapter

import androidx.databinding.DataBindingUtil
import com.cl.common_base.bean.AcademyListData
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeAcademyItemPopBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 学院一级列表
 * @author 李志军 2022-08-06 18:44
 */
class HomeAcademyPopAdapter(data: MutableList<AcademyListData>?) :
    BaseQuickAdapter<AcademyListData, BaseViewHolder>(R.layout.home_academy_item_pop, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<HomeAcademyItemPopBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: AcademyListData) {
        // 获取 Binding
        val binding: HomeAcademyItemPopBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}