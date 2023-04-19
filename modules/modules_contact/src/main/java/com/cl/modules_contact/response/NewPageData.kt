package com.cl.modules_contact.response

import com.cl.common_base.BaseBean

data class NewPageData(
    var total: Int? = null,
    var pages: Int? = null,
    var size: Int? = null,
    var current: Int? = null,
    var optimizeCountSql: Boolean? = null,
    var hitCount: Boolean? = null,
    var searchCount: Boolean? = null,
    var countId: String? = null,
    var maxLimit: String? = null,
    var records: MutableList<Records>? = null,
    var orders: MutableList<Orders>? = null,
) : BaseBean() {
    data class Orders(
        var asc: Boolean? = null,
        var column: String? = null,
    ): BaseBean()

    data class Records(
        var id: Int? = null,
        var userId: Int? = null,
        var avatarPicture: String? = null,
        var learnMoreId: String? = null,
        var nickName: String? = null,
        var abbyId: String? = null,
        var content: String? = null,
        var week: String? = null,
        var day: String? = null,
        var journeyName: String? = null,
        var height: String? = null,
        var environment: String? = null,
        var syncTrend: String? = null,
        var isPraise: String? = null,
        var openData: String? = null,
        var reward: String? = null,
        var praise: String? = null,
        var hot: String? = null,
        var isTop: String? = null,
        var isReward: String? = null,
        var comment: String? = null,
        var isComment: String? = null,
        var healthStatus: String? = null,
        var createTime: String? = null,
        var link: String? = null,
        var proMode: String? = null,
        var total: String? = null,
        var size: String? = null,
        var current: String? = null,
        var imageUrls: MutableList<ImageUrls>? = null,
        var comments: MutableList<Comments>? = null,
    ): BaseBean()


    data class ImageUrls(
        var id: Int? = null,
        var trendId: Int? = null,
        var md5: String? = null,
        var isDeleted: Boolean? = null,
        var imageUrl: String? = null,
        var createTime: String? = null,
        var momentsId: String? = null,
        var sequence: String? = null,
        var thumbImageUrl: String? = null,
        var updateTime: String? = null,
    ): BaseBean()


    data class Comments(
        var abbyId: String? = null,
        var comment: String? = null,
        var commentId: String? = null,
        var commentName: String? = null,
        var createTime: String? = null,
        var parentComentId: String? = null,
        var picture: String? = null,
        var isPraise: Int? = null,
        var isReward: Int? = null,
        var reward: Int? = null,
        var userId: String? = null,
        var praise: Int? = null,
        var replys: MutableList<Replys>? = null,
    ): BaseBean()


    data class Replys(
        var abbyId: String? = null,
        var comment: String? = null,
        var commentId: String? = null,
        var commentName: String? = null,
        var createTime: String? = null,
        var parentReplyId: String? = null,
        var userReplyId: String? = null,
        var parentComentId: String? = null,
        var picture: String? = null,
        var isPraise: Int? = null,
        var isReward: Int? = null,
        var reward: Int? = null,
        var userId: String? = null,
        var praise: Int? = null,
        var replyId: Int? = null,
    ): BaseBean()
}