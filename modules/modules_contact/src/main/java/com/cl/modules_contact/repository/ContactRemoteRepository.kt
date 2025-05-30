package com.cl.modules_contact.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
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

    fun getTags(): Flow<HttpResult<MutableList<String>>> {
        return service.getTags()
    }

    fun like(req: LikeReq): Flow<HttpResult<BaseBean>> {
        return service.like(req)
    }

    fun unlike(req: LikeReq): Flow<HttpResult<BaseBean>> {
        return service.unlike(req)
    }

    fun delete(req: DeleteReq): Flow<HttpResult<BaseBean>> {
        return service.delete(req)
    }

    fun public(syncTrend: Int, momentId: String): Flow<HttpResult<BaseBean>> {
        return service.public(syncTrend, momentId)
    }

    fun report(req: ReportReq): Flow<HttpResult<BaseBean>> {
        return service.report(req)
    }

    fun messageList(req: NewPageReq): Flow<HttpResult<MutableList<MessageListData>>> {
        return service.messageList(req)
    }

    fun getCommentByMomentId(req: CommentByMomentReq): Flow<HttpResult<MutableList<CommentByMomentData>>> {
        return service.getCommentByMomentId(req)
    }

    fun getMomentsDetails(momentsId: Int): Flow<HttpResult<CommentDetailsData>> {
        return service.getMomentsDetails(momentsId)
    }

    fun publish(req: PublishReq): Flow<HttpResult<PublishData>> {
        return service.publish(req)
    }

    fun reply(req: ReplyReq): Flow<HttpResult<ReplyData>> {
        return service.reply(req)
    }

    fun reward(req: RewardReq): Flow<HttpResult<BaseBean>> {
        return service.reward(req)
    }

    fun uploadImages(body: List<MultipartBody.Part>): Flow<HttpResult<MutableList<String>>> {
        return service.uploadImages(body)
    }

    fun add(req: AddTrendReq): Flow<HttpResult<AddTrendData>> {
        return service.add(req)
    }

    fun deleteComment(commentId: String): Flow<HttpResult<BaseBean>> {
        return service.deleteComment(commentId)
    }

    fun deleteReply(replyId: String): Flow<HttpResult<BaseBean>> {
        return service.deleteReply(replyId)
    }

    fun myMoments(req: MyMomentsReq): Flow<HttpResult<NewPageData>> {
        return service.myMoments(req)
    }

    fun getOtherUserInfo(userId: String): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return service.getOtherUserInfo(userId)
    }

    fun wallpaperList(): Flow<HttpResult<MutableList<WallpaperListBean>>> {
        return service.wallpaperList()
    }

    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return service.userDetail()
    }

    fun automaticLogin(body: AutomaticLoginReq): Flow<HttpResult<AutomaticLoginData>> {
        return service.automaticLogin(body)
    }

    fun getTrendPicture(body: TrendPictureReq): Flow<HttpResult<TrendPictureData>> {
        return service.getTrendPicture(body)
    }

    fun updateFollowStatus(body: UpdateFollowStatusReq): Flow<HttpResult<BaseBean>> {
        return service.updateFollowStatus(body)
    }

    fun getDigitalAsset(body: DigitalAsset): Flow<HttpResult<DigitalAssetData>> {
        return service.getDigitalAsset(body)
    }


    fun follower(): Flow<HttpResult<MutableList<FolowerData>>> {
        return service.follower()
    }

    fun following(): Flow<HttpResult<MutableList<FolowerData>>> {
        return service.following()
    }

    fun hotReduce(momentId: String): Flow<HttpResult<BaseBean>> {
        return service.hotReduce(momentId)
    }
}