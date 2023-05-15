package com.cl.modules_contact.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemEmojiBinding
import com.cl.modules_contact.databinding.ContactItemTagsBinding
import com.cl.modules_contact.response.TagsBean

class TagsAdapter (data: MutableList<TagsBean>?) :
    BaseQuickAdapter<TagsBean, BaseDataBindingHolder<ContactItemTagsBinding>>(R.layout.contact_item_tags, data) {
    override fun convert(holder: BaseDataBindingHolder<ContactItemTagsBinding>, item: TagsBean) {
        holder.getView<CheckBox>(R.id.check_tag).apply {
            isChecked = item.isSelected
            text = item.number
            if (item.isSelected) {
                setTextColor(resources.getColor(com.cl.common_base.R.color.white))
            }  else {
                setTextColor(resources.getColor(com.cl.common_base.R.color.mainColor))
            }
        }
    }
}