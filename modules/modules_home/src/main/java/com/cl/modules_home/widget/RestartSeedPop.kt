package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePopGerminationCountdownBinding
import com.bbgo.module_home.databinding.HomePopRestartSeedBinding
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.slidetoconfirmlib.SlideToConfirm
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.CenterPopupView

/**
 * @Description 删除种子弹窗
 */
class RestartSeedPop(
    context: Context,
    private val onDeletePlant: () -> Unit = {},
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_pop_restart_seed
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomePopRestartSeedBinding>(popupImplView)?.apply {
            lifecycleOwner = this@RestartSeedPop
            ivClose.setOnClickListener { dismiss() }
            confirm.slideListener = object : ISlideListener {
                override fun onSlideStart() {

                }

                override fun onSlideMove(percent: Float) {
                }

                override fun onSlideCancel() {
                }

                override fun onSlideDone() {
                    // 删除植物接口
                    onDeletePlant.invoke()
                    dismiss()
                }
            }
        }
    }
}