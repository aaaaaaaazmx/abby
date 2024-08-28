package com.cl.common_base.pop

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.bean.UnreadMessageData
import com.cl.common_base.databinding.HomePlantSixPopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * plant6
 *
 * @author 李志军 2022-08-06 17:35
 */
class HomePlantSixPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null,
    var isFattening: Boolean? = false, // 是否是加水、加肥三步中的最后一步加肥 , false 半包、 true 一包。
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_six_pop
    }

    private var binding: HomePlantSixPopBinding? = null

    override fun beforeShow() {
        super.beforeShow()
        if (isFattening == true) {
            // 是加肥
            binding?.tvDec?.text = context.getString(R.string.decs).trimIndent()

            binding?.ivAdd?.background = ContextCompat.getDrawable(
                context,
                R.mipmap.base_six_feed_bg
            )
        }else {
            binding?.tvDec?.text = context.getString(R.string.decs_two).trimIndent()

            binding?.ivAdd?.background = ContextCompat.getDrawable(
                context,
                R.mipmap.base_six_bg
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantSixPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
        }
    }
}
