package com.cl.common_base.service

import com.cl.common_base.bean.*
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

interface BaseApiService {
    /**
     * 获取名字
     */
    @FormUrlEncoded
    @POST("abby/plant/getStrainName")
    fun getStrainName(
        @Field("strainName") strainName: String
    ): Flow<HttpResult<MutableList<String>>>
}