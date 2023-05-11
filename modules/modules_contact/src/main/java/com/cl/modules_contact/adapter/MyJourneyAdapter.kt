package com.cl.modules_contact.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.LifecycleOwner
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
import com.youth.banner.Banner
import com.youth.banner.indicator.CircleIndicator
import java.util.Locale

class MyJourneyAdapter(
    data: MutableList<NewPageData.Records>?
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

        // 轮播图
        holder.getView<Banner<String, ImageAdapter>>(R.id.banner).apply {
            addBannerLifecycleObserver(context as? LifecycleOwner)
            isAutoLoop(false)
            setBannerRound(20f)
            indicator = CircleIndicator(context)
            val urlList = mutableListOf<String>()
            item.imageUrls?.let {
                // 手动添加图片集合
                it.forEach { data -> data.imageUrl?.let { it1 -> urlList.add(it1) } }
            }
            setAdapter(ImageAdapter(urlList, context))
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