package com.cl.modules_my.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.modules_my.request.ModifyUserDetailReq
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityRetainedScoped
class MyRepository @Inject constructor(private var remoteRepository: MyRemoteRepository) {

    /**
     * 上传图片
     */
    fun uploadImg(body: List<MultipartBody.Part>): Flow<HttpResult<String>> {
        return remoteRepository.uploadImg(body)
    }

    /**
     * 更新用户信息
     */
    fun modifyUserDetail(body: ModifyUserDetailReq): Flow<HttpResult<Boolean>> {
        return remoteRepository.modifyUserDetail(body)
    }

    /**
     * 获取用户信息
     */
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return remoteRepository.userDetail()
    }


    /**
     * 删除设备
     */
    fun deleteDevice(): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deleteDevice()
    }

    /**
     * 删除植物
     */
    fun plantDelete(uuid: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.plantDelete(uuid)
    }

    /**
     * 是否种植
     */
    fun checkPlant(uuid: String): Flow<HttpResult<CheckPlantData>> {
        return remoteRepository.checkPlant(uuid)
    }

    /**
     * 更新app
     */
    fun getAppVersion(): Flow<HttpResult<AppVersionData>> {
        return remoteRepository.getAppVersion()
    }

    /**
     * 换水获取图文
     */
    fun advertising(type: String): Flow<HttpResult<MutableList<AdvertisingData>>> {
        return remoteRepository.advertising(type)
    }
}