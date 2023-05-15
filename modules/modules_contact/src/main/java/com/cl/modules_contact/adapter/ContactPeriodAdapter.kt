package com.cl.modules_contact.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemPeriodBinding
import com.cl.modules_contact.response.ContactPeriodBean

class ContactPeriodAdapter (data: MutableList<ContactPeriodBean>?) :
    BaseQuickAdapter<ContactPeriodBean, BaseDataBindingHolder<ContactItemPeriodBinding>>(R.layout.contact_item_period, data) {
    override fun convert(holder: BaseDataBindingHolder<ContactItemPeriodBinding>, item: ContactPeriodBean) {
        holder.getView<CheckBox>(R.id.check_period).apply {
            isChecked = item.isSelected
            text = item.period
            if (item.isSelected) {
                setTextColor(resources.getColor(com.cl.common_base.R.color.mainColor))
            }  else {
                setTextColor(resources.getColor(com.cl.common_base.R.color.black))
            }
        }
    }
}