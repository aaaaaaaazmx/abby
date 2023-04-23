package com.cl.modules_contact.adapter

import android.text.SpannedString
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.DateHelper
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemNotificationBinding
import com.cl.modules_contact.response.MessageListData
import com.cl.modules_contact.response.NewPageData

class ContactNotificationAdapter(data: MutableList<MessageListData>?) :
    BaseQuickAdapter<MessageListData, BaseDataBindingHolder<ContactItemNotificationBinding>>(R.layout.contact_item_notification, data) {

    override fun convert(holder: BaseDataBindingHolder<ContactItemNotificationBinding>, item: MessageListData) {
        holder.dataBinding?.apply {
            data = item
            adapter = this@ContactNotificationAdapter
            executePendingBindings()
        }

        // 设置富文本
        holder.getView<TextView>(R.id.tv_comment).apply {
            getContents(item.content, item.contentBlod).let {
                text = it
            }
        }

        // 设置时间
        holder.getView<TextView>(R.id.tV_time).apply {
            text = convertTime(item.createTime)
        }
    }

    private fun convertTime(createTime: String? = null): String {
        var text = ""
        createTime?.let {
            // 2023-04-20 10:04:52
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").apply {
                text = DateHelper.convert((this)).toString()
            }
        }
        return text
    }

    private fun getContents(content: String?, mentions: MutableList<String>?): SpannedString {
        var contents = content ?: ""
        mentions?.forEach {
            it.let { nickName ->
                contents = contents.replace(nickName, "").trim()
            }
        }

        return buildSpannedString {
            color(context.getColor(com.cl.common_base.R.color.black)) {
                bold {
                    mentions?.forEach {
                        append("$it ")
                    }
                }
            }
            append(contents)
        }

    }

}
