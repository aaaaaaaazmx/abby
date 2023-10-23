package com.cl.modules_login.viewmodel

import androidx.lifecycle.*
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.request.BindSourceEmailReq
import com.cl.modules_login.repository.RegisterLoginRepository
import com.cl.modules_login.request.LoginReq
import com.cl.modules_login.response.LoginData
import com.thingclips.smart.android.user.api.ILoginCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class VerifyEmailViewModel @Inject constructor(private val repository: RegisterLoginRepository) :
    ViewModel() {

    private val _loginReq = MutableLiveData(
        LoginReq(
            mobileModel = AppUtil.deviceModel,
            mobileBrand = AppUtil.deviceBrand,
            version = AppUtil.appVersionName,
            osType = "1",
            timeZone = DateHelper.getTimeZOneNumber().toString()
        )
    )
    val loginReq: LiveData<LoginReq> = _loginReq

    private val _isVerifySuccess = MutableLiveData<Resource<Boolean>>()
    val isVerifySuccess: LiveData<Resource<Boolean>> = _isVerifySuccess



    private val _registerLoginLiveData = MutableLiveData<Resource<LoginData>>()
    val registerLoginLiveData: LiveData<Resource<LoginData>> = _registerLoginLiveData
    /**
     * 登录
     */
    fun login() {
        viewModelScope.launch {
            repository.loginAbby(_loginReq.value!!)
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
                            "${it.message}"
                        )
                    )
                }.collectLatest { data ->
                    _registerLoginLiveData.value = data
                }
        }
    }


    /**
     * 涂鸦登录
     */
    fun tuYaLogin(
        map: Map<String, Any>? = null,
        interComeUserId: String?,
        userInfo: UserinfoBean.BasicUserBean?,
        deviceId: String?,
        code: String?,
        email: String?,
        password: String?,
        onSuccess: ((user: User?) -> Unit)? = null,
        onError: ((code: String?, error: String?) -> Unit)? = null,
        onRegisterReceiver: ((homeId: String?) -> Unit)? = null
    ) {
        // 登录InterCome
        InterComeHelp.INSTANCE.successfulLogin(
            map = map,
            interComeUserId = interComeUserId,
            userInfo = userInfo
        )
        ThingHomeSdk.getUserInstance()
            .loginWithEmail(
                code,
                email,
                password,
                object : ILoginCallback {
                    override fun onSuccess(user: User?) {
                        // 缓存涂鸦用户信息类
                        // 缓存的目的是为了下一次接口报错做准备
                        GSON.toJson(user)
                            ?.let { Prefs.putStringAsync(Constants.Tuya.KEY_DEVICE_USER, it) }


                        // 加入当前家庭
                        ThingHomeSdk.getHomeManagerInstance().queryHomeList(object :
                            IThingGetHomeListCallback {
                            override fun onSuccess(homeBeans: MutableList<HomeBean>?) {
                                logI("homeBeansList: ${homeBeans?.size}")

                                /**
                                 * getHomeDetail
                                 * 此方法必须调用，不然下发指令可能不成功
                                 */
                                if ((homeBeans?.size ?: 0) > 0) {
                                    homeBeans?.get(0)?.homeId?.let { homeId ->
                                        Prefs.putLongAsync(Constants.Tuya.KEY_HOME_ID, homeId)
                                        ThingHomeSdk.newHomeInstance(homeId)
                                            .getHomeDetail(object : IThingHomeResultCallback {
                                                override fun onSuccess(bean: HomeBean?) {
                                                    logI("DeviceListSize: ${bean?.deviceList?.size}")
                                                    // 取数据
                                                    bean?.let { homeBean ->
                                                        if (homeBean.deviceList.size == 0 || deviceId.isNullOrEmpty()) {
                                                            // 种植检查
                                                             checkPlant()
                                                            // 跳转绑定界面
                                                            //                                                        ARouter.getInstance()
                                                            //                                                            .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                                                            //                                                            .navigation()
                                                            return@let
                                                        }
                                                        kotlin.runCatching {
                                                            // 根据返回deviceId来判断 目前会有多台设备选择
                                                            homeBean.deviceList.firstOrNull { it.devId == deviceId }
                                                                ?.apply {
                                                                    val deviceBean = this
                                                                    // 缓存用户第一个设备数据
                                                                    // 只取第一个
                                                                    // 缓存的目的是为了下一次接口报错做准备
                                                                    GSON.toJson(deviceBean)?.let {
                                                                        Prefs.putStringAsync(
                                                                            Constants.Tuya.KEY_DEVICE_DATA,
                                                                            it
                                                                        )
                                                                    }
                                                                    // 注册广播
                                                                    onRegisterReceiver?.invoke(
                                                                        deviceBean.devId
                                                                    )
                                                                    logI(
                                                                        """
                                                                        login:
                                                                        DEVICE_BEAN_DPS: ${
                                                                            GSON.toJson(
                                                                                deviceBean.dps
                                                                            )
                                                                        }
                                                                    """.trimIndent()
                                                                    )
                                                                }

                                                            /**
                                                             * 从IOS-Android时本地缓存不一致的问题
                                                             */
                                                            /*if (homeBean.deviceList.firstOrNull { it.devId == deviceId } == null) {
                                                            *//*
                                                             * 调用设备列表来查看当前选中的是哪一台
                                                             *  来更新设备列表
                                                             *//*

                                                            // 清除缓存数据
                                                            Prefs.removeKey(Constants.Login.KEY_LOGIN_DATA_TOKEN)
                                                            // 清除上面所有的Activity
                                                            // 跳转到Login页面
                                                            ARouter.getInstance()
                                                                .build(RouterPath.LoginRegister.PAGE_LOGIN)
                                                                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                .navigation()
                                                            getListDevice(user, onRegisterReceiver)
                                                            return@let
                                                        }*/

                                                            // 上面的不能这么写
                                                            // 1、如果上面的ID和涂鸦返回的ID不一致，那么为Null，就会跳转到登录页面，这是理想情况下，在数量和涂鸦的设备数量一致的情况。
                                                            // 2、但是涂鸦的设备情况只取决于在线，如果设备离线，涂鸦那边是获取不到的，但是用户App会有状态表示是离线的。所以并不能判断id是否一致的情况。还是得直接跳转到主页面，
                                                            // 3、最终的显示结果还是由后台那边返回为准。故取消掉上面的跳转到登录界面的操作。

                                                            // 种植检查
                                                             checkPlant()
                                                        }.onFailure { }
                                                    }
                                                }

                                                override fun onError(
                                                    errorCode: String,
                                                    errorMsg: String?
                                                ) {
                                                    logE(
                                                        """
                                                    getHomeDetail:
                                                    errorCode -> $errorCode
                                                    errorMsg -> $errorMsg
                                                """.trimIndent()
                                                    )
                                                    Reporter.reportTuYaError(
                                                        "newHomeInstance",
                                                        errorMsg,
                                                        errorCode
                                                    )
                                                    ARouter.getInstance()
                                                        .build(RouterPath.LoginRegister.PAGE_LOGIN)
                                                        .navigation()
                                                }
                                            })
                                    }
                                }
                            }

                            override fun onError(errorCode: String, error: String?) {
                                // 查询当前家庭列表失败，也要进行下一步
                                // 种植检查
                                 checkPlant()
                                Reporter.reportTuYaError("getHomeManagerInstance", error, errorCode)
                                ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                                    .navigation()
                            }

                        })

                        onSuccess?.invoke(user)
                    }

                    override fun onError(code: String, error: String) {
                        logE("code: $code,, erroe: $error}")
                        error.let { msg -> ToastUtil.shortShow(msg) }
                        Reporter.reportTuYaError("getUserInstance", error, code)
                        onError?.invoke(code, error)
                        ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                            .navigation()
                    }
                }
            )
    }

    /**
     * 检查过是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    fun checkPlant() = viewModelScope.launch {
        repository.checkPlant()
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
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _checkPlant.value = it
            }
    }


    /**
     * 验证验证码
     */
    fun verifyCode(code: String, email: String) =
        viewModelScope.launch {
            repository.verifyCode(code, email)
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
                    logD("verifyEmail: catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _isVerifySuccess.value = it
                }
        }

    /**
     * 忘记密码,发送邮箱验证码
     */
    private val _updatePwds = MutableLiveData<Resource<Boolean>>()
    val updatePwds: LiveData<Resource<Boolean>> = _updatePwds
    fun updatePwd(email: String, type: String) =
        viewModelScope.launch {
            repository.verifyEmail(email, type)
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
                    logD("registerAccount: catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _updatePwds.value = it
                }
        }

    /**
     * 发送验证码
     */
    private val _sendStates = MutableLiveData<Resource<Boolean>>()
    val sendStates: LiveData<Resource<Boolean>> = _sendStates
    fun verifyEmail(email: String, type: String) =
        viewModelScope.launch {
            repository.verifyEmail(email, type)
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
                    logD("verifyEmail: catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _sendStates.value = it
                }
        }

    /**
     * 第三方登录，查看此邮箱是否已经被绑定过
     */
    private val _isBindEmail = MutableLiveData<Resource<Boolean>>()
    val isBindEmail: LiveData<Resource<Boolean>> = _isBindEmail
    fun isBindSourceEmail(email: String) =
        viewModelScope.launch {
            repository.checkUserExists(email)
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
                    logD("verifyEmail: catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _isBindEmail.value = it
                }
        }


    /**
     * 第三方登录，绑定邮箱
     */
    private val _bindEmail = MutableLiveData<Resource<Boolean>>()
    val bindEmail: LiveData<Resource<Boolean>> = _bindEmail
    fun bindSourceEmail(email: BindSourceEmailReq) =
        viewModelScope.launch {
            repository.bindSourceEmail(email)
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
                    logD("verifyEmail: catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _bindEmail.value = it
                }
        }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}