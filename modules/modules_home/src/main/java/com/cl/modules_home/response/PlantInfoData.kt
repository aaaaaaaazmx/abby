package com.cl.modules_home.response

import com.cl.common_base.BaseBean

/**
 * 获取植物基本信息
 *
 * @author 李志军 2022-08-08 16:23
 */
data class PlantInfoData(
    var day: Int? = null,
    var flushingWeight: Int? = null,
    var healthStatus: String? = null,
    var name: String? = null,
    var heigh: Int? = null,
    var id: Int? = null,
    var oxygen: Int? = null,
    var plantStatus: Int? = null,
    var week: Int? = null,
    var list: MutableList<InfoList>? = null
) : BaseBean() {

    data class InfoList(
        var day: String? = null,
        var journeyName: String? = null,
        var guideId: Int? = null,
        var journeyStatus: Int? = null,
        var week: String? = null,
    ) : com.joketng.timelinestepview.bean.BaseBean()
}