package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class CardInfo(
    val showType: String,
    val intercomId: String?,
    val content: String,
    val icon: String,
    val isEdit: Boolean,
    val logId: Int
):BaseBean() {
    companion object {
        // 卡片类型
        const val TYPE_LOG_CARD = "log_card"
        const val TYPE_ACTION_CARD = "action_card"
        const val TYPE_TRAINING_CARD = "training_card"
        const val TYPE_TIPS = "tips_card"
    }
}