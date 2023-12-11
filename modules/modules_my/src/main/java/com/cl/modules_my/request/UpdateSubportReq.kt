package com.cl.modules_my.request

import com.cl.common_base.BaseBean

data class UpdateSubportReq(
    var accessoryId: String? = null,
    var subportParam: Req? = null,
): BaseBean() {
    data class Req(
        var accessoryDeviceId: String? = null,
        var portId: String? = null,
        var status: Boolean? = null,
        var subName: String? = null,
    ): BaseBean()
}