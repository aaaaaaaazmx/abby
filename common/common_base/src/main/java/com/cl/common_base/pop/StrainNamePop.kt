package com.cl.common_base.pop

import android.content.Context
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.ChooseSeedPopBinding
import com.cl.common_base.databinding.StrainNameBinding
import com.cl.common_base.util.databinding.bindLayoutManager
import com.google.gson.annotations.Until
import com.lxj.xpopup.core.BottomPopupView

/**
 * StrainName Pop
 */
class StrainNamePop(
    context: Context,
    private val onConfirmAction: ((strainName: String) -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.strain_name
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<StrainNameBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }

            strainName.addTextChangedListener {
                if (it.isNullOrEmpty()) return@addTextChangedListener

                // 点击按钮状态监听
                btnSuccess.isEnabled = !it.isNullOrEmpty()
            }

            // 清空输入内容
            curingDelete.setOnClickListener {
                strainName.setText("")
            }

            tvHow.setOnClickListener{
                // todo 跳转固定的图文介绍界面
            }

            clNotKnow.setOnClickListener {
                // todo 跳转固定的不知道界面
            }

            btnSuccess.setOnClickListener {
                onConfirmAction?.invoke(strainName.text.toString())
            }

        }
    }
}