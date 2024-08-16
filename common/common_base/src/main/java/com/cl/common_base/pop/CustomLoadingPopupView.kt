package com.cl.common_base.pop

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.widget.TextView
import com.cl.common_base.R
import com.cl.common_base.ext.dp2px
import com.lxj.xpopup.impl.LoadingPopupView

class CustomLoadingPopupView : LoadingPopupView {
    constructor(context: Context, bindLayoutId: Int) : super(context, R.layout.loading_pop)

    override fun onCreate() {
        super.onCreate()
        // 修改背景颜色或背景图片
        popupImplView.setBackgroundColor(Color.TRANSPARENT) // 修改背景颜色
        // 或者设置自定义背景图片
        // popupImplView.setBackgroundResource(R.drawable.custom_background)
    }

    override fun setTitle(title: CharSequence?): LoadingPopupView {
        return super.setTitle(title)
    }
}