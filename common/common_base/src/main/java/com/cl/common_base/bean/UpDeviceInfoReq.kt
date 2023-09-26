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
    var lightOn: String? = null,
    var lightOff: String? = null,
    var lightOnOff: String? = null,
    var deviceId: String?,
    var cupType: Int? = null, // (0-纸杯，1-塑料杯)
    var list: MutableList<DeviceDetailInfo>? = null,
    var plantId: Int? = null, // 植物ID， 主要用于在设备列表里面修改植物名称
    var proMode: String? = null, // 专业模式
    var fanAuto: Int? = null, // 风扇是否自动
    var burnOutProof: Int? = null, // 防烧模式是否开启
    var smartUsbPowder: Int? = null, // USB开关：1-开启、0-关闭
    var spaceName: String? = null,
    var spaceSize: String? = null,
    var numPlant: String? = null,
    var ledWattage: String? = null,
) : BaseBean() {

    /*data class InfoList(
        var day: String? = null,
        var journeyName: String? = null,
        var guideId: Int? = null,
        var journeyStatus: Int? = null,
        var week: String? = null,
    ) : com.joketng.timelinestepview.bean.BaseBean()*/
}