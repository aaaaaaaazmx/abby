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
                append("After clicking Replant, the device will switch to the planting mode of the first week. ")
                color( ResourcesCompat.getColor(
                    resources,
                    com.cl.common_base.R.color.textRed,
                    context.theme
                )) {
                    "This operation is irreversible."
                }
            }

            ivClose.setOnClickListener { smartDismiss() }
            btnSuccess.setOnClickListener {
                smartDismiss()
                onNextAction?.invoke()
            }

        }
    }
}