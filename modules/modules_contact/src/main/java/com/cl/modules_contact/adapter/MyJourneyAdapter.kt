package com.cl.modules_contact.adapter

import android.text.SpannedString
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemMyJourneyBinding
import com.cl.modules_contact.databinding.ItemCircleBinding
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.widget.nineview.GlideNineGridImageLoader
import com.cl.modules_contact.widget.nineview.NineGridImageView
import com.cl.modules_contact.widget.nineview.OnImageItemClickListener
import java.util.Locale

class MyJourneyAdapter(
    data: MutableList<NewPageData.Records>?,
    private val onImageItemClickListener: OnImageItemClickListener
) :
    BaseQuickAdapter<NewPageData.Records, BaseDataBindingHolder<ContactItemMyJourneyBinding>>(R.layout.contact_item_my_journey, data) {

    override fun convert(holder: BaseDataBindingHolder<ContactItemMyJourneyBinding>, item: NewPageData.Records) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }
        // 设置富文本
        holder.setText(R.id.tvDesc, getContents(item.content, item.mentions))
        holder.setText(R.id.tv_time, item.createTime?.let { formatTime(it) })
        holder.setText(R.id.tv_date, item.createTime?.let { formatDate(it) })

        // 九宫格
        holder.getView<NineGridImageView>(R.id.nineGridView).apply {
            item.imageUrls?.let {
                // 手动添加图片集合
                val urlList = mutableListOf<String>()
                it.forEach { data -> data.imageUrl?.let { it1 -> urlList.add(it1) } }
                externalPosition = holder.bindingAdapterPosition
                setUrlList(urlList)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDataBindingHolder<ContactItemMyJourneyBinding> {
        return super.onCreateViewHolder(parent, viewType).apply {
            getView<NineGridImageView>(R.id.nineGridView).apply {
                imageLoader = GlideNineGridImageLoader()
                onImageItemClickListener = this@MyJourneyAdapter.onImageItemClickListener
            }
        }
    }

    fun formatTime(time: String): String {
        var text = ""
        time.let {
            // 2023-05-05 17:35:59
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").apply {
                text = DateHelper.formatTime(this, "HH:mm")
            }
        }
        return text
    }

    fun formatDate(time: String): String {
        var text = ""
        time.let {
            // 2023-04-20 10:04:52
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").apply {
                text = DateHelper.formatTime(this, "ddMMM", Locale.US)
            }
        }
        return text
    }

    /**
     * 获取内容
     */
    private fun getContents(content: String?, mentions: MutableList<NewPageData.Records.Mentions>?): SpannedString {
        var contents = content ?: ""
        mentions?.forEach {
            it.nickName?.let { nickName ->
                contents = contents.replace("$nickName", "").trim()
            }
        }

        return buildSpannedString {
            color(context.getColor(com.cl.common_base.R.color.mainColor)) {
                mentions?.forEach {
                    append("${it.nickName} ")
                }
            }
            append(contents)
        }

    }
}