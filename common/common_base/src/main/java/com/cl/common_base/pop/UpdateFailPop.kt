package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseUpdateFailPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 升级失败弹窗
 *
 * @author 李志军 2022-08-17 13:33
 */
class UpdateFailPop(
    context: Context,
    private val onRetryAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_update_fail_pop
    }

    var binding: BaseUpdateFailPopBinding? = null
    override fun onCreate() {
        binding = DataBindingUtil.bind<BaseUpdateFailPopBinding>(popupImplView)?.apply {
            tvContent.setOnClickListener {
                dismiss()
                onRetryAction?.invoke()
            }
            tvCancel.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
            }
        }
    }

    // 失败内容赋值
    fun setFailtext(text: String) {
        binding?.tvContent?.text = text
    }
}