package com.cl.modules_contact.request

import com.cl.common_base.BaseBean

data class RewardReq(
    val learnMoreId: String? = null,
    val momentsId: String? = null,
    val oxygenNum: String? = null,
    val relationId: String? = null,
    val type: String? = null,
    val userName: String? = null,
    ) : BaseBean()
