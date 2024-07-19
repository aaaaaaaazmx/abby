package com.cl.modules_contact.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.dp2px
import com.cl.common_base.util.ViewUtils
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ItemCircleBinding
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.widget.nineview.GlideNineGridImageLoader
import com.cl.modules_contact.widget.nineview.NineGridImageView
import com.cl.modules_contact.widget.nineview.OnImageItemClickListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.indicator.RoundLinesIndicator
import kotlin.concurrent.thread


/**
 * 朋友圈首页列表适配器
 */
class TrendListAdapter(
    data: MutableList<NewPageData.Records>?,
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
        ViewUtils.setVisible(!item.content.isNullOrEmpty() || !item.mentions.isNullOrEmpty(), holder.getView(R.id.tvDesc))
        holder.setText(R.id.tvDesc, getContents(item.content, item.mentions))
        holder.setText(R.id.tvNum, convertTime(item.createTime))
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


        // 动态更改宽高 iv_head_bg
        val layoutParams = binding?.ivHeadBg?.layoutParams
        layoutParams?.height = dp2px(if (item.framesHeads.isNullOrEmpty()) 40f else 60f)
        layoutParams?.width = dp2px(if (item.framesHeads.isNullOrEmpty()) 60f else 60f)
        binding?.ivHeadBg?.layoutParams = layoutParams
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

    /*override fun getItemId(position: Int): Long {
        return position.toLong()
    }*/
}