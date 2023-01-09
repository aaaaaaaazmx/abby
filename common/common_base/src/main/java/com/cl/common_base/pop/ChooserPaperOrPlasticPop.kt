package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.ChoosePaperClonePlastBinding
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.CenterPopupView

/**
 * 选择纸杯子还是塑料杯子
 */
class ChooserPaperOrPlasticPop(
    context: Context,
    private val onConfirmAction: ((cloneCheck: Boolean) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
    private val onLearnMoreAction: (() -> Unit)? = null,
    private val onNotReadAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.choose_paper_clone_plast
    }

    private lateinit var mBinding: ChoosePaperClonePlastBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = DataBindingUtil.bind<ChoosePaperClonePlastBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ChooserPaperOrPlasticPop
            executePendingBindings()

            checkClone.setOnClickListener {
                checkPlast.isChecked = !checkPlast.isChecked
                checkParper.isChecked = !checkPlast.isChecked
                btnSuccess.isEnabled =
                    checkPlast.isChecked || checkParper.isChecked
            }

            checkSeed.setOnClickListener {
                checkParper.isChecked = !checkParper.isChecked
                checkPlast.isChecked = !checkParper.isChecked
                btnSuccess.isEnabled =
                    checkPlast.isChecked || checkParper.isChecked
            }

            btnSuccess.setOnClickListener {
                // 具体执行哪一步
                val cloneCheck = checkPlast.isChecked
                val seedCheck = checkParper.isChecked
                onConfirmAction?.invoke(cloneCheck)
                dismiss()
            }

            ivClose.setOnClickListener { dismiss() }

        }!!
    }
}