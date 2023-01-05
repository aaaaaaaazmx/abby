package com.cl.modules_home.adapter

import androidx.databinding.DataBindingUtil
import com.cl.common_base.bean.AcademyDetails
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeAcademyDetailItemPopBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 学院一级列表
 * @author 李志军 2022-08-06 18:44
 */
class HomeAcademyDetailAdapter(data: MutableList<AcademyDetails>?) :
    BaseQuickAdapter<AcademyDetails, BaseViewHolder>(R.layout.home_academy_detail_item_pop, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<HomeAcademyDetailItemPopBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: AcademyDetails) {
        // 获取 Binding
        val binding: HomeAcademyDetailItemPopBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}
