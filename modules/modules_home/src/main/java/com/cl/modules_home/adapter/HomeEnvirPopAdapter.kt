package com.cl.modules_home.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeEnvirGridBinding
import com.cl.modules_home.databinding.HomeEnvirItemFanPopBinding
import com.cl.modules_home.databinding.HomeEnvirItemPopBinding
import com.cl.modules_home.databinding.HomeGrowLightItemPopBinding
import com.cl.modules_home.databinding.HomeTextItemBinding
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.EnvironmentInfoData
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.equalsIgnoreCase
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.widget.FeatureItemSwitch
import com.cl.common_base.widget.SvTextView
import com.cl.common_base.widget.SwitchButton
import com.cl.common_base.widget.decoraion.FullyGridLayoutManager
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.activity.ProModeEnvActivity
import com.cl.modules_home.databinding.HomeGrowDripPopBinding
import com.cl.modules_home.widget.HomeFanBottonPop
import com.google.android.material.transition.Hold
import com.luck.picture.lib.utils.DensityUtil
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * pop初始化数据
 *
 * @author 李志军 2022-08-06 18:44
 */
class HomeEnvirPopAdapter(data: MutableList<EnvironmentInfoData.Environment>?, val airPumpAction: ((HomeEnvirPopAdapter, EnvironmentInfoData.Environment, Int ,Boolean)->Unit)? = null, val dripKitAction : ((HomeEnvirPopAdapter, EnvironmentInfoData.Environment, Int ,Boolean)->Unit)? = null) :
    BaseMultiItemQuickAdapter<EnvironmentInfoData.Environment, BaseViewHolder>(data) {

    init {
        addItemType(EnvironmentInfoData.KEY_TYPE_GRID, R.layout.home_envir_grid)// recyclview
        addItemType(EnvironmentInfoData.KEY_TYPE_TEXT, R.layout.home_text_item)// 文字描述
        addItemType(EnvironmentInfoData.KEY_TYPE_NORMAL, R.layout.home_envir_item_pop)
        addItemType(EnvironmentInfoData.KEY_TYPE_FAN, R.layout.home_envir_item_fan_pop)
        addItemType(EnvironmentInfoData.KEY_TYPE_LIGHT, R.layout.home_grow_light_item_pop)
        addItemType(EnvironmentInfoData.KEY_DRIP, R.layout.home_grow_drip_pop)
    }

    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            EnvironmentInfoData.KEY_TYPE_GRID -> {
                val binding = DataBindingUtil.bind<HomeEnvirGridBinding>(holder.itemView)
                binding?.executePendingBindings()
            }

            EnvironmentInfoData.KEY_TYPE_TEXT -> {
                val binding = DataBindingUtil.bind<HomeTextItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }

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

            EnvironmentInfoData.KEY_DRIP -> {
                val binding = DataBindingUtil.bind<HomeGrowDripPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }


    @SuppressLint("StringFormatInvalid")
    override fun convert(helper: BaseViewHolder, item: EnvironmentInfoData.Environment) {
        when (helper.itemViewType) {
            EnvironmentInfoData.KEY_TYPE_GRID -> {
                helper.getView<RecyclerView>(R.id.rv_envir).apply {
                    layoutManager = GridLayoutManager(context, 2)
                    /*addItemDecoration(
                        GridSpaceItemDecoration(
                            2,
                            DensityUtil.dip2px(context, 0f), DensityUtil.dip2px(context, 18f)
                        )
                    )*/
                    adapter = EnvAdapter(item.additionalData)
                }
            }

            EnvironmentInfoData.KEY_TYPE_FAN -> {
                ViewUtils.setVisible(item.runningModelShow?.equalsIgnoreCase("Manual") == true, helper.getView(R.id.rl_fan_intake), helper.getView(R.id.rl_fan_exhaust))

                helper.setText(R.id.tv_fan_value, item.fanIntake.toString())
                helper.setText(R.id.tv_fan_exhaust_value, item.fanExhaust.toString())

                helper.getView<IndicatorSeekBar>(R.id.fan_intake_seekbar).apply {
                    customSectionTrackColor { colorIntArr ->
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
                    setProgress(item.fanIntake?.toFloat() ?: 0f)
                    onSeekChangeListener = object : OnSeekChangeListener {
                        override fun onSeeking(p0: SeekParams?) {

                        }

                        override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                            val progress = seekBar?.progress ?: 0
                            if (progress >= 7) {
                                val boolean = Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_FAN_SEVEN_TIP, false)
                                if (!boolean) {
                                    context?.let {
                                        xpopup(it) {
                                            isDestroyOnDismiss(false)
                                            dismissOnTouchOutside(false)
                                            asCustom(
                                                HomeFanBottonPop(
                                                    it,
                                                    title = context.getString(com.cl.common_base.R.string.home_you_re_about_to),
                                                    tag = HomeFanBottonPop.FAN_TAG,
                                                    remindMeAction = {
                                                    },
                                                    benOKAction = {})
                                            ).show()
                                        }
                                    }
                                }
                            }
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
                helper.setTextColor(R.id.period_time, getColor(item.environmentType, item.detectionValue, item.healthStatus))
                helper.getView<ImageView>(R.id.iv_gt).apply {
                    if (item.alert == 0) {
                        setImageResource(com.cl.common_base.R.mipmap.base_gt)
                    } else {
                        setImageResource(com.cl.common_base.R.mipmap.base_error_gt)
                    }
                }
                if (!item.roomData.isNullOrEmpty()) {
                    if (item.value?.contains("%") == true) {
                        helper.setText(R.id.tv_going_unit, context.getString(com.cl.common_base.R.string.home_room_one, "${item.roomData}"))
                    } else if (item.value?.contains("℉") == true) {
                        val temp = com.cl.common_base.ext.temperatureConversion(item.roomData.safeToFloat(), isMetric)
                        val tempUnit = if (isMetric) "℃" else "℉"
                        helper.setText(R.id.tv_going_unit, context.getString(com.cl.common_base.R.string.home_rooms, "$temp", "$tempUnit"))
                    }
                }
            }

            EnvironmentInfoData.KEY_TYPE_LIGHT -> {
                helper.setText(R.id.period, item.detectionValue)
                helper.setText(R.id.period_status, item.explain)
                helper.setText(R.id.tv_healthStatuss, item.healthStatus)
                helper.getView<ImageView>(R.id.iv_light_status).apply {
                    setSafeOnClickListener {
                        // 跳转到灯光设置参数界面
                        context.startActivity(Intent(context, ProModeEnvActivity::class.java).apply {
                            putExtra(ProModeEnvActivity.STEP, item.step)
                            putExtra(ProModeEnvActivity.STEP_NOW, item.stepShow)
                            putExtra(ProModeEnvActivity.TEMPLATE_ID, item.templateId)
                            putExtra(ProModeEnvActivity.IS_UPDATE_LIGHT, true)
                        })
                    }
                }
                when (item.healthStatus) {
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

            EnvironmentInfoData.KEY_DRIP -> {
                helper.getView<FeatureItemSwitch>(R.id.fis_item_switchs).apply {
                    isItemChecked = item.dripKit == true
                    setSwitchCheckedChangeListener { compoundButton, b ->
                        item.dripKit = b
                        dripKitAction?.invoke(this@HomeEnvirPopAdapter, item, helper.layoutPosition, b)
                        notifyItemChanged(helper.layoutPosition)
                    }
                }

                helper.getView<FeatureItemSwitch>(R.id.air_pump).apply {
                    setSwitchCheckedChangeListener { compoundButton, b ->
                        airPumpAction?.invoke(this@HomeEnvirPopAdapter, item, helper.layoutPosition, b)
                    }
                }

                helper.getView<ImageView>(R.id.tv_air_pump_desc).apply {
                    setSafeOnClickListener {
                        InterComeHelp.INSTANCE.openInterComeSpace(
                            InterComeHelp.InterComeSpace.Article,
                            Constants.InterCome.KEY_INTER_COME_AIR_PUMP
                        )
                    }
                }

                helper.getView<ImageView>(R.id.tv_drip_pump_desc).apply {
                    setSafeOnClickListener {
                        InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_DRIP)
                    }
                }
            }
        }
    }

    /**
     * @param detectionValue 检测项
     * @param text 检测具体值
     */
    private fun getColor(type: String?, detectionValue: String?, text: String?): Int {
        return when (text) {
            "Too High" -> Color.parseColor("#D61744")
            "High" -> {
                if (type == EnvironmentInfoData.KEY_TYPE_WATER_TEMPERATURE_TYPE) Color.parseColor("#006241") else Color.parseColor(
                    "#E3A00D"
                )
            }

            "Ideal" -> Color.parseColor("#006241")
            "Low" -> {
                if (type == EnvironmentInfoData.KEY_TYPE_WATER_TEMPERATURE_TYPE) Color.parseColor("#006241") else Color.parseColor(
                    "#E3A00D"
                )
            }

            "Too Low" -> Color.parseColor("#D61744")
            "OK" -> Color.parseColor("#006241")
            "Error" -> Color.parseColor("#D61744")
            else -> Color.BLACK
        }
    }

    private fun temperatureConversion(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        if (text.contains("℉")) {
            // 默认为false
            val isF = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
            if (isF) {
                kotlin.runCatching {
                    val replace = text.replace("℉", "")
                    val te = replace.safeToInt()
                    // (1°F − 32) × 5/9
                    // String result1 = String.format("%.2f", d);
                    return "${String.format(Locale.US, "%.1f", (te - 32).times(5f).div(9f))}℃"
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