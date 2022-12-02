package com.cl.common_base.pop

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.databinding.BasePopActivityBinding
import com.lxj.xpopup.XPopup.getAnimationDuration
import com.lxj.xpopup.widget.SmartDragLayout

/**
 * 通用弹窗
 */
class BasePopActivity : BaseActivity<BasePopActivityBinding>() {
    override fun initView() {
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.smart) { v, insets ->
            binding.smart.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
        binding.smart.setDuration(getAnimationDuration());
        binding.smart.enableDrag(true)
        binding.smart.dismissOnTouchOutside(false)
        binding.smart.isThreeDrag(false)
        binding.smart.open()
        binding.smart.setOnCloseListener(callback)
    }

    private val callback by lazy {
        object : SmartDragLayout.OnCloseListener {
            override fun onClose() {
                finish()
            }

            override fun onDrag(y: Int, percent: Float, isScrollUp: Boolean) {
                binding.smart.alpha = percent
            }

            override fun onOpen() {
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
    }
}