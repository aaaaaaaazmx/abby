package com.cl.modules_my.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.repository.MyRepository
import com.tuya.smart.android.device.bean.UpgradeInfoBean
import com.tuya.smart.android.user.bean.User
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IGetOtaInfoCallback
import com.tuya.smart.sdk.api.IOtaListener
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.smart.sdk.bean.OTAErrorMessageBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 固件升级
 *
 * @author 李志军 2022-08-16 17:42
 */

@ActivityRetainedScoped
class FirmwareUpdateViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {
    // 获取当前设备信息
    private val tuYaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    private val tuYaHomeSdk by lazy {
        TuyaHomeSdk.newOTAInstance(tuYaDeviceBean?.devId)
    }

    private val _upgradeInfoBeans = MutableLiveData<MutableList<UpgradeInfoBean>>()
    val upgradeInfoBeans: LiveData<MutableList<UpgradeInfoBean>> = _upgradeInfoBeans

    private val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    /**
     * 查询固件升级信息
     */
    fun checkFirmwareUpdateInfo(
        onOtaInfo: ((upgradeInfoBeans: MutableList<UpgradeInfoBean>?, isShow: Boolean) -> Unit)? = null,
    ) {
        tuYaHomeSdk.getOtaInfo(object : IGetOtaInfoCallback {
            override fun onSuccess(upgradeInfoBeans: MutableList<UpgradeInfoBean>?) {
                logI("getOtaInfo:  ${GSON.toJson(upgradeInfoBeans?.firstOrNull { it.type == 9 })}")
                _upgradeInfoBeans.value = upgradeInfoBeans
                // 如果可以升级
                if (hasHardwareUpdate(upgradeInfoBeans)) {
                    onOtaInfo?.invoke(upgradeInfoBeans, true)
                } else {
                    // 如果不可以升级过
                    onOtaInfo?.invoke(upgradeInfoBeans, false)
                }
            }

            override fun onFailure(code: String?, error: String?) {
                logI(
                    """
                        getOtaInfo:
                        code: $code
                        error: $error
                    """.trimIndent()
                )
                Reporter.reportTuYaError("getOtaInfo", error, code)
            }
        })
    }

    /**
     * 开始升级
     */
    fun startOta() {
        tuYaHomeSdk.startOta()
    }


    /**
     * 结束升级
     * iTuyaOta.onDestory();
     */
    fun stopOta() {
//        tuYaHomeSdk.onDestroy()
    }

    /**
     * 固件升级监听
     */
    fun setOtaListener(
        onSuccess: (() -> Unit)? = null,
        onFailure: ((otaType: Int, code: String?, error: String?) -> Unit)? = null,
        onFailureWithText: ((
            otaType: Int,
            code: String?,
            messageBean: OTAErrorMessageBean?
        ) -> Unit)? = null,
        onProgress: ((otaType: Int, progress: Int) -> Unit)? = null,
        onTimeOut: (() -> Unit)? = null
    ) {
        tuYaHomeSdk.setOtaListener(object : IOtaListener {
            override fun onSuccess(otaType: Int) {
                onSuccess?.invoke()
                stopOta()
            }

            override fun onFailure(otaType: Int, code: String?, error: String?) {
                logE(
                    """
                    setOtaListener:
                    otaType: $otaType
                    code: $code
                    error: $error
                """.trimIndent()
                )
                Reporter.reportTuYaError("setOtaListener", error, code)
                onFailure?.invoke(otaType, code, error)
                stopOta()
            }

            override fun onFailureWithText(
                otaType: Int,
                code: String?,
                messageBean: OTAErrorMessageBean?
            ) {
                onFailureWithText?.invoke(otaType, code, messageBean)
                stopOta()
                logE(
                    """
                    setOtaListener:
                    upgrade failure, errorCode = 
                    code: $code
                    errorMessage = ${messageBean?.text}
                """.trimIndent()
                )
            }

            override fun onProgress(otaType: Int, progress: Int) {
                logI(
                    """
                    setOtaListener:    
                    otaType: $otaType
                    progress: $progress
                """.trimIndent()
                )
                onProgress?.invoke(otaType, progress)
            }

            override fun onTimeout(otaType: Int) {
                logE(
                    """
                    onTimeout:
                    otaType: $otaType
                """.trimIndent()
                )
                onTimeOut?.invoke()
                stopOta()
            }

            override fun onStatusChanged(otaStatus: Int, otaType: Int) {
                // 3.23.0 版本新增，针对于低功耗类设备会通过该方法返回设备等待唤醒状态。此时 otaStatus 参数返回 5

            }
        })
    }

    /**
     * 检查固件是否可以升级
     */
    private fun hasHardwareUpdate(list: MutableList<UpgradeInfoBean>?): Boolean {
        if (null == list || list.size == 0) return false
        return list.firstOrNull { it.type == 9 }?.upgradeStatus == 1
    }


    // 删除设备
    fun delete() {
        TuyaHomeSdk.newDeviceInstance(tuYaDeviceBean?.devId)
            .removeDevice(object : IResultCallback {
                override fun onError(code: String?, error: String?) {
                    logE(
                        """
                        removeDevice:
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    ToastUtil.shortShow(error)
                    Reporter.reportTuYaError("newDeviceInstance", error, code)
                    // 调用接口请求删除设备
                    deleteDevice(tuYaDeviceBean?.devId.toString())
                }

                override fun onSuccess() {
                    // 调用接口请求删除设备
                    deleteDevice(tuYaDeviceBean?.devId.toString())
                }
            })
    }


    /**
     * 删除用户设备
     */
    private val _deleteDevice = MutableLiveData<Resource<BaseBean>>()
    val deleteDevice: LiveData<Resource<BaseBean>> = _deleteDevice
    private fun deleteDevice(devId: String) = viewModelScope.launch {
        repository.deleteDevice(devId)
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
                _deleteDevice.value = it
            }
    }
}