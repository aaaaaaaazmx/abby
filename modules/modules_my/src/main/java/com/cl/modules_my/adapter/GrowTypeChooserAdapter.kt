package com.cl.modules_my.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyGrowAddTypeItemBinding
import com.cl.modules_my.request.GrowTypeListDataItem

class GrowTypeChooserAdapter(data: MutableList<GrowTypeListDataItem>?) :
    BaseQuickAdapter<GrowTypeListDataItem, BaseDataBindingHolder<MyGrowAddTypeItemBinding>>(
        R.layout.my_grow_add_type_item,
        data
    ) {
    override fun convert(
        holder: BaseDataBindingHolder<MyGrowAddTypeItemBinding>,
        item: GrowTypeListDataItem
    ) {
        holder.getView<CheckBox>(R.id.check_period_chooser).apply {
            isChecked = item.isSelected
            text = item.showUiText
            if (item.isSelected) {
                setTextColor(resources.getColor(com.cl.common_base.R.color.white))
            } else {
                setTextColor(resources.getColor(com.cl.common_base.R.color.mainColor))
            }
        }
    }
}