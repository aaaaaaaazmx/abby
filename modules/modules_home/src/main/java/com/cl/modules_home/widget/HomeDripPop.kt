package com.cl.modules_home.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputFilter
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBinderMapper
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeDripPopBinding
import com.cl.common_base.bean.TrickData
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.TimePickerPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.service.HttpHomeApiService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.thingclips.smart.sdk.bean.DeviceBean
import io.intercom.android.sdk.Intercom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.jvm.internal.Intrinsics.Kotlin
import kotlin.math.min

class HomeDripPop(context: Context) : CenterPopupView(context) {

    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    override fun getImplLayoutId(): Int {
        return R.layout.home_drip_pop
    }

    private var turnOnHour: Int? = null
    private var turnOffHour: Int? = null

    private var mBinding: HomeDripPopBinding? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = DataBindingUtil.bind<HomeDripPopBinding>(popupImplView)?.apply {
            tvAirPumpDesc.setOnClickListener {
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_DRIP)
            }
            tvSeconds.setOnClickListener {
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_DRIP)
            }
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
                                turnOnHour = hour
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
                                    tvEnd.text = "12:00 AM"
                                }
                                // 赋值给他
                                turnOffHour = hour
                            }
                        }, chooseTime = turnOffHour ?: 12)
                    ).show()
                }
            }

            etTurnTime.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    // 当焦点失去时，进行10-120的判断
                    runCatching {
                        val inputValue = etTurnTime.text.toString().toIntOrNull()
                        if (inputValue != null) {
                            if (inputValue < 5) {
                                etTurnTime.setText("5")
                                ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_2421))
                            } else if (inputValue > 30) {
                                etTurnTime.setText("30")
                                ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_2421))
                            }
                        }
                    }
                }
            }

            etTurnMin.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    // 当焦点失去时，进行10-120的判断
                    runCatching {
                        val inputValue = etTurnMin.text.toString().toIntOrNull()
                        if (inputValue != null) {
                            if (inputValue < 10) {
                                etTurnMin.setText("10")
                                ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_2422))
                            } else if (inputValue > 120) {
                                etTurnMin.setText("120")
                                ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_2422))                            }
                        }
                    }
                }
            }
            tvConfirm.setOnClickListener {
                // 点击确认
                val turnTime = etTurnTime.text.toString()
                val mins = etTurnMin.text.toString()
                val startTime = tvStart.text.toString()
                val endTime = tvEnd.text.toString()

                val turnValue = runCatching { turnTime.toInt() }.getOrDefault(0)
                if (turnValue < 5 || turnValue > 30) {
                    ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_1428))
                    return@setOnClickListener
                }
                if (turnTime.isEmpty()) {
                    ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_1429))
                    return@setOnClickListener
                }
                val minsValue = runCatching { mins.toInt() }.getOrDefault(0)
                if (minsValue < 10 || minsValue > 120) {
                    ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_1430))
                    return@setOnClickListener
                }

                if (mins.isEmpty()) {
                    ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_1431))
                    return@setOnClickListener
                }
                if (startTime.isEmpty()) {
                    ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_1432))
                    return@setOnClickListener
                }
                if (endTime.isEmpty()) {
                    ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.string_1433))
                    return@setOnClickListener
                }


                lifecycleScope.launch {
                    trickleIrrigationConfig(
                        TrickData(
                            deviceId = userInfo?.deviceId,
                            everyStartTime = if (turnOnHour == 24) 12 else if (turnOnHour == 12) 0 else turnOnHour,
                            everyEndTime = if (turnOffHour == 24) 12 else if (turnOffHour == 12) 0 else turnOffHour,
                            turnOnSecond = turnTime,
                            everyMinute = mins,
                            status = fisItemSwitch.isChecked
                        )
                    )
                }
            }

        }
        lifecycleScope.launch {
            getTrickleIrrigationConfig(userInfo?.deviceId ?: "")
        }
    }


    /**
     * 获取滴灌参数
     */
    private suspend fun getTrickleIrrigationConfig(deviceId: String) {
        service.getTrickleIrrigationConfig(deviceId).map {
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
                    it.data?.let { data ->
                        mBinding?.apply {
                            etTurnTime.setText(data.turnOnSecond.toString())
                            etTurnMin.setText(data.everyMinute.toString())
                            fisItemSwitch.isChecked = data.status == true
                            // 后台返回的是24小时、需要转换为12小时制度的数字

                            // {
                            //  "deviceId": "6c55de1155d3127455yqsa",
                            //  "turnOnSecond": 10,
                            //  "everyMinute": 10,
                            //  "everyStartTime": 6,
                            //  "everyEndTime": 18,
                            //  "status": false
                            //}

                            val startTime = when (data.everyStartTime) {
                                0 -> 12
                                12 -> 24
                                else -> data.everyStartTime ?: 12
                            }

                            val endTime = when (data.everyEndTime) {
                                0 -> 12
                                12 -> 24
                                else -> data.everyEndTime ?: 12
                            }

                            // 将12 AM和12 PM的情况单独处理
                            tvStart.text =
                                if (startTime == 0) "12 AM" else if (startTime == 24) "12 PM" else "${if (startTime > 12) startTime -12 else startTime} ${if (startTime > 12) "PM" else "AM"}"
                            turnOnHour = startTime
                            tvEnd.text =
                                if (endTime == 0) "12 AM" else if (endTime == 24) "12 PM" else "${if (endTime > 12) endTime - 12 else endTime} ${if (endTime > 12) "PM" else "AM"}"
                            turnOffHour = endTime
                        }
                    }
                }

                else -> {
                    ToastUtil.shortShow(it.errorMsg)
                }
            }
        }
    }


    /**
     * 上传滴灌参数配置
     */
    private suspend fun trickleIrrigationConfig(data: TrickData) {
        service.trickleIrrigationConfig(data).map {
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
                    this@HomeDripPop.dismiss()
                }

                else -> {
                    ToastUtil.shortShow(it.errorMsg)
                }
            }
        }
    }
}