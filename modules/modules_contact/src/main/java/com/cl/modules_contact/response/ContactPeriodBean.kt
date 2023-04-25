package com.cl.modules_contact.response

import com.cl.common_base.BaseBean

data class ContactPeriodBean(val period: String, var isSelected: Boolean = false) : BaseBean() {
}