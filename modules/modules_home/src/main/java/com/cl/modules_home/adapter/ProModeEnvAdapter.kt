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

class ProModeEnvAdapter(data: MutableList<EnvParamListBeanItem>?) :
    BaseQuickAdapter<EnvParamListBeanItem, BaseDataBindingHolder<HomeProModeEnvItemBinding>>(R.layout.home_pro_mode_env_item, data) {


    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun convert(holder: BaseDataBindingHolder<HomeProModeEnvItemBinding>, item: EnvParamListBeanItem) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
            // 还有个灰色状态。

            //  这个开始时间就是从第一周第一天开始。第二个就是上一个的结束时间。
            if (holder.layoutPosition == 0) {
                tvDateRang.text = "week1 day1-week${item.week} day${item.day}"
                item.sweek = 1
                item.sday = 1
            } else {
                // 开始时间是上一个的结束时间
                tvDateRang.text = "week${item.sweek} day${item.sday}-week${item.week} day${item.day}"
            }

            ivGth.setSafeOnClickListener {
                // 跳转富文本
                val intent = Intent(context, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, item.ppfdTxtId)
                intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, false)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "Back")
                context.startActivity(intent)
            }
            tvFan.setSafeOnClickListener {
                // 跳转富文本
                val intent = Intent(context, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, item.ppfdTxtId)
                intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, false)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "Back")
                context.startActivity(intent)
            }

            // 只有最后一个和只有第0个时候才能显示删除按钮
            ivClose.visibility = if (holder.layoutPosition == this@ProModeEnvAdapter.data.size - 1 && holder.layoutPosition != 0) {
                View.VISIBLE
            } else {
                View.INVISIBLE
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
            fanIntakeSeekbar.setProgress(item.brightValue.safeToFloat())
            fanIntakeSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(p0: SeekParams?) {

                }

                override fun onStartTrackingTouch(p0: IndicatorSeekBar?) {
                }

                override fun onStopTrackingTouch(seekbar: IndicatorSeekBar?) {
                    val progress = seekbar?.progress ?: 0
                    if (progress >= 7) {
                        val boolean = Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_FAN_SEVEN_TIP, false)
                        if (!boolean) {
                            context.let {
                                xpopup(it) {
                                    isDestroyOnDismiss(false)
                                    dismissOnTouchOutside(false)
                                    asCustom(HomeFanBottonPop(it, title = "You're about to set the intake fan to its maximum level. Be aware that this may cause 'wind burn,' leading to rapid water loss in the leaves. We recommend keeping the intake fan level below 7 during the plant's first four weeks.", tag = HomeFanBottonPop.FAN_TAG, remindMeAction = {
                                    }, benOKAction = {})).show()
                                }
                            }
                        }
                    }
                    tvFanValue.text = seekbar?.progress.toString()
                    // 把当前的数据也设置到data数据中。
                    this@ProModeEnvAdapter.data[holder.layoutPosition].brightValue = seekbar?.progress ?: 0
                }
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