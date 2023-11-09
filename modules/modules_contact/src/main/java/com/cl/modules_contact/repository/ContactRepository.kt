package com.cl.modules_contact.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.modules_contact.request.AddTrendData
import com.cl.modules_contact.request.AddTrendReq
import com.cl.modules_contact.request.CommentByMomentReq
import com.cl.modules_contact.request.DeleteReq
import com.cl.common_base.bean.LikeReq
import com.cl.modules_contact.request.MyMomentsReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.PublishReq
import com.cl.modules_contact.request.ReplyReq
import com.cl.modules_contact.request.ReportReq
import com.cl.common_base.bean.RewardReq
import com.cl.modules_contact.request.TrendPictureReq
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.response.CommentDetailsData
import com.cl.modules_contact.response.MessageListData
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.response.PublishData
import com.cl.modules_contact.response.ReplyData
import com.cl.modules_contact.response.TrendPictureData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityRetainedScoped
class ContactRepository @Inject constructor(private var remoteRepository: ContactRemoteRepository) {

    fun newPage(req: NewPageReq): Flow<HttpResult<NewPageData>> {
        return remoteRepository.newPage(req)
    }

    fun getTags(): Flow<HttpResult<MutableList<String>>> {
        return remoteRepository.getTags()
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

    fun public(syncTrend: Int, momentId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.public(syncTrend, momentId)
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

    fun reward(req: RewardReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.reward(req)
    }

    fun uploadImg(body: List<MultipartBody.Part>): Flow<HttpResult<MutableList<String>>> {
        return remoteRepository.uploadImages(body)
    }

    fun add(req: AddTrendReq): Flow<HttpResult<AddTrendData>> {
        return remoteRepository.add(req)
    }


    fun deleteComment(commentId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deleteComment(commentId)
    }

    fun deleteReply(replyId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deleteReply(replyId)
    }

    fun myMoments(req: MyMomentsReq): Flow<HttpResult<NewPageData>> {
        return remoteRepository.myMoments(req)
    }

    fun getOtherUserInfo(userId: String): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return remoteRepository.getOtherUserInfo(userId)
    }

    fun wallpaperList(): Flow<HttpResult<MutableList<WallpaperListBean>>> {
        return remoteRepository.wallpaperList()
    }

    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return remoteRepository.userDetail()
    }

    fun automaticLogin(body: AutomaticLoginReq): Flow<HttpResult<AutomaticLoginData>> {
        return remoteRepository.automaticLogin(body)
    }

    fun getTrendPicture(body: TrendPictureReq): Flow<HttpResult<TrendPictureData>> {
        return remoteRepository.getTrendPicture(body)
    }

    fun updateFollowStatus(body: UpdateFollowStatusReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateFollowStatus(body)
    }
}