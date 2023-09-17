package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBinderMapper
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeDripPopBinding
import com.cl.common_base.bean.TrickData
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.service.HttpHomeApiService
import com.lxj.xpopup.core.CenterPopupView
import com.thingclips.smart.sdk.bean.DeviceBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class HomeDripPop(context: Context) : CenterPopupView(context) {

    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    val tuyaHomeBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    override fun getImplLayoutId(): Int {
        return R.layout.home_drip_pop
    }

    private var mBinding: HomeDripPopBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = DataBindingUtil.bind<HomeDripPopBinding>(popupImplView)
        lifecycleScope.launch {
            getTrickleIrrigationConfig(tuyaHomeBean?.devId.toString())
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
                            // 后台返回的是24小时、需要转换为12小时制度的数字

                            // {
                            //  "deviceId": "6c55de1155d3127455yqsa",
                            //  "turnOnSecond": 10,
                            //  "everyMinute": 10,
                            //  "everyStartTime": 6,
                            //  "everyEndTime": 18,
                            //  "status": false
                            //}

                            val startTime = data.everyStartTime ?: 0
                            val endTime = data.everyEndTime ?: 0

                            // 将12 AM和12 PM的情况单独处理
                            tvStart.text = if (startTime == 0) "12 AM" else "${if (startTime > 12) 24 - startTime else startTime} ${if (startTime > 12) "PM" else "AM"}"
                            tvEnd.text = if (endTime == 0) "12 AM" else "${if (endTime > 12) 24 - endTime else endTime} ${if (endTime > 12) "PM" else "AM"}"
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
                    it.data?.apply {

                    }
                }

                else -> {
                    ToastUtil.shortShow(it.errorMsg)
                }
            }
        }
    }
}