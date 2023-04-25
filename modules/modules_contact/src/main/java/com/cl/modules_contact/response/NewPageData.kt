package com.cl.modules_contact.response

import com.cl.common_base.BaseBean
import com.google.gson.annotations.SerializedName

data class NewPageData(
    val total: Int? = null,
    val pages: Int? = null,
    val size: Int? = null,
    val current: Int? = null,
    val optimizeCountSql: Boolean? = null,
    val hitCount: Boolean? = null,
    val searchCount: Boolean? = null,
    val countId: String? = null,
    val maxLimit: String? = null,
    val records: MutableList<Records>? = null,
    val orders: MutableList<Orders>? = null,
) : BaseBean() {
    data class Orders(
        val asc: Boolean? = null,
        val column: String? = null,
    ) : BaseBean()

    data class Records(
        val id: Int? = null,
        val userId: String? = null,
        val avatarPicture: String? = null,
        val learnMoreId: String? = null,
        val nickName: String? = null,
        val abbyId: String? = null,
        val content: String? = null,
        val week: String? = null,
        val day: String? = null,
        val journeyName: String? = null,
        val height: String? = null,
        val environment: String? = null,
        val syncTrend: String? = null,
        @set:JvmName("setIsFoo")
        var isPraise: Int? = null,
        val openData: Int? = null,
        var reward: Int? = null,
        var praise: Int? = null,
        val hot: String? = null,
        val isTop: String? = null,
        @set:JvmName("setIsFod")
        var isReward: Int? = null,
        var comment: Int? = null,
        val isComment: String? = null,
        val healthStatus: String? = null,
        val createTime: String? = null,
        val link: String? = null,
        val proMode: String? = null,
        val total: String? = null,
        val size: String? = null,
        val current: String? = null,
        val imageUrls: MutableList<ImageUrls>? = null,
        var comments: MutableList<Comments>? = null,
        val mentions: MutableList<Mentions>? = null,
    ) : BaseBean() {

        data class Mentions(
            val abbyId: String? = null,
            val nickName: String? = null,
            val picture: String? = null,
            val userId: String? = null,
        ): BaseBean()

        data class ImageUrls(
            val id: Int? = null,
            val trendId: Int? = null,
            val md5: String? = null,
            val isDeleted: Boolean? = null,
            val imageUrl: String? = null,
            val createTime: String? = null,
            val momentsId: String? = null,
            val sequence: String? = null,
            val thumbImageUrl: String? = null,
            val updateTime: String? = null,
        ) : BaseBean()

        data class Comments(
            val abbyId: String? = null,
            var comment: String? = null,
            val commentId: String? = null,
            var commentName: String? = null,
            val createTime: String? = null,
            val parentComentId: String? = null,
            val picture: String? = null,
            val isPraise: Int? = null,
            val isReward: Int? = null,
            val reward: Int? = null,
            val userId: String? = null,
            val praise: Int? = null,
            val replys: MutableList<Replys>? = null,
        ) : BaseBean() {
            data class Replys(
                val abbyId: String? = null,
                val comment: String? = null,
                val commentId: String? = null,
                val commentName: String? = null,
                val createTime: String? = null,
                val parentReplyId: String? = null,
                val userReplyId: String? = null,
                val parentComentId: String? = null,
                val picture: String? = null,
                val isPraise: Int? = null,
                val isReward: Int? = null,
                val reward: Int? = null,
                val userId: String? = null,
                val praise: Int? = null,
                val replyId: Int? = null,
            ) : BaseBean()
        }
    }
}