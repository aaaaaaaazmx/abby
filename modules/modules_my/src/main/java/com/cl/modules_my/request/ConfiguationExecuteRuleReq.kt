package com.cl.modules_my.request

import com.cl.common_base.BaseBean
import com.cl.modules_my.repository.GetAutomationRuleBean

data class ConfiguationExecuteRuleReq(
    val accessoryId: String? = null,
    val accessoryName: String? = null,
    val automationId: String? = null,
    val deviceId: String? = null,
    val iocn: String? = null,
    val status: Int? = null,
    val portId: String? = null,
    val thenDescribe: String? = null,
    val usbPort: String? = null,
    val list: MutableList<GetAutomationRuleBean.AutomationRuleListBean>? = null,
): BaseBean()