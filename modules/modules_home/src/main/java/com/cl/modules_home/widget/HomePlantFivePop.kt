package com.cl.modules_home.widget

import android.content.Context
import android.text.method.LinkMovementMethod
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePlantFivePopBinding
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.tuya.smart.sdk.bean.DeviceBean

/**
 * 开水下一步
 * plant5
 *
 * @author 李志军 2022-08-06 17:35
 */
class HomePlantFivePop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_five_pop
    }

    /**
     * 设备信息
     */
    val tuyaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    private val getDeviceDps by lazy {
        tuyaDeviceBean?.dps
    }

    // 水位
    private val _getWaterVolume =
        MutableLiveData(getDeviceDps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS).toString())
    val getWaterVolume: LiveData<String> = _getWaterVolume
    fun setWaterVolume(volume: String) {
        _getWaterVolume.value = volume
    }


    private var binding: HomePlantFivePopBinding? = null

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantFivePopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }
            btnSuccess.setOnClickListener {
                // 查看当前水位
                // Fill the water tank弹窗，Next按钮无当前水箱水位判断逻辑（水箱水位等于0时置灰按钮，点击按钮后弹出The current water level is too low. Add more water提示信息；
                // 水箱水位大于0时点亮按钮，点击后进入下一步）
                logI("getWaterVolume: ${getWaterVolume.value}")
                // todo 加水时不判断当前水箱水位了。
                onNextAction?.invoke()
                dismiss()
//                when (getWaterVolume.value) {
//                    "0L" -> {
//                        ToastUtil.shortShow("The current water level is too low. Add more water")
//                        return@setOnClickListener
//                    }
//                    else -> {
//                        onNextAction?.invoke()
//                        dismiss()
//                    }
//                }

            }
        }

        LiveEventBus.get().with(Constants.Tuya.KEY_TUYA_DEVICE_TO_APP, String::class.java)
            .observe(this) {
                val map = GSON.parseObject(it, Map::class.java)
                map?.forEach { (key, value) ->
                    when (key) {
                        TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_STATUS_INSTRUCTIONS -> {
                            setWaterVolume(value.toString())
                        }
                    }
                }
            }


    }
}