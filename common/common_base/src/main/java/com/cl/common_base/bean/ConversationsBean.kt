package com.cl.common_base.bean

data class ConversationsBean(
    val body: String,
    val conversation_id: String,
    val created_at: Int,
    val id: String,
    val message_type: String,
    val type: String
)