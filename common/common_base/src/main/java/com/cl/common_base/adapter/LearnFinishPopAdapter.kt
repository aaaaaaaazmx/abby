package com.cl.common_base.adapter

import android.text.style.UnderlineSpan
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.text.underline
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.R
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.databinding.HomeFinishGuideItemBinding
import com.cl.common_base.databinding.HomeFinishGuideTextItemBinding
import com.cl.common_base.databinding.HomeFinishGuideUrlItemBinding
import com.cl.common_base.util.span.appendClickable

/**
 * 通用图文界面
 *
 * @author 李志军 2022-08-06 18:44
 */
class LearnFinishPopAdapter(data: MutableList<DetailByLearnMoreIdData.ItemBean>?) :
    BaseMultiItemQuickAdapter<DetailByLearnMoreIdData.ItemBean, BaseViewHolder>(data) {

    init {
        addItemType(DetailByLearnMoreIdData.KEY_TEXT_TYPE, R.layout.home_finish_guide_text_item)
        addItemType(DetailByLearnMoreIdData.KEY_IMAGE_TYPE, R.layout.home_finish_guide_item)
        addItemType(DetailByLearnMoreIdData.KEY_URL_TYPE, R.layout.home_finish_guide_url_item)
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
        when (holder.itemViewType) {
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
            DetailByLearnMoreIdData.KEY_URL_TYPE -> {
                DataBindingUtil.bind<HomeFinishGuideUrlItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
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

        when (helper.itemViewType) {
            DetailByLearnMoreIdData.KEY_URL_TYPE -> {
                val tvHtml = helper.itemView.findViewById<TextView>(R.id.tv_html)
                tvHtml.text = buildSpannedString {
                    color(context.getColor(R.color.mainColor)) {
                        underline { appendLine(item.extend?.title) }
                    }
                }
            }
        }

    }
}
