package com.cl.modules_contact.adapter

import android.content.Intent
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.web.WebActivity
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemContactNewEnvBinding
import com.cl.modules_contact.response.NewPageData

class ContactNewEnvAdapter (data: MutableList<NewPageData.Records.AccessorysList>?) :
    BaseQuickAdapter<NewPageData.Records.AccessorysList, BaseDataBindingHolder<ContactItemContactNewEnvBinding>>(R.layout.contact_item_contact_new_env, data) {
    override fun convert(holder: BaseDataBindingHolder<ContactItemContactNewEnvBinding>, item: NewPageData.Records.AccessorysList) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()

            tvLearn.setSafeOnClickListener {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, item.buyLink)
                intent.putExtra(WebActivity.KEY_IS_SHOW_CAR, true)
                context.startActivity(intent)
            }
        }
    }
}