package com.cl.modules_planting_log.request

data class FieldAttributes(
    val description: String, // textView的文案
    val hintDescription: String, // 文本框的描述文案
    var unit: String, // 这是第四个参数
    val inputType: String, // 文本框的输入类型
    var isVisible: Boolean = true, // 是否显示当前条目，因为需求是选中特定条目之后，显示多个附加条目
    val metricUnits: String? = "", // 公制单位
    var imperialUnits : String? = "", // 英制单位
    var isShowRefreshIcon: Boolean = false, // 是否显示刷新按钮
    var isConnect: Boolean = false, // 是否连接
)