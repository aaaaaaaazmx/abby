package com.cl.modules_login.viewmodel

import androidx.lifecycle.*
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.repository.RegisterLoginRepository
import com.cl.modules_login.request.LoginReq
import com.cl.modules_login.response.LoginData
import com.tuya.smart.android.user.api.ILoginCallback
import com.tuya.smart.android.user.bean.User
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class LoginViewModel @Inject constructor(private val repository: RegisterLoginRepository) :
    ViewModel() {
    private val _registerLoginLiveData = MutableLiveData<Resource<LoginData>>()
    val registerLoginLiveData: LiveData<Resource<LoginData>> = _registerLoginLiveData

    private val _loginReq = MutableLiveData(LoginReq())
    val loginReq: LiveData<LoginReq> = _loginReq

    // 账号
    val account by lazy {
        Prefs.getString(Constants.Login.KEY_LOGIN_ACCOUNT)
    }

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
                            "$it"
                        )
                    )
                }.collectLatest {
                    _registerLoginLiveData.value = it
                }
        }
    }


    /**
     * 检查过是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    fun checkPlant(
        body: String
    ) = viewModelScope.launch {
        repository.checkPlant(body)
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
                _checkPlant.value = it
            }
    }


    /**
     * 涂鸦登录
     */
    fun tuYaLogin(
        code: String?,
        email: String?,
        password: String?,
        onSuccess: ((user: User?) -> Unit)? = null,
        onError: ((code: String?, error: String?) -> Unit)? = null,
        onRegisterReceiver: ((homeId: String?) -> Unit)? = null
    ) {
        TuyaHomeSdk.getUserInstance()
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
                        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(object :
                            ITuyaGetHomeListCallback {
                            override fun onSuccess(homeBeans: MutableList<HomeBean>?) {
                                logI("homeBeansList: ${homeBeans?.size}")

                                /**
                                 * getHomeDetail
                                 * 此方法必须调用，不然下发指令可能不成功
                                 */
                                homeBeans?.get(0)?.homeId?.let { homeId ->
                                    Prefs.putLongAsync(Constants.Tuya.KEY_HOME_ID, homeId)
                                    TuyaHomeSdk.newHomeInstance(homeId)
                                        .getHomeDetail(object : ITuyaHomeResultCallback {
                                            override fun onSuccess(bean: HomeBean?) {
                                                // 取数据
                                                bean?.let { homeBean ->
                                                    if (homeBean.deviceList.size == 0) {
                                                        // 跳转绑定界面
                                                        ARouter.getInstance()
                                                            .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                                                            .navigation()
                                                        return@let
                                                    }
                                                    kotlin.runCatching {
                                                        // 目前只允许绑定一个，那么只取第一个
                                                        val deviceBean = homeBean.deviceList[0]
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
                                                        onRegisterReceiver?.invoke(deviceBean.devId)
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

                                                        // 种植检查
                                                        user?.uid?.let { uid ->
                                                            checkPlant(uid)
                                                        }
                                                    }.onFailure { }
                                                }
                                            }

                                            override fun onError(
                                                errorCode: String?,
                                                errorMsg: String?
                                            ) {
                                                logE(
                                                    """
                                                    getHomeDetail:
                                                    errorCode -> $errorCode
                                                    errorMsg -> $errorMsg
                                                """.trimIndent()
                                                )
                                            }
                                        })
                                }
                            }

                            override fun onError(errorCode: String?, error: String?) {
                                // 查询当前家庭列表失败，也要进行下一步
                                // 种植检查
                                user?.uid?.let { uid ->
                                    checkPlant(uid)
                                }
                            }

                        })

                        onSuccess?.invoke(user)
                    }

                    override fun onError(code: String?, error: String?) {
                        logE("code: $code,, erroe: $error}")
                        error?.let { msg -> ToastUtil.shortShow(msg) }

                        onError?.invoke(code, error)
                    }
                }
            )
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}