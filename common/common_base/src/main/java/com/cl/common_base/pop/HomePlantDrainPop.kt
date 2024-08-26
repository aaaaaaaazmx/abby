package com.cl.common_base.pop

import android.content.Context
import android.text.method.LinkMovementMethod
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.HomePlantDrainPopBinding
import com.cl.common_base.ext.dp2px
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.span.appendClickable
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * 排水弹窗
 *
 * @author 李志军
 */
class HomePlantDrainPop(
    context: Context,
    private var isShow: Boolean? = false,
    private val onNextAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
    private val onTvSkipAddWaterAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_drain_pop
    }

    private var binding: HomePlantDrainPopBinding? = null

    // 是否显示跳过文字
    fun setData(isShow: Boolean): HomePlantDrainPop {
        this.isShow = isShow
        ViewUtils.setVisible(isShow, binding?.tvSkipAddWater)
        return this
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
        ViewUtils.setGone(binding?.tvSkipAddWater)
    }

    override fun beforeShow() {
        super.beforeShow()
        isShow?.let { ViewUtils.setVisible(it, binding?.tvSkipAddWater) }
        // 判断是否是英制
        val isFractional = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        binding?.tvDec?.text = if (!isFractional) context.getString(R.string.string_163) else context.getString(R.string.string_164)
    }

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantDrainPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
            }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }

            // 弹出跳过加水的提示框
            tvSkipAddWater.text = buildSpannedString {
                color(context.resources.getColor(R.color.mainColor)) {
                    appendClickable(context.getString(R.string.base_water_already)) {
                        dismiss()
                        onTvSkipAddWaterAction?.invoke()
                    }
                }
            }
            // 点击无背景
            tvSkipAddWater.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
            tvSkipAddWater.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
                resources,
                R.color.transparent,
                context.theme
            )
        }
    }
}
