package com.cl.common_base.pop

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.databinding.BasePopActivityBinding
import com.cl.common_base.ext.logI
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * 通用弹窗
 */
class BasePopActivity : BaseActivity<BasePopActivityBinding>() {
    override fun initView() {
        /*
                binding.vvDivider.setOnClickListener { finish() }
        */

        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.cl) { v, insets ->
            binding.ll.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop

                val from = BottomSheetBehavior.from(binding.ll)
                from.skipCollapsed = true
                from.state = BottomSheetBehavior.STATE_EXPANDED
                from.addBottomSheetCallback(callback)
            }
            return@setOnApplyWindowInsetsListener insets
        }

    }

    private val callback by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.vvDivider.alpha = slideOffset
            }
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        BottomSheetBehavior.from(binding.ll).removeBottomSheetCallback(callback)
    }
}