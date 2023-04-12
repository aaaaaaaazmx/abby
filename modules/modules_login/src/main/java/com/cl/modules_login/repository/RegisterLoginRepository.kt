package com.cl.modules_login.repository

import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.UserinfoBean
import com.cl.modules_login.request.*
import com.cl.modules_login.response.CountData
import com.cl.modules_login.response.LoginData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ActivityRetainedScoped
class RegisterLoginRepository @Inject constructor(private var remoteRepository: RegisterLoginRemoteRepository) {

    /**
     * 登录
     */
    fun loginAbby(body: LoginReq): Flow<HttpResult<LoginData>> {
        return remoteRepository.loginAbby(body)
    }


    /**
     * 设备列表
     */
    fun listDevice(): Flow<HttpResult<MutableList<ListDeviceBean>>> {
        return remoteRepository.listDevice()
    }

    /**
     * 获取国家列表
     */
    fun getCountList(): Flow<HttpResult<MutableList<CountData>>> {
        return remoteRepository.countList()
    }

    /**
     * 发送验证邮箱邮件
     */
    fun verifyEmail(email: String, type: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.verifyEmail(email, type)
    }

    /**
     * 发送验证邮箱邮件
     */
    fun verifyCode(code: String, email: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.verifyCode(code, email)
    }

    /**
     * 用户注册
     */
    fun registerAccount(body: UserRegisterReq): Flow<HttpResult<Boolean>> {
        return remoteRepository.registerAccount(body)
    }

    /**
     * 修改密码
     */
    fun updatePwd(body: UpdatePwdReq): Flow<HttpResult<Boolean>> {
        return remoteRepository.updatePwd(body)
    }

    /**
     * 检查用户是否种植过植物
     */
    fun checkPlant(body: String): Flow<HttpResult<CheckPlantData>> {
        return remoteRepository.checkPlant(body)
    }

    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return remoteRepository.userDetail()
    }
}