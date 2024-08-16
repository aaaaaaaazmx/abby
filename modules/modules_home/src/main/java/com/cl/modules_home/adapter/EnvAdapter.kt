package com.cl.modules_home.adapter

import android.graphics.Color
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeEnvItemBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.EnvironmentInfoData
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.lxj.xpopup.XPopup

class EnvAdapter(data: MutableList<EnvironmentInfoData.Environment>?) :
    BaseQuickAdapter<EnvironmentInfoData.Environment, BaseViewHolder>(R.layout.home_env_item, data) {
    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<HomeEnvItemBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: EnvironmentInfoData.Environment) {
        // 获取 Binding
        val binding: HomeEnvItemBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
        helper.getView<ImageView>(R.id.iv_icon).background = when(item.environmentType) {
            EnvironmentInfoData.KEY_TYPE_HUMIDITY_TYPE -> ContextCompat.getDrawable(context, R.mipmap.home_humidity)
            EnvironmentInfoData.KEY_TYPE_WATER_TEMPERATURE_TYPE -> ContextCompat.getDrawable(context, R.mipmap.home_water_temperature)
            EnvironmentInfoData.KEY_TYPE_WATER_LEVEL_TYPE -> ContextCompat.getDrawable(context, R.mipmap.home_water_level)
            EnvironmentInfoData.KEY_TYPE_TEMPERATURE_TYPE -> ContextCompat.getDrawable(context, R.mipmap.home_temperature)
            else -> ContextCompat.getDrawable(context, R.mipmap.home_humidity)
        }
        helper.setText(R.id.tv_health, if (item.environmentType == EnvironmentInfoData.KEY_TYPE_WATER_LEVEL_TYPE) "" else item.healthStatus)
        helper.setTextColor(R.id.tv_health, getColor(item.environmentType, item.detectionValue, item.healthStatus))
        helper.setText(R.id.tv_going, if (item.environmentType == EnvironmentInfoData.KEY_TYPE_WATER_LEVEL_TYPE) item.healthStatus else temperatureConversion(item.value))
        helper.getView<ImageView>(R.id.iv_gt).apply {
            if (item.alert == 0) {
                setImageResource(com.cl.common_base.R.mipmap.base_gt)
            } else {
                setImageResource(com.cl.common_base.R.mipmap.base_error_gt)
            }
        }
        if (!item.roomData.isNullOrEmpty()) {
            if (item.value?.contains("%") == true) {
                helper.setText(R.id.tv_going_unit, "Room ${item.roomData}%")
            } else if (item.value?.contains("℉") == true) {
                val temp = com.cl.common_base.ext.temperatureConversion(item.roomData.safeToFloat(), isMetric)
                val tempUnit = if (isMetric) "℃" else "℉"
                helper.setText(R.id.tv_going_unit, "Room $temp$tempUnit")
            }
        }

        helper.getView<FrameLayout>(R.id.rl_edit).setSafeOnClickListener {
            XPopup.Builder(context)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(false)
                .asCustom(
                    BaseCenterPop(
                        context,
                        onConfirmAction = {
                            // 跳转到InterCome文章详情里面去
                            InterComeHelp.INSTANCE.openInterComeSpace(
                                space = InterComeHelp.InterComeSpace.Article,
                                id = item.articleId
                            )
                        },
                        confirmText = "Detail",
                        content = item.articleDetails,
                    )
                ).show()
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
                    val te = replace.safeToInt()
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
}