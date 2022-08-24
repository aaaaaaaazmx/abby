package com.cl.modules_login.repository

import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_login.request.*
import com.cl.modules_login.response.CountData
import com.cl.modules_login.response.LoginData
import com.cl.modules_login.service.HttpLoginApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 登录注册忘记密码提供
 */
@ActivityRetainedScoped
class RegisterLoginRemoteRepository @Inject constructor() {
    private val service = ServiceCreators.create(HttpLoginApiService::class.java)

    fun loginAbby(body: LoginReq): Flow<HttpResult<LoginData>> {
        return service.loginAbby(body)
    }

    fun countList(): Flow<HttpResult<MutableList<CountData>>> {
        return service.getCountList()
    }

    fun verifyEmail(email: String, type: String): Flow<HttpResult<Boolean>> {
        return service.verifyEmail(email, type)
    }

    fun verifyCode(code: String, email: String): Flow<HttpResult<Boolean>> {
        return service.verifyCode(code, email)
    }

    fun registerAccount(body: UserRegisterReq): Flow<HttpResult<Boolean>> {
        return service.registerAccount(body)
    }

    fun updatePwd(body: UpdatePwdReq): Flow<HttpResult<Boolean>> {
        return service.updatePwd(body)
    }

    fun checkPlant(body: String): Flow<HttpResult<CheckPlantData>> {
        return service.checkPlant(body)
    }
}