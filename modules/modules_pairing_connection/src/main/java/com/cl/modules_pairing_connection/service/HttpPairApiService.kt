package com.cl.modules_pairing_connection.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.UserinfoBean
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

/**
 * Pair 配网接口
 *
 * @author 李志军 2022-08-04 10:27
 */
interface HttpPairApiService {

    @FormUrlEncoded
    @POST("abby/userDevice/bindDevice")
    fun bindDevice(
        @Field("deviceId") deviceId: String,
        @Field("deviceUuid") deviceUuid: String
    ): Flow<HttpResult<String>>

    /**
     * 是否种植
     */
    @FormUrlEncoded
    @POST("abby/plant/check")
    fun checkPlant(@Field("deviceUuid") body: String): Flow<HttpResult<CheckPlantData>>

    /**
     * 获取用户信息
     */
    @POST("abby/user/userDetail")
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>>


    @FormUrlEncoded
    @POST("abby/userDevice/checkSN")
    fun checkSN(@Field("deviceSn") deviceSn: String): Flow<HttpResult<BaseBean>>
}