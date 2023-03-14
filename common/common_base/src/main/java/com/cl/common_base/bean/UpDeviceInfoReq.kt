package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class UpDeviceInfoReq(
    var attribute: String? = null, // 种植属性(Photo、Auto)
    var strainName: String? = null, // 种植名字
    var plantName: String? = null, // 种植名字
    var plantWay: String? = null, // 种植方式
    var day: Int? = null,
    var flushingWeight: Int? = null, // 称重重量
    var healthStatus: String? = null,
    var name: String? = null,
    var heigh: Int? = null,
    var id: Int? = null,
    var oxygen: Int? = null,
    var nightMode: Int? = null, // 夜间模式
    var childLock: Int? = null, // 童锁
    var nightTimer: String? = null, // 夜间模式时间
    var plantStatus: Int? = null,
    var week: Int? = null,
    var deviceId: String? = null,
    var cupType: Int? = null, // (0-纸杯，1-塑料杯)
    var list: MutableList<InfoList>? = null,
    var plantId: Int? = null, // 植物ID， 主要用于在设备列表里面修改植物名称
) : BaseBean() {

    data class InfoList(
        var day: String? = null,
        var journeyName: String? = null,
        var guideId: Int? = null,
        var journeyStatus: Int? = null,
        var week: String? = null,
    ) : com.joketng.timelinestepview.bean.BaseBean()
}