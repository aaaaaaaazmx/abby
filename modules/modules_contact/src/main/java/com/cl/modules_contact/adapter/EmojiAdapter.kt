package com.cl.modules_contact.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemContactEnvBinding
import com.cl.modules_contact.databinding.ContactItemEmojiBinding
import com.cl.modules_contact.request.ContactEnvData

class EmojiAdapter(data: MutableList<String>?) :
    BaseQuickAdapter<String, BaseDataBindingHolder<ContactItemEmojiBinding>>(R.layout.contact_item_emoji, data) {

    override fun convert(holder: BaseDataBindingHolder<ContactItemEmojiBinding>, item: String) {
        holder.dataBinding?.apply {
            executePendingBindings()
        }

        holder.setText(R.id.face_item_emoji, item)
    }
}