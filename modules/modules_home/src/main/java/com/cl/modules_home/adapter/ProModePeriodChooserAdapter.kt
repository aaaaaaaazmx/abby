package com.cl.modules_home.adapter

import android.annotation.SuppressLint
import com.cl.modules_home.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.modules_home.databinding.HomeProModePeriodItemBinding
import com.cl.modules_home.request.CycleListBean

class ProModePeriodChooserAdapter(data: MutableList<CycleListBean>?) :
    BaseQuickAdapter<CycleListBean, BaseDataBindingHolder<HomeProModePeriodItemBinding>>(R.layout.home_pro_mode_period_item, data) {


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun convert(holder: BaseDataBindingHolder<HomeProModePeriodItemBinding>, item: CycleListBean) {
        holder.dataBinding?.apply {
            executePendingBindings()

            container.background = if (item.isSelect) context.getDrawable(com.cl.common_base.R.drawable.background_button_main_color_r100) else context.getDrawable(com.cl.common_base.R.drawable.background_button_white_r100)
            textView.text = item.stepShow
            textView.setTextColor(if(item.isSelect) context.getColor(com.cl.common_base.R.color.white) else context.getColor(com.cl.common_base.R.color.mainColor))
            // 还有个灰色状态。
        }
    }
}