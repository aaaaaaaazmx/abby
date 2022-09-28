package com.cl.modules_my.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.service.HttpMyApiService
import dagger.Reusable
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

    fun updatePlantInfo(body: UpPlantInfoReq): Flow<HttpResult<BaseBean>> {
        return service.updatePlantInfo(body)
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

    fun getGuideInfo(type: String): Flow<HttpResult<GuideInfoData>> {
        return service.getGuideInfo(type)
    }

    fun troubleShooting(): Flow<HttpResult<MyTroubleData>> {
        return service.troubleShooting()
    }

    fun howTo(): Flow<HttpResult<MutableList<MyTroubleData.Bean>>> {
        return service.howTo()
    }

    fun getDetailByLearnMoreId(learnMoreId: String): Flow<HttpResult<DetailByLearnMoreIdData>> {
        return service.getDetailByLearnMoreId(learnMoreId)
    }

    fun getCalendar(startDate: String, endDate: String): Flow<HttpResult<MutableList<CalendarData>>> {
        return service.getCalendar(startDate, endDate)
    }

    fun updateTask(body: UpdateReq): Flow<HttpResult<BaseBean>> {
        return service.updateTask(body)
    }

    fun unlockJourney(name: String, weight: String? = null): Flow<HttpResult<BaseBean>> {
        return service.unlockJourney(name, weight)
    }

    fun finishTask(taskId: String, weight: String? = null): Flow<HttpResult<BaseBean>> {
        return service.finishTask(taskId, weight)
    }

    fun deviceOperateStart(businessId: String, type: String): Flow<HttpResult<BaseBean>> {
        return service.deviceOperateStart(businessId, type)
    }

    fun deviceOperateFinish(type:String): Flow<HttpResult<BaseBean>> {
        return service.deviceOperateFinish(type)
    }


}