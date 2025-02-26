package com.cl.modules_home.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.view.isInvisible
import com.cl.modules_home.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.setVisible
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.databinding.HomeProModeEnvItemBinding
import com.cl.modules_home.request.EnvParamListBeanItem
import com.cl.modules_home.widget.HomeFanBottonPop
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams

class ProModeEnvAdapter(data: MutableList<EnvParamListBeanItem>?, private val plantType: String? = null) : BaseQuickAdapter<EnvParamListBeanItem, BaseDataBindingHolder<HomeProModeEnvItemBinding>>(R.layout.home_pro_mode_env_item, data) {


    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun convert(holder: BaseDataBindingHolder<HomeProModeEnvItemBinding>, item: EnvParamListBeanItem) {
        holder.dataBinding?.apply {
            data = item
            plantType = this@ProModeEnvAdapter.plantType
            executePendingBindings()
            // 还有个灰色状态。

            //  这个开始时间就是从第一周第一天开始。第二个就是上一个的结束时间。
            if (holder.layoutPosition == 0) {
                tvDateRang.text = context.getString(com.cl.common_base.R.string.home_week1_day1_week_day, "${item.week}", "${item.day}")
                item.sweek = 1
                item.sday = 1
            } else {
                // 开始时间是上一个的结束时间
                tvDateRang.text =
                    "${context.getString(com.cl.common_base.R.string.weeks)}${item.sweek} ${context.getString(com.cl.common_base.R.string.days)}${item.sday}" + "-${context.getString(com.cl.common_base.R.string.weeks)}${item.week} ${
                        context.getString(com.cl.common_base.R.string.days)
                    }${item.day}"
            }

            ivGth.setSafeOnClickListener {
                // 跳转富文本
                val intent = Intent(context, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, item.ppfdTxtId)
                intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, false)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, context.getString(com.cl.common_base.R.string.string_2251))
                context.startActivity(intent)
            }
            tvFan.setSafeOnClickListener {
                // 跳转富文本
                val intent = Intent(context, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, item.ppfdTxtId)
                intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, false)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, context.getString(com.cl.common_base.R.string.string_2251))
                context.startActivity(intent)
            }

            // 只有最后一个和只有第0个时候才能显示删除按钮
            ivClose.visibility = if (holder.layoutPosition == this@ProModeEnvAdapter.data.size - 1 && holder.layoutPosition != 0) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            // 显示时间
            // 获取当前开关灯的时间
            // 0- 12, 12-24
            val startTime = when (item.turnOnLight) {
                0 -> 12
                12 -> 24
                else -> item.turnOnLight
            }

            val endTime = when (item.turnOffLight) {
                0 -> 12
                12 -> 24
                else -> item.turnOffLight
            }

            val ftTurnOn = startTime.let {
                if (it > 12) {
                    "${it - 12}:00 PM"
                } else if (it < 12) {
                    "${it}:00 AM"
                } else if (it == 12) {
                    "12:00 AM"
                } else {
                    "12:00 AM"
                }
            }

            val ftTurnOff = endTime.let {
                if (it > 12) {
                    "${it - 12}:00 PM"
                } else if (it < 12) {
                    "${it}:00 AM"
                } else if (it == 12) {
                    "12:00 AM"
                } else {
                    "12:00 AM"
                }
            }

            // 展示灯光的开始时间和结束时间
            val lightSchedule = "$ftTurnOn-$ftTurnOff"
            ftTimer.text = lightSchedule
        }
    }
}