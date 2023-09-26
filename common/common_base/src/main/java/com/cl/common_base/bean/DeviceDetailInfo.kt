package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class DeviceDetailInfo(
    var attribute: String? = null,
    var plantId: Int? = null,
    var plantName: String? = null,
    var plantWay: String? = null,
    var strainName: String? = null,
    var syncStrainName: String? = null,
    var syncType: String? = null,
    var isSyncStrainCheck: Boolean? = false,
    var isSyncTypeCheck: Boolean? = false
) : BaseBean()