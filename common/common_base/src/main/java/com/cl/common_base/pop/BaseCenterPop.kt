package com.cl.common_base.pop

import android.content.Context
import android.text.SpannedString
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.underline
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseCenterBinding
import com.cl.common_base.util.ViewUtils
import com.lxj.xpopup.core.CenterPopupView
import com.tuya.sdk.device.cache.SmartCacheEntityManager

/**
 * 可以显示2个按钮、也可以显示1个按钮的弹窗
 */
class BaseCenterPop(
    context: Context,
    private val onConfirmAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
    private val content: String? = null,
    private val titleText: String? = null,
    private val confirmText: String? = context.getString(R.string.base_ok),
    private val cancelText: String? = context.getString(R.string.my_cancel),
    private val isShowCancelButton: Boolean = true,
    private val richText: String? = null, // 富文本
    private val spannedString: SpannedString? = null,
    private val contentBackGround: Int? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_center
    }

    override fun beforeShow() {
        super.beforeShow()
        content?.let {
            // 判断当前是否有富文本
            if (richText?.isNotEmpty() == true) {
                // 直接展示富文本
                binding?.tvContent?.text = buildSpannedString {
                    // 3 Month Digital will be added to dee@baypac.com
                    bold { append(it) }
                    appendLine()
                    color(context.getColor(R.color.mainColor)) {
                        bold {
                            appendLine("$richText")
                        }
                    }
                }
            } else {
                binding?.tvContent?.text = it
            }
        }
        // 富文本
        spannedString?.let {
            binding?.tvContent?.text = it
        }
    }

    private var binding: BaseCenterBinding? = null

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<BaseCenterBinding>(popupImplView)?.apply {
            tvConfirm.text = confirmText
            tvCancel.text = cancelText
            tvTitle.text = titleText
            contentBackGround?.let { ivBackground.setImageResource(it) }


            // 是否显示中间图拼啊
            ViewUtils.setVisible(contentBackGround != null, ivBackground)
            // 是否显示标题
            ViewUtils.setVisible(titleText?.isNotEmpty() == true, tvTitle)
            // 是否显示和隐藏按钮
            ViewUtils.setVisible(isShowCancelButton, tvCancel, xpopupDivider2)
            // 隐藏中间内容
            ViewUtils.setVisible(content?.isNotEmpty() == true, tvContent)

            tvCancel.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
            }
            tvConfirm.setOnClickListener {
                dismiss()
                onConfirmAction?.invoke()
            }
        }
    }
}