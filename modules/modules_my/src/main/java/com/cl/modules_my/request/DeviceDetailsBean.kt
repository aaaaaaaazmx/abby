package com.cl.modules_my.request

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.DeviceDetailInfo

data class DeviceDetailsBean(
    var deviceId: String?= null,
    var ledWattage: String?= null,
    var lightOff: String?= null,
    var lightOn: String?= null,
    var list: MutableList<DeviceDetailInfo>?= null,
    var numPlant: Int?= null,
    var spaceName: String?= null,
    var spaceSize: String?= null
) : BaseBean()