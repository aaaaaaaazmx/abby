package com.cl.modules_contact.adapter

import android.text.SpannedString
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemCommentBinding
import com.cl.modules_contact.databinding.ContactItemCommentReplyBinding
import com.cl.modules_contact.response.CommentByMomentData

class ContactReplyAdapter(data: MutableList<CommentByMomentData.Replys>?) :
    BaseQuickAdapter<CommentByMomentData.Replys, BaseDataBindingHolder<ContactItemCommentReplyBinding>>(R.layout.contact_item_comment_reply, data) {

    override fun convert(holder: BaseDataBindingHolder<ContactItemCommentReplyBinding>, item: CommentByMomentData.Replys) {
        holder.dataBinding?.apply {
            data = item
            adapter = this@ContactReplyAdapter
            lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
            executePendingBindings()
        }


        // 设置富文本
        holder.setText(R.id.tvDesc, getContents(item.commentName, item.parentReplyId, item.comment, holder.layoutPosition))
        holder.setText(R.id.tv_reply_time, convertTime(item.createTime))
    }

    private fun convertTime(createTime: String? = null): String {
        return createTime?.let {
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").let {
                logI("1231231: $createTime ,,, ${DateHelper.convert((it)).toString()}")
                DateHelper.convert((it)).toString()
            }.toString()
        } ?: ""
    }

    /**
     * 获取内容
     * @param commentName 回复者的名字
     * @param parentComentId 查看是否是有在上一个回复中回复的
     * @param comment 回复的内容
     */
    private fun getContents(commentName: String?, parentComentId: String?, comment: String?, position: Int?): SpannedString {
        return parentComentId?.let {
            buildSpannedString {
                bold { append(commentName) }
                color(context.getColor(com.cl.common_base.R.color.mainColor)) {
                    // 添加上一级的回复者名字
                    data.firstOrNull { it.replyId == parentComentId }?.let {
                        append(" @${it.commentName}")
                    }
                }
                append(" $comment")
            }
        } ?: run {
            buildSpannedString {
                bold { append(commentName) }
                append(" $comment")
            }
        }
    }
}