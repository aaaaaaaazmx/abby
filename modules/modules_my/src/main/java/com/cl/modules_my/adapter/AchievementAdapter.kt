package com.cl.modules_my.adapter

import android.annotation.SuppressLint
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyAchievementItemBinding
import com.cl.modules_my.request.AchievementBean
import com.cl.modules_my.widget.GrayscaleTransformation

class AchievementAdapter(data: MutableList<AchievementBean>?, val isAchievement: Boolean) :
    BaseQuickAdapter<AchievementBean, BaseDataBindingHolder<MyAchievementItemBinding>>(R.layout.my_achievement_item, data) {

    @SuppressLint("CheckResult")
    override fun convert(holder: BaseDataBindingHolder<MyAchievementItemBinding>, item: AchievementBean) {
        holder.dataBinding?.apply {
            isAchievement = this@AchievementAdapter.isAchievement
            info = item
            executePendingBindings()


            val requestOptions = RequestOptions()
            // 判断是否需要应用灰度转换
            if (!item.isGain) {
                requestOptions.transform(GrayscaleTransformation())
            }
            // 设置 placeholder 和 error
            requestOptions.placeholder(com.cl.common_base.R.mipmap.placeholder)
            requestOptions.error(com.cl.common_base.R.mipmap.errorholder)

            // 加载图片收获原色，不收获灰度
            Glide.with(context)
                .load(item.picture)
                .apply(requestOptions)
                .into(ivAchievement)

            // 选中和未选中
            if (item.selectedStatus) {
                flRoot.setBackgroundResource(com.cl.common_base.R.drawable.background_achievement_border)
            } else {
                // 不设置背景
                flRoot.background = null
            }
        }
    }
}