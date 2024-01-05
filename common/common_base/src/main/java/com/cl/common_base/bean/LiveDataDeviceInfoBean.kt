package com.cl.common_base.bean

import com.cl.common_base.BaseBean

/**
 * This is a short description.
 *
 * @author 李志军 2023-09-19 21:57
 */
data class LiveDataDeviceInfoBean(
    var deviceId: String?= null,
    var spaceType: String? = ListDeviceBean.KEY_SPACE_TYPE_BOX,
    var onlineStatus: String? = null,
): BaseBean()