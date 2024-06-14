package com.cl.modules_contact.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AutomaticLoginData
import com.cl.common_base.bean.AutomaticLoginReq
import com.cl.common_base.bean.DigitalAsset
import com.cl.common_base.bean.DigitalAssetData
import com.cl.common_base.bean.FolowerData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.bean.WallpaperListBean
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
import com.cl.common_base.bean.UpdateFollowStatusReq
import com.cl.modules_contact.request.TrendPictureReq
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.response.CommentDetailsData
import com.cl.modules_contact.response.MentionData
import com.cl.modules_contact.response.MessageListData
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.response.PublishData
import com.cl.modules_contact.response.ReplyData
import com.cl.modules_contact.response.TrendPictureData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * 登录注册忘记密码接口地址
 */
interface HttpContactApiService {

    // 获取最新动态
    @POST("abby/moments/newPage")
    fun newPage(
        @Body requestBody: NewPageReq,
    ): Flow<HttpResult<NewPageData>>

    /**
     * 获取标签
     */
    @POST("abby/moments/getTags")
    fun getTags(): Flow<HttpResult<MutableList<String>>>

    /**
     * 点赞
     */
    @POST("abby/moments/like")
    fun like(
        @Body requestBody: LikeReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 取消点赞
     */
    @POST("abby/moments/dislike")
    fun unlike(
        @Body requestBody: LikeReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 删除动态
     */
    @POST("abby/moments/delete")
    fun delete(
        @Body requestBody: DeleteReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 公开动态
     */
    @FormUrlEncoded
    @POST("abby/moments/syncTrend")
    fun public(
        @Field("syncTrend") syncTrend: Int, @Field("momentId") momentId: String,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 举报动态
     */
    @POST("abby/moments/reportAdd")
    fun report(
        @Body requestBody: ReportReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 获取消息列表
     */
    @POST("abby/comment/getCommentMessageList")
    fun messageList(
        @Body requestBody: NewPageReq,
    ): Flow<HttpResult<MutableList<MessageListData>>>

    /**
     * 获取评论列表
     */
    @POST("abby/comment/getCommentByMomentId")
    fun getCommentByMomentId(@Body requestBody: CommentByMomentReq): Flow<HttpResult<MutableList<CommentByMomentData>>>

    /**
     * 获取动态详情
     */
    @FormUrlEncoded
    @POST("abby/moments/getMomentsDetails")
    fun getMomentsDetails(@Field("momentsId") momentsId: Int): Flow<HttpResult<CommentDetailsData>>

    /**
     * 发布评论
     */
    @POST("abby/comment/publish")
    fun publish(@Body requestBody: PublishReq): Flow<HttpResult<PublishData>>

    /**
     * 回复评论
     */
    @POST("abby/comment/reply")
    fun reply(@Body requestBody: ReplyReq): Flow<HttpResult<ReplyData>>

    /**
     * 打赏
     */
    @POST("abby/moments/reward")
    fun reward(@Body requestBody: RewardReq): Flow<HttpResult<BaseBean>>

    /**
     * 获取@人的列表
     */
    @FormUrlEncoded
    @POST("abby/moments/getMentionList")
    fun getMentionList(@Field("searchName") searchName: String): Flow<HttpResult<MutableList<MentionData>>>

    /**
     * 新增动态
     */
    @POST("abby/moments/add")
    fun add(@Body requestBody: AddTrendReq): Flow<HttpResult<AddTrendData>>

    /**
     * 上传图片多张
     */
    @Multipart
    @POST("abby/base/uploadImgs")
    fun uploadImages(@Part partLis: List<MultipartBody.Part>): Flow<HttpResult<MutableList<String>>>

    /**
     * 删除评论
     */
    @FormUrlEncoded
    @POST("abby/comment/delete")
    fun deleteComment(@Field("commentId") commentId: String): Flow<HttpResult<BaseBean>>

    /**
     * 删除回复
     */
    @FormUrlEncoded
    @POST("abby/comment/deleteReply")
    fun deleteReply(@Field("replyId") replyId: String): Flow<HttpResult<BaseBean>>

    /**
     * 我的动态
     */
    @POST("abby/moments/myPage")
    fun myMoments(@Body requestBody: MyMomentsReq): Flow<HttpResult<NewPageData>>

    /**
     * 获取他人信息
     */
    @FormUrlEncoded
    @POST("abby/user/otherUserDetail")
    fun getOtherUserInfo(@Field("userId") userId: String): Flow<HttpResult<UserinfoBean.BasicUserBean>>

    /**
     * 获取壁纸列表
     */
    @POST("abby/user/getWallList")
    fun wallpaperList(): Flow<HttpResult<MutableList<WallpaperListBean>>>

    /**
     * 获取用户信息
     */
    @POST("abby/user/userDetail")
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>>

    @POST("abby/user/app/automaticLogin")
    fun automaticLogin(
        @Body requestBody: AutomaticLoginReq,
    ): Flow<HttpResult<AutomaticLoginData>>

    /**
     * 获取Trend历史照片
     */
    @POST("abby/moments/getTrendPicture")
    fun getTrendPicture(
        @Body requestBody: TrendPictureReq,
    ): Flow<HttpResult<TrendPictureData>>

    /**
     * 修改关注状态
     */
    @POST("abby/moments/updateFollowStatus")
    fun updateFollowStatus(
        @Body requestBody: UpdateFollowStatusReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 获取资产
     */
    @POST("abby/digitalAsset/homePage")
    fun getDigitalAsset(@Body body: DigitalAsset): Flow<HttpResult<DigitalAssetData>>

    // 被关注列表 abby/user/follower
    @POST("abby/user/follower")
    fun follower(): Flow<HttpResult<MutableList<FolowerData>>>

    // 关注列表 abby/user/following
    @POST("abby/user/following")
    fun following(): Flow<HttpResult<MutableList<FolowerData>>>

}