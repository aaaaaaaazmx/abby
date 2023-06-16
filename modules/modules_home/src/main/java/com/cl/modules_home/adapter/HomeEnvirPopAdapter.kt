package com.cl.modules_home.adapter

import android.graphics.Color
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeEnvirItemFanPopBinding
import com.bbgo.module_home.databinding.HomeEnvirItemPopBinding
import com.bbgo.module_home.databinding.HomeGrowLightItemPopBinding
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.EnvironmentInfoData
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.widget.SwitchButton
import com.cl.common_base.widget.toast.ToastUtil
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams

/**
 * pop初始化数据
 *
 * @author 李志军 2022-08-06 18:44
 */
class HomeEnvirPopAdapter(data: MutableList<EnvironmentInfoData.Environment>?) :
    BaseMultiItemQuickAdapter<EnvironmentInfoData.Environment, BaseViewHolder>(data) {

    init {
        addItemType(EnvironmentInfoData.KEY_TYPE_NORMAL, R.layout.home_envir_item_pop)
        addItemType(EnvironmentInfoData.KEY_TYPE_FAN, R.layout.home_envir_item_fan_pop)
        addItemType(EnvironmentInfoData.KEY_TYPE_LIGHT, R.layout.home_grow_light_item_pop)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            EnvironmentInfoData.KEY_TYPE_FAN -> {
                val binding = DataBindingUtil.bind<HomeEnvirItemFanPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            EnvironmentInfoData.KEY_TYPE_NORMAL -> {
                val binding = DataBindingUtil.bind<HomeEnvirItemPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }

            EnvironmentInfoData.KEY_TYPE_LIGHT -> {
                val binding = DataBindingUtil.bind<HomeGrowLightItemPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }


    override fun convert(helper: BaseViewHolder, item: EnvironmentInfoData.Environment) {
        when (helper.itemViewType) {
            EnvironmentInfoData.KEY_TYPE_FAN -> {
                helper.getView<SwitchButton>(R.id.fis_item_switch).apply {
                    isChecked = item.automation == 1
                    ViewUtils.setVisible(item.automation != 1, helper.getView(R.id.rl_fan_intake), helper.getView(R.id.rl_fan_exhaust))
                    setOnCheckedChangeListener { button, isChecked ->
                        /*helper.setText(R.id.tv_desc, if (isChecked) "Auto" else "Manual")*/
                        ViewUtils.setVisible(isChecked == false, helper.getView(R.id.rl_fan_intake), helper.getView(R.id.rl_fan_exhaust))
                        listener?.onCheckedChanged(button, isChecked)
                    }
                }
                helper.setText(R.id.tv_fan_value, item.fanIntake.toString())
                helper.setText(R.id.tv_fan_exhaust_value, item.fanExhaust.toString())

                helper.getView<IndicatorSeekBar>(R.id.fan_intake_seekbar).apply {
                    setProgress(item.fanIntake?.toFloat() ?: 0f)
                    onSeekChangeListener = object : OnSeekChangeListener {
                        override fun onSeeking(p0: SeekParams?) {

                        }

                        override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                            DeviceControl.get()
                                .success {
                                    helper.setText(R.id.tv_fan_value, seekBar?.progress.toString())
                                }
                                .error { code, error ->
                                    ToastUtil.shortShow(
                                        """
                                          fanIntake: 
                                          code-> $code
                                          errorMsg-> $error
                                            """.trimIndent()
                                    )
                                    helper.setText(R.id.tv_fan_value, item.fanIntake.toString())
                                }
                                .fanIntake(seekBar?.progress ?: 0)
                        }
                    }
                }

                helper.getView<IndicatorSeekBar>(R.id.fan_exhaust_seekbar).apply {
                    setProgress(item.fanExhaust?.toFloat() ?: 0f)
                    onSeekChangeListener = object : OnSeekChangeListener {
                        override fun onSeeking(p0: SeekParams?) {

                        }

                        override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                            DeviceControl.get()
                                .success {
                                    helper.setText(R.id.tv_fan_exhaust_value, seekBar?.progress.toString())
                                }
                                .error { code, error ->
                                    ToastUtil.shortShow(
                                        """
                                          fanExhaust: 
                                          code-> $code
                                          errorMsg-> $error
                                            """.trimIndent()
                                    )
                                    helper.setText(R.id.tv_fan_exhaust_value, item.fanExhaust.toString())
                                }
                                .fanExhaust(seekBar?.progress ?: 0)
                        }
                    }
                }

                helper.getView<ImageView>(R.id.iv_gt).apply {
                    if (item.alert == 0) {
                        setImageResource(com.cl.common_base.R.mipmap.base_gt)
                    } else {
                        setImageResource(com.cl.common_base.R.mipmap.base_error_gt)
                    }
                }
            }

            EnvironmentInfoData.KEY_TYPE_NORMAL -> {
                helper.setText(R.id.period_title, item.detectionValue)
                helper.setText(R.id.period_time, item.healthStatus)
                helper.setText(R.id.tv_going, temperatureConversion(item.value))
                helper.setTextColor(R.id.period_time, getColor(item.detectionValue, item.healthStatus))
                helper.getView<ImageView>(R.id.iv_gt).apply {
                    if (item.alert == 0) {
                        setImageResource(com.cl.common_base.R.mipmap.base_gt)
                    } else {
                        setImageResource(com.cl.common_base.R.mipmap.base_error_gt)
                    }
                }
            }

            EnvironmentInfoData.KEY_TYPE_LIGHT -> {
                helper.setText(R.id.period, item.detectionValue)
                helper.setText(R.id.period_status, item.explain)
                helper.setText(R.id.tv_healthStatuss, item.healthStatus)
                when(item.healthStatus) {
                    "OK" -> {
                        helper.setTextColor(R.id.tv_healthStatuss, Color.BLACK)
                    }
                    "Error" -> {
                        helper.setTextColor(R.id.tv_healthStatuss, Color.parseColor("#D61744"))
                    }
                }
                helper.getView<ImageView>(R.id.iv_gt).apply {
                    if (item.alert == 0) {
                        setImageResource(com.cl.common_base.R.mipmap.base_gt)
                    } else {
                        setImageResource(com.cl.common_base.R.mipmap.base_error_gt)
                    }
                }
            }
        }
    }

    /**
     * @param detectionValue 检测项
     * @param text 检测具体值
     */
   private fun getColor(detectionValue: String?, text: String?): Int {
        return when (text) {
            "Too High" -> Color.parseColor("#D61744")
            "High" -> {
                if (detectionValue == "Water Tank Temperture") Color.parseColor("#006241") else Color.parseColor(
                    "#E3A00D"
                )
            }
            "Ideal" -> Color.parseColor("#006241")
            "Low" -> {
                if (detectionValue == "Water Tank Temperture") Color.parseColor("#006241") else Color.parseColor(
                    "#E3A00D"
                )
            }
            "Too Low" -> Color.parseColor("#D61744")
            "OK" -> Color.parseColor("#006241")
            "Error" -> Color.parseColor("#D61744")
            else -> Color.BLACK
        }
    }

    private  fun temperatureConversion(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        if (text.contains("℉")) {
            // 默认为false
            val isF = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
            if (isF) {
                kotlin.runCatching {
                    val replace = text.replace("℉", "")
                    val te = replace.toInt()
                    // (1°F − 32) × 5/9
                    // String result1 = String.format("%.2f", d);
                    return "${String.format("%.1f", (te - 32).times(5f).div(9f))}℃"
                }.getOrElse {
                    return text
                }
            }
            return text
        }
        return text
    }

    interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean)
    }

    private var listener: OnCheckedChangeListener? = null

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        this.listener = listener
    }

}