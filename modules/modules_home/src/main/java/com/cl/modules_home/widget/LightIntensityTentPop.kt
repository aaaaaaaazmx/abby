package com.cl.modules_home.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeLightPopsBinding
import com.cl.modules_home.databinding.HomeLightTentPopBinding
import com.lxj.xpopup.core.CenterPopupView
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams

/**
 * 光照强度弹窗, 只有tent机器的时候才有这个
 */
class LightIntensityTentPop(context: Context, var brightValue: Float? = 0f, val onSeekAction: ((Float) -> Unit)? = null) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_light_tent_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeLightTentPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@LightIntensityTentPop
            executePendingBindings()

            ivClose.setSafeOnClickListener {
                dismiss()
            }

            // 风扇滑块
            fanIntakeSeekbar.setProgress(brightValue ?: 0f)
            fanIntakeSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(p0: SeekParams?) {

                }

                override fun onStartTrackingTouch(p0: IndicatorSeekBar?) {
                }

                @SuppressLint("SetTextI18n")
                override fun onStopTrackingTouch(seekbar: IndicatorSeekBar?) {
                    tvValue.text = "${seekbar?.progress ?: 0f}%"
                    onSeekAction?.invoke(seekbar?.progress.toString().safeToFloat())
                }
            }

        }
    }
}