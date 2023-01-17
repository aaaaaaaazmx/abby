package com.cl.common_base.pop

import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseCustomBubbleAttachPopupBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class CustomBubbleAttachPopup(
    context: Context,
    private var easeNumber: Int? = null, // 婚讯啊未读数量
    private val bubbleClickAction: (() -> Unit)? = null,
    private var calendarNumber: Int? = null,
    private var envNumber: Int? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_custom_bubble_attach_popup
    }

    /**
     * 学院消息
     */
    fun setCalendarNumbers(calendarNumber: Int) {
        this.calendarNumber = calendarNumber
        mBinding?.tvCalendarNumber?.text = "$calendarNumber"
        mBinding?.tvCalendarNumber?.visibility = if ((calendarNumber ?: 0) > 0) View.VISIBLE else View.GONE
        mBinding?.ivCalendar?.visibility = if ((calendarNumber ?: 0) > 0) View.VISIBLE else View.GONE
    }

    /**
     * 环信消息
     */
    fun setEaseNumber(easeNumber: Int) {
        this.easeNumber = easeNumber
        mBinding?.tvSupportNumber?.text = "$easeNumber"
        mBinding?.tvSupportNumber?.visibility = if ((easeNumber ?: 0) > 0) View.VISIBLE else View.GONE
        mBinding?.ivSupport?.visibility = if ((easeNumber ?: 0) > 0) View.VISIBLE else View.GONE
    }

    /**
     * 环境消息
     */
    fun setEnvNumber(envNumber: Int) {
        this.envNumber = envNumber
        mBinding?.tvEnvNumber?.text = "$envNumber"
        mBinding?.tvEnvNumber?.visibility = if ((envNumber) > 0) View.VISIBLE else View.GONE
        mBinding?.ivEnv?.visibility = if (envNumber > 0) View.VISIBLE else View.GONE
    }

    override fun beforeShow() {
        super.beforeShow()
        mBinding?.tvCalendarNumber?.text = "$calendarNumber"
        mBinding?.tvCalendarNumber?.visibility = if ((calendarNumber ?: 0) > 0) View.VISIBLE else View.GONE
        mBinding?.ivCalendar?.visibility = if ((calendarNumber ?: 0) > 0) View.VISIBLE else View.GONE

        mBinding?.tvSupportNumber?.text = "$easeNumber"
        mBinding?.tvSupportNumber?.visibility = if ((easeNumber ?: 0) > 0) View.VISIBLE else View.GONE
        mBinding?.ivSupport?.visibility = if ((easeNumber ?: 0) > 0) View.VISIBLE else View.GONE

        mBinding?.tvEnvNumber?.text = "$envNumber"
        mBinding?.tvEnvNumber?.visibility = if ((envNumber ?: 0) > 0) View.VISIBLE else View.GONE
        mBinding?.ivEnv?.visibility = if ((envNumber ?: 0) > 0) View.VISIBLE else View.GONE
    }

    private var mBinding: BaseCustomBubbleAttachPopupBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = DataBindingUtil.bind<BaseCustomBubbleAttachPopupBinding>(popupImplView)?.apply {
            tvSupportNumber.visibility = if ((easeNumber ?: 0) > 0) View.VISIBLE else View.GONE
            ivSupport.visibility = if ((easeNumber ?: 0) > 0) View.VISIBLE else View.GONE
            tvSupportNumber.text = "$easeNumber"

            tvCalendarNumber.text = "$calendarNumber"
            tvCalendarNumber.visibility = if ((calendarNumber ?: 0) > 0) View.VISIBLE else View.GONE
            ivCalendar.visibility = if ((calendarNumber ?: 0) > 0) View.VISIBLE else View.GONE


            tvEnvNumber.text = "$envNumber"
            tvEnvNumber.visibility = if ((envNumber ?: 0) > 0) View.VISIBLE else View.GONE
            ivEnv.visibility = if ((envNumber ?: 0) > 0) View.VISIBLE else View.GONE



            clRoot.setOnClickListener {
                bubbleClickAction?.invoke()
                dismiss()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }
}