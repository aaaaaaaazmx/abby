package com.cl.modules_contact.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemContactEnvBinding
import com.cl.modules_contact.databinding.ContactItemSerachBinding
import com.cl.modules_contact.response.MentionData

class ContactListAdapter(data: MutableList<MentionData>?) :
    BaseQuickAdapter<MentionData, BaseDataBindingHolder<ContactItemSerachBinding>>(R.layout.contact_item_serach, data) {

    override fun convert(holder: BaseDataBindingHolder<ContactItemSerachBinding>, item: MentionData) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }
    }
}