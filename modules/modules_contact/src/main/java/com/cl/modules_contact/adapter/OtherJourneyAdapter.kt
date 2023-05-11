package com.cl.modules_contact.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.DateHelper
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemOtherJourneyBinding
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.widget.nineview.GlideNineGridImageLoader
import com.cl.modules_contact.widget.nineview.NineGridImageView
import com.cl.modules_contact.widget.nineview.OnImageItemClickListener
import java.util.Locale

class OtherJourneyAdapter(
    data: MutableList<NewPageData.Records>?,
    private val onImageItemClickListener: OnImageItemClickListener
) :
    BaseQuickAdapter<NewPageData.Records, BaseDataBindingHolder<ContactItemOtherJourneyBinding>>(R.layout.contact_item_other_journey, data) {

    override fun convert(holder: BaseDataBindingHolder<ContactItemOtherJourneyBinding>, item: NewPageData.Records) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDataBindingHolder<ContactItemOtherJourneyBinding> {
        return super.onCreateViewHolder(parent, viewType).apply {
            getView<NineGridImageView>(R.id.nineGridView).apply {
                imageLoader = GlideNineGridImageLoader()
                onImageItemClickListener = this@OtherJourneyAdapter.onImageItemClickListener
            }
        }
    }


    /**
     * 获取内容
     */
    private fun getContents(content: String?, mentions: MutableList<NewPageData.Records.Mentions>?): SpannableString {
        content?.let {
            val spannableString = SpannableString(content)
            mentions?.forEach {
                it.nickName?.let { nickName ->
                    val colorSpan = ForegroundColorSpan(Color.parseColor("#006241"))
                    val startIndex = spannableString.indexOf(nickName)
                    var endIndex = spannableString.indexOf(" ", startIndex)
                    if (endIndex == -1) {
                        endIndex = spannableString.length
                    }
                    spannableString.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            return spannableString
        } ?: return SpannableString("")
    }
}