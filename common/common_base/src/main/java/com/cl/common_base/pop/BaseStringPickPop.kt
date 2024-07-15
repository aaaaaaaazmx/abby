package com.cl.common_base.pop

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseStringPickPopBinding
import com.cl.common_base.widget.wheel.time.StringPicker
import com.lxj.xpopup.core.CenterPopupView

/**
 * 通用String滑动选择器
 */
class BaseStringPickPop(
    context: Context,
    private val title: String? = null,
    private val cancelText: String? = null,
    private val confirmText: String? = null,
    private val cancelAction: (() -> Unit)? = null,
    private val confirmAction: ((Int) -> Unit)? = null,
    private val listString: MutableList<String?>? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_string_pick_pop
    }

    private var index = 0

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseStringPickPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@BaseStringPickPop
            executePendingBindings()
            tvTitle.text = title

            this@BaseStringPickPop.listString?.let {
                scopeLayoutTime.setStringList(it.toList())
            }
            scopeLayoutTime.setSelectedScope(0)
            scopeLayoutTime.setOnStringSelectedListener(object : StringPicker.OnStringSelectedListener {
                override fun onScopeSelected(index: Int) {
                    this@BaseStringPickPop.index = index
                }

                override fun onScopeSelected(index: String?) {
                }
            })

            tvCancel.setOnClickListener {
                cancelAction?.invoke()
                dismiss()
            }

            tvConfirm.setOnClickListener {
                confirmAction?.invoke(index)
                dismiss()
            }
        }
    }
}