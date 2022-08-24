package com.cl.common_base.bean

import androidx.annotation.Keep

/**
 * @Description:
 */
@Keep
class HttpResult<T>(val data: T) : BaseBean()

