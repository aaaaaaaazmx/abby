package com.cl.common_base.pop

import android.content.Context
import android.graphics.Typeface
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.StrainNameBinding
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * StrainName Pop
 */
class StrainNamePop(
    context: Context,
    private val onConfirmAction: ((strainName: String) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
    private val isNoStrainName: Boolean? = false
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.strain_name
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<StrainNameBinding>(popupImplView)?.apply {
            tvHow.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            ViewUtils.setGone(ivClose, isNoStrainName ?: false)

            ivClose.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
                btnSuccess.isEnabled = false
            }

            strainName.addTextChangedListener {
                if (it.isNullOrEmpty()) return@addTextChangedListener

                // 点击按钮状态监听
                btnSuccess.isEnabled = !it.isNullOrEmpty()
            }

            // 清空输入内容
            curingDelete.setOnClickListener {
                strainName.setText("")
                btnSuccess.isEnabled = false
            }

            tvHow.setOnClickListener {
                XPopup.Builder(context)
                    .isDestroyOnDismiss(false)
                    .isDestroyOnDismiss(false)
                    .asCustom(BaseCenterPop(context, content = context.getString(R.string.seed_strain_name), isShowCancelButton = false))
                    .show()
            }

            clNotKnow.setOnClickListener {
                // 跳过的话，默认名字
                onConfirmAction?.invoke("I don’t know")
            }

            btnSuccess.setOnClickListener {
                // 输入范围为1～24字节
                if (getTextLength(strainName.text.toString()) < 1 || getTextLength(strainName.text.toString()) > 24) {
                    ToastUtil.shortShow(context.getString(R.string.strain_name_desc))
                    return@setOnClickListener
                }
                onConfirmAction?.invoke(strainName.text.toString())
            }

        }
    }

    /**
     * 屏蔽手机系统返回键
     */
    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onDismiss() {
        onCancelAction?.invoke()
        super.onDismiss()
    }

    private fun getTextLength(text: CharSequence): Int {
        var length = 0
        for (element in text) {
            if (element.code > 255) {
                length += 2
            } else {
                length++
            }
        }
        return length
    }
}