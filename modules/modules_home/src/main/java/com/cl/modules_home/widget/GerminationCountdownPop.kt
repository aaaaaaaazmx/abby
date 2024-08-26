package com.cl.modules_home.widget

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.underline
import androidx.databinding.DataBindingUtil
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomePopGerminationCountdownBinding
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.lxj.xpopup.core.CenterPopupView
import com.shuyu.gsyvideoplayer.utils.OrientationUtils

/**
 * @Description 播种倒计时弹窗
 */
class GerminationCountdownPop(
    context: Context,
    private val plantInfoData: PlantInfoData? = null,
    private val onSkipAction: () -> Unit = {},
    private val onCheckAction: () -> Unit = {},
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_pop_germination_countdown
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomePopGerminationCountdownBinding>(popupImplView)?.apply {
            lifecycleOwner = this@GerminationCountdownPop
            tVHtml.text = buildSpannedString {
                /*Check for a tap root in 1 day(s) 23 hrs... Lights should be off at this stage*/
                bold { append(context.getString(com.cl.common_base.R.string.string_1420)) }
                appendLine()
                color(ContextCompat.getColor(context, com.cl.common_base.R.color.textRed)) {
                    bold { append("${DateHelper.formatTime(plantInfoData?.germinationTime?.toLong()?.div(1000) ?: 0L, "dd")} day(s) ${DateHelper.formatTime(plantInfoData?.germinationTime?.toLong()?.div(1000) ?: 0L, "HH")} hrs..." ) }
                }
                appendLine()
                bold { append(context.getString(com.cl.common_base.R.string.string_1422)) }
            }
            btnSkip.setOnClickListener {
                onSkipAction.invoke()
            }
            btnCheck.setOnClickListener {
                onCheckAction.invoke()
            }
        }
    }
}