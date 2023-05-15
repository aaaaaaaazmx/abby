package com.cl.common_base.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.bean.RewardBean
import com.cl.common_base.databinding.ItemReawrdBinding

/**
 * 打赏适配器
 */
class RewardAdapter(data: MutableList<RewardBean>?) :
    BaseQuickAdapter<RewardBean, BaseDataBindingHolder<ItemReawrdBinding>>(R.layout.item_reawrd, data) {
    override fun convert(holder: BaseDataBindingHolder<ItemReawrdBinding>, item: RewardBean) {
        holder.getView<CheckBox>(R.id.check_reward).apply {
            isChecked = item.isSelected
            text = item.number
            if (item.isSelected) {
                setTextColor(resources.getColor(R.color.white))
            }  else {
                setTextColor(resources.getColor(R.color.mainColor))
            }
        }
    }
}