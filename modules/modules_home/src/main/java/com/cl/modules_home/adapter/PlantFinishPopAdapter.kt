package com.cl.modules_home.adapter

import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeFinishGuideItemBinding
import com.bbgo.module_home.databinding.HomeFinishGuideTextItemBinding
import com.bbgo.module_home.databinding.HomeItemPopBinding
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_home.response.DetailByLearnMoreIdData
import com.cl.modules_home.response.GuideInfoData

/**
 * 种植完成通用图文界面
 *
 * @author 李志军 2022-08-06 18:44
 */
class PlantFinishPopAdapter(data: MutableList<DetailByLearnMoreIdData.ItemBean>?) :
    BaseMultiItemQuickAdapter<DetailByLearnMoreIdData.ItemBean, BaseViewHolder>(data){

    init {
        addItemType(DetailByLearnMoreIdData.KEY_TEXT_TYPE, R.layout.home_finish_guide_text_item)
        addItemType(DetailByLearnMoreIdData.KEY_IMAGE_TYPE, R.layout.home_finish_guide_item)
    }

//    override fun onItemViewHolderCreated(holder: BaseViewHolder, viewType: Int) {
//        when(viewType) {
//            DetailByLearnMoreIdData.KEY_TEXT_TYPE -> {
//                val binding = DataBindingUtil.bind<HomeFinishGuideTextItemBinding>(holder.itemView)
//                if (binding != null) {
//                    // 设置数据
//                    binding.data = data[holder.layoutPosition]
//                    binding.executePendingBindings()
//                }
//            }
//            DetailByLearnMoreIdData.KEY_IMAGE_TYPE -> {
//                val binding = DataBindingUtil.bind<HomeFinishGuideItemBinding>(holder.itemView)
//                if (binding != null) {
//                    // 设置数据
//                    binding.data = data[holder.layoutPosition]
//                    binding.executePendingBindings()
//                }
//            }
//        }
//    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when(holder.itemViewType) {
            DetailByLearnMoreIdData.KEY_TEXT_TYPE -> {
                val binding = DataBindingUtil.bind<HomeFinishGuideTextItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            DetailByLearnMoreIdData.KEY_IMAGE_TYPE -> {
                val binding = DataBindingUtil.bind<HomeFinishGuideItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun convert(helper: BaseViewHolder, item: DetailByLearnMoreIdData.ItemBean) {
        // 获取 Binding
//        val binding: HomeFinishGuideItemBinding? = helper.getBinding()
//        if (binding != null) {
//            binding.data = item
//            binding.executePendingBindings()
//        }
    }
}
