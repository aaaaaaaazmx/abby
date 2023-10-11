package com.cl.modules_home.adapter

import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeJoinItemBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_home.request.GrowSpaceData

class FirstJoinAdapter(data: MutableList<GrowSpaceData>?) :
    BaseQuickAdapter<GrowSpaceData, BaseViewHolder>(R.layout.home_join_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<HomeJoinItemBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: GrowSpaceData) {
        // 获取 Binding
        val binding: HomeJoinItemBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }

        helper.getView<ImageView>(R.id.grow_res).apply {
            when(item.back) {
                1 -> setImageResource(R.mipmap.home_grow_box)
                2 -> setImageResource(R.mipmap.home_grow_tent)
            }
        }
    }
}