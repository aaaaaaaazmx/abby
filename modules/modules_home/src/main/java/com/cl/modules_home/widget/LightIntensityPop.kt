package com.cl.modules_home.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.databinding.DataBindingUtil
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeLightPopsBinding
import com.lxj.xpopup.core.CenterPopupView
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.coroutines.flow.Flow

/**
 * 光照强度弹窗, 只有abby机器的时候才有这个
 */
class LightIntensityPop(context: Context, var brightValue: Float? = 0f, val onSeekAction: ((Float) -> Unit)? = null) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_light_pops
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeLightPopsBinding>(popupImplView)?.apply {
            lifecycleOwner = this@LightIntensityPop
            executePendingBindings()

            ivClose.setSafeOnClickListener {
                dismiss()
            }

            // 风扇
            fanIntakeSeekbar.customSectionTrackColor { colorIntArr ->
                //the length of colorIntArray equals section count
                //                colorIntArr[0] = Color.parseColor("#008961");
                //                colorIntArr[1] = Color.parseColor("#008961");
                // 当刻度为最后4段时才显示红色
                // colorIntArr[6] = Color.parseColor("#F72E47")
                colorIntArr[7] = Color.parseColor("#F72E47")
                colorIntArr[8] = Color.parseColor("#F72E47")
                colorIntArr[9] = Color.parseColor("#F72E47")
                true //true if apply color , otherwise no change
            }

            // 风扇滑块
            fanIntakeSeekbar.setProgress(brightValue.safeToFloat())
            fanIntakeSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(p0: SeekParams?) {

                }

                override fun onStartTrackingTouch(p0: IndicatorSeekBar?) {
                }

                @SuppressLint("SetTextI18n")
                override fun onStopTrackingTouch(seekbar: IndicatorSeekBar?) {
                    tvValue.text = "${seekbar?.progress ?: 0f}"
                    onSeekAction?.invoke(seekbar?.progress.toString().safeToFloat())
                }
            }

        }
    }
}