package com.cl.common_base.pop

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.MyChooseTimePopBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit


/**
 * 选择时间弹窗
 */
class ChooseTimePop(
    context: Context,
    var turnOnText: String? = "turn on Night mode",
    var turnOffText: String? = "turn off Night mode",
    var isShowNightMode: Boolean = true,
    var turnOnHour: Int? = null,
    var turnOffHour: Int? = null,
    var isTheSpacingHours: Boolean = true, // 是否需要间隔12小时
    private var lightIntensity: Int? = null, // 当前灯光数值。
    private var isProMode: Boolean? = false, // 是否是proMode模式。
    private val proModeAction: ((onTime: String?, onMinute: String?, timeOn: Int?, timeOff: Int?, timeOpenHour: String?, timeCloseHour: String?, lightIntensity: Int?) -> Unit)? = null, // proMode下的选择
    private val onConfirmAction: ((onTime: String?, onMinute: String?, timeOn: Int?, timeOff: Int?, timeOpenHour: String?, timeCloseHour: String?) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_choose_time_pop
    }

    private val userInfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    private var binding: MyChooseTimePopBinding? = null
    private var isChanged: Boolean = false

    //   24小时制
    //    private var turnHour = turnOnHour
    //    private var turnHour = turnOffHour


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<MyChooseTimePopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }

            // 展示Seekbar
            ViewUtils.setVisible(isProMode == true, clLight)
            tvLightIntensityValue.text = lightIntensity.toString()
            lightIntensity?.toFloat()?.let { lightIntensitySeekbar.setProgress(it) }
            seekBarChang(lightIntensitySeekbar)

            ftTurnOn.setItemTitle(turnOnText ?: "turn on Night mode")
            ftTurnOff.setItemTitle(turnOffText ?: "turn off Night mode")
            ViewUtils.setVisible(isShowNightMode, tvNightModeTime)

            ftTurnOn.itemValue = turnOnHour?.let {
                if (it > 12) {
                    "${it - 12}:00 PM"
                } else if (it < 12) {
                    "${it}:00 AM"
                } else if (it == 12) {
                    "12:00 AM"
                } else {
                    "12:00 AM"
                }
            } ?: "12:00 AM"

            ftTurnOff.itemValue = turnOffHour?.let {
                if (it > 12) {
                    "${it - 12}:00 PM"
                } else if (it < 12) {
                    "${it}:00 AM"
                } else if (it == 12) {
                    "12:00 AM"
                } else {
                    "12:00 AM"
                }
            } ?: "12:00 PM"

            /*letMultiple(turnOnHour, turnOffHour) { onTime, offTime ->
                btnSuccess.isEnabled = (offTime - onTime) >= 12
            }*/

            ftTurnOn.setOnClickListener {
                // 时间开启
                XPopup.Builder(context)
                    .asCustom(TimePickerPop(context, onConfirmAction = { time, timeMis ->
                        val hour = if (time.safeToInt() == 0) 12 else time.safeToInt()
                        /*turnOffHour?.let {
                            if (it < 12) { // 表示是AM、也就是第二天
                                // val turnOff = format24Hour(24 + it, 0).toInt()
                                if (hour < 12) {
                                    btnSuccess.isEnabled = false
                                    ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                                    return@TimePickerPop
                                }
                                val is12Exceed = ((24 + it) - hour) < 12
                                if (is12Exceed) {
                                    btnSuccess.isEnabled = false
                                    ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                                    return@TimePickerPop
                                }
                            } else if (it > 12) { // 表示是PM
                                val is12Exceed = (it - hour) < 12
                                if (!is12Exceed) {
                                    btnSuccess.isEnabled = false
                                    ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                                    return@TimePickerPop
                                }
                            } else if (it == 12) {
                                val is12Exceed = (24 - hour) == 12
                                if (!is12Exceed) {
                                    btnSuccess.isEnabled = false
                                    ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                                    return@TimePickerPop
                                }
                            }
                        }*/

                        if (hour > 12) {
                            ftTurnOn.itemValue = "${hour - 12}:00 PM"
                        } else if (hour < 12) {
                            ftTurnOn.itemValue = "${hour}:00 AM"
                        } else if (hour == 12) {
                            ftTurnOn.itemValue = "12:00 AM"
                        }
                        btnSuccess.isEnabled = true
                        if (turnOnHour != hour && isProMode == true) {
                            isChanged = true
                        }
                        // 赋值给他
                        turnOnHour = hour
                    }, chooseTime = turnOnHour ?: 12))
                    .show()
            }
            ftTurnOff.setOnClickListener {
                // 时间关闭
                XPopup.Builder(context)
                    .asCustom(TimePickerPop(context, onConfirmAction = { time, timeMis ->
                        val hour = time.safeToInt()

                        /*turnOnHour?.let {
                            if (it < 12) {
                                val is12Exceed = (hour - it) < 12
                                if (!is12Exceed) {
                                    btnSuccess.isEnabled = false
                                    ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                                    return@TimePickerPop
                                }
                            } else if (it >= 12) {
                                val is12Exceed = (it - hour) < 12
                                if (!is12Exceed) {
                                    btnSuccess.isEnabled = false
                                    ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                                    return@TimePickerPop
                                }
                            }*//* else if (it == 12) {
                                val is12Exceed = hour - it < 0
                                if (is12Exceed) {
                                    ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                                    return@TimePickerPop
                                }
                            }*//*
                        }*/

                        if (hour > 12) {
                            ftTurnOff.itemValue = "${hour - 12}:00 PM"
                        } else if (hour < 12) {
                            ftTurnOff.itemValue = "${if (time.safeToInt() == 0) 12 else time.safeToInt()}:00 AM"
                        } else if (hour == 12) {
                            ftTurnOff.itemValue = "12:00 PM"
                        }
                        btnSuccess.isEnabled = true

                        // 赋值给他
                        val hours = if (time.safeToInt() == 0) 12 else time.safeToInt()
                        if (turnOffHour != hours && isProMode == true) {
                            isChanged = true
                        }
                        turnOffHour = if (time.safeToInt() == 0) 12 else time.safeToInt()
                    }, chooseTime = turnOffHour ?: 12))
                    .show()
            }
            btnSuccess.setOnClickListener {
                kotlin.runCatching {
                    logI("turnOnHour: $turnOnHour,,,turnOffHour: $turnOffHour")

                    if (isTheSpacingHours) {
                        if (turnOnHour == turnOffHour) {
                            ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                            return@setOnClickListener
                        }
                    }

                    if (isTheSpacingHours) {
                        // 计算时间是否大于12个小时
                        val now = LocalDateTime.now()
                        val turn = if (turnOnHour == 24) 0 else turnOnHour
                        val turnOff = if (turnOffHour == 24) 0 else turnOffHour
                        val start = LocalDateTime.of(now.year, now.month, now.dayOfMonth, turn ?: 0, 0) // 开始时间
                        var end = LocalDateTime.of(now.year, now.month, now.dayOfMonth, turnOff ?: 0, 0) // 结束时间

                        if (start > end) {
                            end = end.plusDays(1) // 如果结束时间小于开始时间，加一天
                        }

                        val duration = Duration.between(start, end) // 计算两个时间的差异
                        val hours = duration.toHours() // 转换为小时

                        logI("The difference is $hours hours.")
                        if (hours > 12) {
                            // 差距超过12小时
                            ToastUtil.shortShow("The time interval cannot be greater than 12 hours.")
                            return@setOnClickListener
                        }
                        if ((turnOffHour?.minus(turnOnHour ?: 0) ?: 0) <= 12) {
                            val timeOpenHour = turnOnHour?.let {
                                if (it > 12) {
                                    "$it:00 PM"
                                } else {
                                    "$it:00 AM"
                                }
                            }

                            val timeCloseHour = turnOffHour?.let {
                                if (it > 12) {
                                    "$it:00 PM"
                                } else {
                                    "$it:00 AM"
                                }
                            }
                            onConfirmAction?.invoke(ftTurnOn.itemValue.toString(), ftTurnOff.itemValue.toString(), turnOnHour, turnOffHour, timeOpenHour, timeCloseHour)
                            proModeAction?.invoke(ftTurnOn.itemValue.toString(), ftTurnOff.itemValue.toString(), turnOnHour, turnOffHour, timeOpenHour, timeCloseHour, lightIntensity)
                            /*if (isChanged && isProMode == true) {
                                // 表示不是默认的配置了。 已经改过了。
                                Prefs.putStringAsync(Constants.Global.KEY_LOAD_CONFIGURED, "-1")
                            }*/
                            dismiss()
                        } else {
                            ToastUtil.shortShow("The time interval cannot be less than 12 hours.")
                        }
                    } else {
                        val timeOpenHour = turnOnHour?.let {
                            if (it > 12) {
                                "$it:00 PM"
                            } else {
                                "$it:00 AM"
                            }
                        }

                        val timeCloseHour = turnOffHour?.let {
                            if (it > 12) {
                                "$it:00 PM"
                            } else {
                                "$it:00 AM"
                            }
                        }
                        onConfirmAction?.invoke(ftTurnOn.itemValue.toString(), ftTurnOff.itemValue.toString(), turnOnHour, turnOffHour, timeOpenHour, timeCloseHour)
                        proModeAction?.invoke(ftTurnOn.itemValue.toString(), ftTurnOff.itemValue.toString(), turnOnHour, turnOffHour, timeOpenHour, timeCloseHour, lightIntensity)
                        /*if (isChanged && isProMode == true) {
                            // 表示不是默认的配置了。 已经改过了。
                            Prefs.putStringAsync(Constants.Global.KEY_LOAD_CONFIGURED, "-1")
                        }*/
                        dismiss()
                    }
                }
            }
        }
    }

    private val pop by lazy {
        XPopup.Builder(context)
    }

    private fun seekBarChang(lightIntensitySeekbar: IndicatorSeekBar) {
        lightIntensitySeekbar.customSectionTrackColor { colorIntArr ->
            //the length of colorIntArray equals section count
            //                colorIntArr[0] = Color.parseColor("#008961");
            //                colorIntArr[1] = Color.parseColor("#008961");
            // 当刻度为最后4段时才显示红色
            // colorIntArr[6] = Color.parseColor("#F72E47")
            // colorIntArr[7] = Color.parseColor("#F72E47")
            colorIntArr[8] = Color.parseColor("#F72E47")
            colorIntArr[9] = Color.parseColor("#F72E47")
            true //true if apply color , otherwise no change
        }
        lightIntensitySeekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(p0: SeekParams?) {
            }

            override fun onStartTrackingTouch(p0: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekbar: IndicatorSeekBar?) {
                isChanged = true
                val progress = seekbar?.progress ?: 0
                val growLightValue = lightIntensity ?: 0
                // 应该只提示一次
                if (growLightValue <= 7 && progress > 7) {
                    pop.isDestroyOnDismiss(false).dismissOnTouchOutside(false)
                        .asCustom(context?.let {
                            BaseCenterPop(
                                it,
                                content = "Caution! Increasing the light intensity level above 7 may cause damage to the flowers. Are you sure you want to continue?",
                                cancelText = "No",
                                confirmText = "Yes",
                                onCancelAction = {
                                    // 需要恢复到之前到档位
                                    // mViewMode.setGrowLight("${mViewMode.getGrowLight.value}")
                                    lightIntensitySeekbar.setProgress(growLightValue.toFloat())
                                },
                                onConfirmAction = {
                                    // mViewMode.setGrowLight(seekbar?.progress.toString())
                                    lightIntensity = seekbar?.progress
                                    binding?.tvLightIntensityValue?.text = seekbar?.progress.toString()
                                }
                            )
                        }).show()
                } else {
                    // mViewMode.setGrowLight(seekbar?.progress.toString())
                    lightIntensity = seekbar?.progress
                    binding?.tvLightIntensityValue?.text = seekbar?.progress.toString()
                }
            }
        }
    }
}