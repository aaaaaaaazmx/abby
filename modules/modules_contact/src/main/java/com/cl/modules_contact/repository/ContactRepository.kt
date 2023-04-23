package com.cl.modules_contact.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.modules_contact.request.CommentByMomentReq
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.MomentsDetailsReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.PublishReq
import com.cl.modules_contact.request.ReplyReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.response.CommentDetailsData
import com.cl.modules_contact.response.MessageListData
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.response.PublishData
import com.cl.modules_contact.response.ReplyData
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

    fun messageList(req: NewPageReq): Flow<HttpResult<MutableList<MessageListData>>> {
        return remoteRepository.messageList(req)
    }

    fun getCommentByMomentId(req: CommentByMomentReq): Flow<HttpResult<MutableList<CommentByMomentData>>> {
        return remoteRepository.getCommentByMomentId(req)
    }

    fun getMomentsDetails(momentsId: Int): Flow<HttpResult<CommentDetailsData>> {
        return remoteRepository.getMomentsDetails(momentsId)
    }

    fun publish(req: PublishReq): Flow<HttpResult<PublishData>> {
        return remoteRepository.publish(req)
    }

    fun reply(req: ReplyReq): Flow<HttpResult<ReplyData>> {
        return remoteRepository.reply(req)
    }
}