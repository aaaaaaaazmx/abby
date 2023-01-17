package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.ChooseTipPopBinding
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.CenterPopupView

/**
 * 选择种子还是clone的tip弹窗
 */
class ChooserTipsPop(
    context: Context,
    private val onConfirmAction: (()->Unit)? = null,
    private val onCancelAction: (()->Unit)? = null,
    private val onLearnMoreAction: (()->Unit)? = null,
    private val onNotReadAction: (()->Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.choose_tip_pop
    }

    private lateinit var mBinding: ChooseTipPopBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = DataBindingUtil.bind<ChooseTipPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ChooserTipsPop
            executePendingBindings()


            btnRead.setOnClickListener {
                onConfirmAction?.invoke()
            }

            btnNoRead.setOnClickListener {
                onNotReadAction?.invoke()
            }

            ivLearnMore.setOnClickListener {
                onLearnMoreAction?.invoke()
            }

            ivClose.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }

        }!!
    }
}