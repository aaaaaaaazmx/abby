package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class OpenUsbReq(
    val deviceId: String? = null,
    val usbPort: String? = null,
    val accessoryId: String? = null,
) : BaseBean()