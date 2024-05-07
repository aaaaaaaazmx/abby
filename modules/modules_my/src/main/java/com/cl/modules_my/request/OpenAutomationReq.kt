package com.cl.modules_my.request

import com.cl.common_base.BaseBean

data class OpenAutomationReq(
    val accessoryId: String? = null,
    val status: Int? = null,
    val automationId: String? = null,
    val deviceId: String? = null,
    val usbPort: String? = null,
): BaseBean()