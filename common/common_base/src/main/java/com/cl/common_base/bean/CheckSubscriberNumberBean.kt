package com.cl.common_base.bean

import com.cl.common_base.BaseBean

/**
 * 订阅码检查
 */
data class CheckSubscriberNumberBean(
    var email: String? = null,
    var month: String? = null,
    var subscriberNumber: String? = null,
): BaseBean()