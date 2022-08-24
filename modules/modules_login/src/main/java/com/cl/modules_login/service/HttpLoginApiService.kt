package com.cl.modules_login.service

import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.modules_login.request.*
import com.cl.modules_login.response.CountData
import com.cl.modules_login.response.LoginData
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

/**
 * 登录注册忘记密码接口地址
 */
interface HttpLoginApiService {
    /**
     * 登录
     * /abby/user/app/login
     */
    @POST("abby/user/app/login")
    fun loginAbby(
        @Body requestBody: LoginReq,
    ): Flow<HttpResult<LoginData>>


    /**
     * 获取国家列表
     */
    @GET("abby/base/nation")
    fun getCountList(): Flow<HttpResult<MutableList<CountData>>>

    /**
     * 发送验证码
     */
    @GET("abby/user/verify/email")
    fun verifyEmail(@Query("email") email: String, @Query("type") type: String): Flow<HttpResult<Boolean>>


    /**
     * 邮箱验证码
     */
    @GET("abby/user/verify/code")
    fun verifyCode(@Query("code") code: String, @Query("email") email: String): Flow<HttpResult<Boolean>>

    /**
     * 用户注册
     */
    @POST("abby/user/app/register")
    fun registerAccount(@Body requestBody: UserRegisterReq): Flow<HttpResult<Boolean>>

    /**
     * 忘记密码
     */
    @POST("abby/user/app/updatePwd")
    fun updatePwd(@Body requestBody: UpdatePwdReq): Flow<HttpResult<Boolean>>

    /**
     * 是否种植
     */
    @FormUrlEncoded
    @POST("abby/plant/check")
    fun checkPlant(@Field("deviceUuid") body: String): Flow<HttpResult<CheckPlantData>>

}