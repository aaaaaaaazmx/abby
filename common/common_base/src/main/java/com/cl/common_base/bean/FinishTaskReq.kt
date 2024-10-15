package com.cl.common_base.bean


/**
 * 解锁周期新接口请求类
 */
data class FinishTaskReq(
    val taskId: String? = null,
    val weight: String? = null,
    val packetNo: String? = null,
    val templateId: String? = null,
    val viewDatas: MutableList<ViewData>? = null
) : com.cl.common_base.BaseBean() {
    /**
     * 应该是为了称重，如果输入狂不输入时，那么默认为""空字符串
     */
    data class ViewData(
        var textId: String? = null,
        var dataArray: MutableList<String>? = null
    ): com.cl.common_base.BaseBean()
}