package com.cl.common_base.bean

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

@Keep
data class SnoozeReq(val taskId: String? = null, val taskNo: String? = null): BaseBean()