package com.cl.common_base.bean

import com.cl.common_base.BaseBean
import com.google.gson.annotations.SerializedName

/**
 * 获取植物基本信息
 *
 * @author 李志军 2022-08-08 16:23
 */
data class PlantInfoData(
    var categoryCode: String? = null, // 植物标记码
    var attribute: String? = null, // 种植属性(Photo、Auto)
    var strainName: String? = null, // 种植名字
    var plantWay: String? = null, // 种植方式
    var spaceName: String? = null,// 帐篷名字
    var day: Int? = null,
    var flushingWeight: Int? = 0, // 称重重量
    var healthStatus: String? = null,
    var name: String? = null,
    var period: String? = null,
    var heigh: Int? = null,
    var id: Int? = null,
    var plantId: Int? = null,
    var lightOn: Int? = null,
    var lightOff: Int? = null,
    var lightOnOff: String? = null,
    var oxygen: Int? = null,
    var plantStatus: Int? = null,
    var week: Int? = null,
    var totalDay: Int? = null,
    var envirVO: envirVOData? = null,
    var lightingStatus: Int? = 1, // 0 关灯、1 开灯
    var list: MutableList<InfoList>? = null,
    var cupType: Int? = null, // 塑料杯、纸杯
    var plantName: String? = null,
    var harvestTime: String? = null,
    var templateId: String? = null,
    @SerializedName("timeRemaining")
    var germinationTime: String? = null, // 剩余发芽时间
) : BaseBean() {

    data class envirVOData(
        var roomTemp: String? = null,
        var roomHumiture: String? = null,
        var temp: String? = null,
        var humiture: String? = null,
    )
    data class InfoList(
        var day: String? = null,
        var journeyName: String? = null,
        var guideId: Int? = null,
        var guideType: String? = null,
        var taskTime: String? = null,
        var taskId: String? = null,
        var journeyStatus: Int? = null,
        var totalDay: Int? = null,
        var week: String? = null,
        var step: String? = null,
        var etaTime: String? = null,
        var unlockNow: Boolean? = null, // 是否解锁
    ) : com.joketng.timelinestepview.bean.BaseBean()
}