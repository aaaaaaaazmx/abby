package com.cl.modules_planting_log.request

data class FieldAttributes(
    val description: String, // textView的文案
    val hintDescription: String, // 文本框的描述文案
    val unit: String, // 这是第四个参数
    val inputType: String, // 文本框的输入类型
    var isVisible: Boolean = true, // 是否显示当前条目，因为需求是选中特定条目之后，显示多个附加条目
)