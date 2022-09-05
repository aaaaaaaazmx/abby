package com.cl.common_base.pop

import android.content.Context
import android.view.animation.DecelerateInterpolator
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.UpdateProgressBinding
import com.lxj.xpopup.core.CenterPopupView


/**
 * 固件升级进度条
 *
 * @author 李志军 2022-08-17 11:31
 */
class UpdateProgressPop(
    context: Context,
    var progress: Int? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.update_progress
    }

    fun setData(progress: Int) {
        this.progress = progress
        binding?.circleBar?.value = progress.toFloat()
    }

    override fun onDismiss() {
        super.onDismiss()
        binding?.circleBar?.reset()
    }

    var binding: UpdateProgressBinding? = null
    override fun onCreate() {
        binding = DataBindingUtil.bind<UpdateProgressBinding>(popupImplView)?.apply {
            circleBar.reset()
        }
    }
}