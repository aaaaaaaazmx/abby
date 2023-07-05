package com.cl.modules_pairing_connection.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.modules_pairing_connection.repository.PairRepository
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SN修复
 *
 * @author 李志军 2022-08-19 17:05
 */
class PairFrontScanCodeViewModel @Inject constructor(private val repository: PairRepository) :
    ViewModel() {

    // 获取当前设备信息
    val tuYaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    /**
     * 检查用户SN是否正确
     */
    private val _checkSN = MutableLiveData<Resource<BaseBean>>()
    val checkSN: LiveData<Resource<BaseBean>> = _checkSN
    fun checkSN(deviceSn: String) = viewModelScope.launch {
        repository.checkSN(deviceSn)
            .map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code,
                        it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }
            .flowOn(Dispatchers.IO)
            .onStart {
                emit(Resource.Loading())
            }
            .catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1,
                        "$it"
                    )
                )
            }.collectLatest {
                _checkSN.value = it
            }
    }

    private val _SN = MutableLiveData<String>()
    val SN: LiveData<String> = _SN
    fun setSn(sn: String) {
        _SN.value = sn
    }

    fun getActivationStatus() {
        ThingHomeSdk.newDeviceInstance(tuYaDeviceBean?.devId)?.let {
            it.getDp(TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN, object : IResultCallback {
                override fun onError(code: String, error: String?) {
                    logI(
                        """
                        KEY_DEVICE_REPAIR_REST_STATUS: error
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    Reporter.reportTuYaError("newDeviceInstance", error, code)
                }

                override fun onSuccess() {
                    logI("sdasdas")
                }
            })
        }
    }
}