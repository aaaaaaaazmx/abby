package com.cl.modules_home.widget

import android.content.Context
import android.text.method.LinkMovementMethod
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePlantFivePopBinding
import com.cl.common_base.ext.dp2px
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.span.appendClickable
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * 开水下一步
 * plant5
 *
 * @author 李志军 2022-08-06 17:35
 */
class HomePlantFivePop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_five_pop
    }

    private var binding: HomePlantFivePopBinding? = null

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantFivePopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
        }
    }
}