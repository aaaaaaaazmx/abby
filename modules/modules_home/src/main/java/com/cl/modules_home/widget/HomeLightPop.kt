package com.cl.modules_home.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeLightPopBinding
import com.cl.common_base.bean.TrickData
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.TimePickerPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.service.HttpHomeApiService
import com.lxj.xpopup.core.CenterPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class HomeLightPop(
    context: Context,
    val deviceId: String,
    val onTime: Int,
    val onOffTime: Int,
    val onConfirmAction: ((turnOnTime: Int, turnOffTime: Int) -> Unit)? = null
) : CenterPopupView(context) {
    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    override fun getImplLayoutId(): Int {
        return R.layout.home_light_pop
    }

    private var turnOnHour: Int? = null
    private var turnOffHour: Int? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeLightPopBinding>(popupImplView)?.apply {

            val startTime = onTime.safeToInt()
            val endTime = onOffTime.safeToInt()

            // 将12 AM和12 PM的情况单独处理
            tvStart.text =
                if (startTime == 0) "12 AM" else if (startTime == 24) "12 PM" else "${if (startTime > 12) startTime - 12 else startTime} ${if (startTime > 12) "PM" else "AM"}"
            turnOnHour =
                if (startTime == 0) 12 else startTime
            tvEnd.text =
                if (endTime == 0) "12 AM" else if (endTime == 24) "12 PM" else "${if (endTime > 12) endTime - 12 else endTime} ${if (endTime > 12) "PM" else "AM"}"
            turnOffHour =
                if (endTime == 0) 12 else endTime


            tvStart.setOnClickListener {
                // 时间开启
                xpopup(context) {
                    asCustom(
                        TimePickerPop(context, onConfirmAction = { time, timeMis ->
                            runCatching {
                                // 返回的是24小时制度。
                                val hour = if (time.toInt() == 0) 12 else time.toInt()

                                if (hour > 12) {
                                    tvStart.text = "${hour - 12}:00 PM"
                                } else if (hour < 12) {
                                    tvStart.text = "${hour}:00 AM"
                                } else if (hour == 12) {
                                    tvStart.text = "12:00 AM"
                                }
                                turnOnHour = time.toInt()
                            }

                        }, chooseTime = turnOnHour ?: 12)
                    ).show()
                }
            }


            tvEnd.setOnClickListener {
                xpopup(context) {
                    asCustom(
                        TimePickerPop(context, onConfirmAction = { time, timeMis ->
                            runCatching {
                                val hour = if (time.toInt() == 0) 12 else time.toInt()
                                if (hour > 12) {
                                    tvEnd.text = "${hour - 12}:00 PM"
                                } else if (hour < 12) {
                                    tvEnd.text = "${hour}:00 AM"
                                } else if (hour == 12) {
                                    tvEnd.text = "12:00 PM"
                                }
                                // 赋值给他
                                turnOffHour = time.toInt()
                            }
                        }, chooseTime = turnOffHour ?: 12)
                    ).show()
                }
            }

            tvConfirm.setOnClickListener {
                // 点击确认
                val startTime = tvStart.text.toString()
                val endTime = tvEnd.text.toString()

                if (startTime.isEmpty()) {
                    ToastUtil.shortShow("Please enter the light start value.")
                    return@setOnClickListener
                }
                if (endTime.isEmpty()) {
                    ToastUtil.shortShow("Please enter the light end value.")
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    upDeviceInfo(
                        UpDeviceInfoReq(
                            deviceId = deviceId,
                            lightOn = turnOnHour.toString(),
                            lightOff = turnOffHour.toString(),
                            lightOnOff = "$turnOnHour+$turnOffHour"
                        )
                    )
                }
            }
        }
    }


    /**
     * 更新设备信息
     */
    private suspend fun upDeviceInfo(req: UpDeviceInfoReq) {
        service.updateDeviceInfo(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            logI(it.toString())
            when (it) {
                is Resource.Success -> {
                    onConfirmAction?.invoke(turnOnHour ?: 0, turnOffHour ?: 0)
                    dismiss()
                }
                is Resource.DataError -> {
                    ToastUtil.shortShow(it.errorMsg)
                }

                else -> {}
            }
        }
    }

}