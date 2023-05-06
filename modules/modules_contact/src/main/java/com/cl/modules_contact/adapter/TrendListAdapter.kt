package com.cl.modules_contact.adapter

import android.text.SpannedString
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.ext.DateHelper
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ItemCircleBinding
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.widget.NineGridView
import com.cl.modules_contact.widget.nineview.GlideNineGridImageLoader
import com.cl.modules_contact.widget.nineview.NineGridImageView
import com.cl.modules_contact.widget.nineview.OnImageItemClickListener
import kotlin.concurrent.thread


/**
 * 朋友圈首页列表适配器
 */
class TrendListAdapter(
    data: MutableList<NewPageData.Records>?,
    private val onImageItemClickListener: OnImageItemClickListener
) :
    BaseQuickAdapter<NewPageData.Records, BaseDataBindingHolder<ItemCircleBinding>>(R.layout.item_circle, data) {

    override fun convert(holder: BaseDataBindingHolder<ItemCircleBinding>, item: NewPageData.Records) {
        // 获取 Binding
        val binding: ItemCircleBinding? = holder.dataBinding
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
        // 设置富文本
        holder.setText(R.id.tvDesc, getContents(item.content, item.mentions))
        holder.setText(R.id.tvNum, convertTime(item.createTime))

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDataBindingHolder<ItemCircleBinding> {
        return super.onCreateViewHolder(parent, viewType).apply {
            getView<NineGridImageView>(R.id.nineGridView).apply {
                imageLoader = GlideNineGridImageLoader()
                onImageItemClickListener = this@TrendListAdapter.onImageItemClickListener
            }
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

    /*override fun getItemId(position: Int): Long {
        return position.toLong()
    }*/
}