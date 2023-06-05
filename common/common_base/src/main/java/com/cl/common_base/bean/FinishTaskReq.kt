package com.cl.common_base.bean


/**
 * 解锁周期新接口请求类
 */
data class FinishTaskReq(
    val taskId: String? = null,
    val weight: String? = null,
    val packetNo: String? = null,
) : BaseBean()