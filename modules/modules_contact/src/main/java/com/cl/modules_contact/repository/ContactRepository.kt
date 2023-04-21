package com.cl.modules_contact.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.NewPageData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityRetainedScoped
class ContactRepository @Inject constructor(private var remoteRepository: ContactRemoteRepository) {

    fun newPage(req: NewPageReq): Flow<HttpResult<NewPageData>> {
        return remoteRepository.newPage(req)
    }

    fun like(req: LikeReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.like(req)
    }

    fun unlike(req: LikeReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.unlike(req)
    }
    fun delete(req: DeleteReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.delete(req)
    }

    fun public(req: SyncTrendReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.public(req)
    }

    fun report(req: ReportReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.report(req)
    }
}