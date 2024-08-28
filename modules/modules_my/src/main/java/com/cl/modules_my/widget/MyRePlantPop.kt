package com.cl.modules_my.widget

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyRePlantPopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 删除植物弹窗
 *
 * @author 李志军 2022-08-12 12:33
 */
class MyRePlantPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null
) : BottomPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.my_re_plant_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyRePlantPopBinding>(popupImplView)?.apply {
            tvDec.text = buildSpannedString {
                append(context.getString(com.cl.common_base.R.string.setting_replant_tips))
                color( ResourcesCompat.getColor(
                    resources,
                    com.cl.common_base.R.color.textRed,
                    context.theme
                )) {
                    append(context.getString(com.cl.common_base.R.string.string_1874))
                }
            }

            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                dismiss()
                onNextAction?.invoke()
            }

        }
    }
}