package com.cl.modules_contact.response

import com.cl.common_base.BaseBean

data class TagsBean(val number: String, var isSelected: Boolean = false) : BaseBean() {
}