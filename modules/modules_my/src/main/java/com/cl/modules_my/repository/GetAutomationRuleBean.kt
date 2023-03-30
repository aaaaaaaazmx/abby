package com.cl.modules_my.repository

import com.cl.common_base.BaseBean


data class GetAutomationRuleBean(
    var accessoryId: String? = null,
    var accessoryName: String? = null,
    var automationId: String? = null,
    var deviceId: String? = null,
    var iocn: String? = null,
    var thenDescribe: String? = null,
    var status: Int? = null,
    var list: MutableList<AutomationRuleListBean>? = null,
):BaseBean() {
    data class AutomationRuleListBean(
        var operator: String? = null,
        var type: String? = null,
        var value: Int? = null,

    ): BaseBean()
}