package com.cl.common_base.bean

data class AutomationListBean(
    val accessoryId: Int? = null,
    var accessoryName: String? = null,
    var title: String? = null,
    var image: String? = null,
    var isDefault: Int? = null,
    var status: Int? = null,
    var list: MutableList<AutoBean>? = null,
) : com.cl.common_base.BaseBean() {
    data class AutoBean(
        val accessoryName:String? = null,
        val automationId: Int? = null,
        val describes: String? = null,
        val isDefault: Int? = null,
        val status: Int? = null,
    ): com.cl.common_base.BaseBean()
}