package com.cl.modules_contact.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.service.HttpContactApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * 我的界面提供
 */
@ActivityRetainedScoped
class ContactRemoteRepository @Inject constructor() {
    private val service = ServiceCreators.create(HttpContactApiService::class.java)

    fun newPage(req: NewPageReq): Flow<HttpResult<NewPageData>> {
        return service.newPage(req)
    }

}