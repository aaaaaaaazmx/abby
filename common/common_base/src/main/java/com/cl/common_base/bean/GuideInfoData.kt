package com.cl.common_base.bean

import androidx.annotation.Keep
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean

/**
 * 图文引导
 *
 * @author 李志军 2022-08-06 12:56
 */
@Keep
data class GuideInfoData(
    var items: MutableList<PlantInfo>? = null,  // 引导描述详情
    var title: String? = null,  //	标题
    var type: Int? = null, // 引导类型:0-种植、1-开始种植、2-开始花期、3-开始清洗期、5-开始烘干期、6-完成种植、8-seed
) : BaseBean() {
    @Keep
    data class PlantInfo(
        var explain: String? = null,
        var extend: Extend? = null,
        var picture: String? = null,
        var title: String? = null,
        var isCheck: Boolean? = false,
        var isCurrentStatus: Int? = 0
    ) : BaseBean(), MultiItemEntity {
        // 返回当前布局状态
        override val itemType: Int
            get() = isCurrentStatus!!
    }

    @Keep
    data class Extend(
        val width: Int? = null,
        val height: Int? = null,
    ) : BaseBean()
}