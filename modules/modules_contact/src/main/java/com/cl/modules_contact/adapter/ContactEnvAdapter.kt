package com.cl.modules_contact.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemContactEnvBinding
import com.cl.modules_contact.databinding.ItemCircleBinding
import com.cl.modules_contact.request.ContactEnvData
import com.cl.modules_contact.response.NewPageData

class ContactEnvAdapter (data: MutableList<ContactEnvData>?) :
    BaseQuickAdapter<ContactEnvData, BaseDataBindingHolder<ContactItemContactEnvBinding>>(R.layout.contact_item_contact_env, data) {
    override fun convert(holder: BaseDataBindingHolder<ContactItemContactEnvBinding>, item: ContactEnvData) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }
    }
}