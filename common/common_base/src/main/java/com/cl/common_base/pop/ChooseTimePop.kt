package com.cl.common_base.pop

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.MyChooseTimePopBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
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
    private val onConfirmAction: ((onTime: String?, onMinute: String?, timeOn: Int?, timeOff: Int?, timeOpenHour: String?, timeCloseHour: String?) -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_choose_time_pop
    }

    private val userInfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    private var binding: MyChooseTimePopBinding? = null

    //   24小时制
    //    private var turnHour = turnOnHour
    //    private var turnHour = turnOffHour


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<MyChooseTimePopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }

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
                        val hour = if (time.toInt() == 0) 12 else time.toInt()
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
                        // 赋值给他
                        turnOnHour = hour
                    }, chooseTime = turnOnHour ?: 12))
                    .show()
            }
            ftTurnOff.setOnClickListener {
                // 时间关闭
                XPopup.Builder(context)
                    .asCustom(TimePickerPop(context, onConfirmAction = { time, timeMis ->
                        val hour = time.toInt()

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
                            ftTurnOff.itemValue = "${if (time.toInt() == 0) 12 else time.toInt()}:00 AM"
                        } else if (hour == 12) {
                            ftTurnOff.itemValue = "12:00 PM"
                        }
                        btnSuccess.isEnabled = true
                        // 赋值给他
                        turnOffHour =  if (time.toInt() == 0) 12 else time.toInt()
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
                        dismiss()
                    }
                }
            }
        }
    }
}