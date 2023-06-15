package com.cl.modules_login.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.modules_login.repository.BindSourceEmailReq
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


    @POST("abby/userDevice/listDevice")
    fun listDevice(): Flow<HttpResult<MutableList<ListDeviceBean>>>

    /**
     * 获取用户信息
     */
    @POST("abby/user/userDetail")
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>>

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

    /**
     * 获取InterCome同步信息
     */
    @POST("abby/user/intercomDataAttributeSync")
    fun intercomDataAttributeSync(): Flow<HttpResult<Map<String, Any>>>

    /**
     * 第三方登录绑定邮箱
     */
    @POST("abby/user/app/bindSourceEmail")
    fun bindSourceEmail(@Body requestBody: BindSourceEmailReq): Flow<HttpResult<BaseBean>>

    /**
     * 检查用户第三方登录邮箱是否绑定过
     */
    @FormUrlEncoded
    @POST("abby/user/app/checkUserExists")
    fun checkUserExists(@Field("email") email: String): Flow<HttpResult<Boolean>>

}