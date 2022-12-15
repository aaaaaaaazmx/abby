package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.ChooseSeedClonePopBinding
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.CenterPopupView

/**
 * 选择种子还是clone的tip弹窗
 */
class ChooserSeedOrClonePop(
    context: Context,
    private val onConfirmAction: ((cloneCheck: Boolean) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
    private val onLearnMoreAction: (() -> Unit)? = null,
    private val onNotReadAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.choose_seed_clone_pop
    }

    private lateinit var mBinding: ChooseSeedClonePopBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = DataBindingUtil.bind<ChooseSeedClonePopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ChooserSeedOrClonePop
            executePendingBindings()

            checkClone.setOnCheckedChangeListener { _, b ->
                if (checkSeed.isChecked) {
                    checkSeed.isChecked = !b
                }
                btnSuccess.isEnabled =
                    checkClone.isChecked || checkSeed.isChecked
            }

            checkSeed.setOnCheckedChangeListener { _, b ->
                if (checkClone.isChecked) {
                    checkClone.isChecked = !b
                }
                btnSuccess.isEnabled =
                    checkClone.isChecked || checkSeed.isChecked
            }

            btnSuccess.setOnClickListener {
                // 具体执行哪一步
                val cloneCheck = checkClone.isChecked
                val seedCheck = checkSeed.isChecked
                onConfirmAction?.invoke(cloneCheck)
                dismiss()
            }

            ivClose.setOnClickListener { dismiss() }

        }!!
    }
}