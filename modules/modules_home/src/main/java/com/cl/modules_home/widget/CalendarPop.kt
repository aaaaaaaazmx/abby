package com.cl.modules_home.widget

import android.content.Context
import android.text.style.ImageSpan
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.DataBindingUtil
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.span.append
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeCalendarPopBinding
import com.lxj.xpopup.core.CenterPopupView

class CalendarPop(
    context: Context, private val onConfirmAction: (() -> Unit)? = null,
    private val content: String? = null,
    private val onCancelAction: (() -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_calendar_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeCalendarPopBinding>(popupImplView)?.apply {
            executePendingBindings()

            tvContent.text = buildSpannedString {
                append(content)
                append(R.mipmap.home_calendar_bg)
            }

            tvCancel.setSafeOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }

            tvConfirm.setSafeOnClickListener {
                onConfirmAction?.invoke()
                dismiss()
            }
        }
    }
}