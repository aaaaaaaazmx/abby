package com.cl.common_base.ext

/**
 *  后台默认返回的是英制，不适用大部分情况
 */
fun temperatureConversion(value: Float, isMetric: Boolean, isUpload: Boolean): String {
    val result = if (isUpload) {
        if (isMetric) value.times(9f).div(5f).plus(32) else value
    } else {
        if (isMetric) (value.minus(32)).times(5f).div(9f) else value
    }
    return if (result == 0f) "" else String.format("%.1f", result)
}

fun unitsConversion(value: Float, isMetric: Boolean, isUpload: Boolean): String {
    val result = if (isUpload) {
        if (isMetric) value.div(2.54f) else value
    } else {
        if (isMetric) value.times(2.54f) else value
    }
    return if (result == 0f) "" else String.format("%.1f", result)
}

fun weightConversion(value: Float, isMetric: Boolean, isUpload: Boolean): String {
    val result = if (isUpload) {
        if (isMetric) value.div(28.3495f) else value
    } else {
        if (isMetric) value.times(28.3495f) else value
    }
    return if (result == 0f) "" else String.format("%.1f", result)
}


fun gallonConversion(value: Float, isMetric: Boolean, isUpload: Boolean): String {
    val result = if (isUpload) {
        if (isMetric) {
            value.times(3.78541f) // 公制上传时转换为升
        } else {
            value // 英制上传不转换
        }
    } else {
        if (isMetric) {
            value.div(3.78541f) // 公制获取后台数据时从升转换为加仑
        } else {
            value // 英制获取不转换
        }
    }
    return if (result == 0f) "" else String.format("%.1f", result)
}

// 后台默认返回的是英制
// 温度转换，英制to华氏度
fun temperatureConversion(value: Float, isMetric: Boolean): String {
    val result = if (isMetric) value.times(9f).div(5f).plus(32) else value
    return if (result == 0f) "" else result.safeToInt().toString()
}
