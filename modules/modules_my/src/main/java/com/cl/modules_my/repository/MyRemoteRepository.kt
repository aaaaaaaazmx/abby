package com.cl.modules_my.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.service.HttpMyApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * 我的界面提供
 */
@ActivityRetainedScoped
class MyRemoteRepository @Inject constructor() {
    private val service = ServiceCreators.create(HttpMyApiService::class.java)

    fun uploadImg(body: List<MultipartBody.Part>): Flow<HttpResult<String>> {
        return service.uploadImg(body)
    }

    fun modifyUserDetail(body: ModifyUserDetailReq): Flow<HttpResult<Boolean>> {
        return service.modifyUserDetail(body)
    }

    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return service.userDetail()
    }

    fun deleteDevice(): Flow<HttpResult<BaseBean>> {
        return service.deleteDevice()
    }

    fun plantDelete(uuid: String): Flow<HttpResult<Boolean>> {
        return service.plantDelete(uuid)
    }

    fun checkPlant(uuid: String): Flow<HttpResult<CheckPlantData>> {
        return service.checkPlant(uuid)
    }

    fun getAppVersion(): Flow<HttpResult<AppVersionData>> {
        return service.getAppVersion()
    }

    fun advertising(type: String): Flow<HttpResult<MutableList<AdvertisingData>>> {
        return service.advertising(type)
    }
}