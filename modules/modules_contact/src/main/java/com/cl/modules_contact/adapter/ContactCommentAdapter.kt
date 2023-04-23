package com.cl.modules_contact.adapter

import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.text.SpannedString
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.DateHelper
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemCommentBinding
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.response.NewPageData

/**
 * 评论适配器
 */
class ContactCommentAdapter(data: MutableList<CommentByMomentData>?) :
    BaseQuickAdapter<CommentByMomentData, BaseDataBindingHolder<ContactItemCommentBinding>>(R.layout.contact_item_comment, data) {

    override fun convert(holder: BaseDataBindingHolder<ContactItemCommentBinding>, item: CommentByMomentData) {
        holder.dataBinding?.apply {
            data = item
            adapter = this@ContactCommentAdapter
            lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
            executePendingBindings()
        }



        // 设置富文本
        holder.setText(R.id.tvDesc, getContents(item.commentName, item.parentComentId, item.comment, holder.layoutPosition))
        holder.setText(R.id.tv_create_time, convertTime(item.createTime))


        // 设置回复的适配器
        holder.getView<RecyclerView>(R.id.rv_reply).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ContactReplyAdapter(item.replys)
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

    /**
     * 获取内容
     * @param commentName 回复者的名字
     * @param parentComentId 查看是否是有在上一个回复中回复的
     * @param comment 回复的内容
     */
    private fun getContents(commentName: String?, parentComentId: String?, comment: String?, position: Int?): SpannedString {
        /*parentComentId?.let {
            buildSpannedString {
                bold { append(commentName) }
                color(context.getColor(com.cl.common_base.R.color.mainColor)) {
                    // 添加上一级的回复者名字
                    append()
                }

            }
        } ?: run {
            buildSpannedString {
                bold { append(commentName) }
                append(comment)
            }
        }*/

        return  buildSpannedString {
            bold { append(commentName) }
            append(" $comment")
        }
    }
}