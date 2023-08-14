package com.cl.common_base.ext

/**
 *  后台默认返回的是英制，不适用大部分情况
 */
fun temperatureConversion(value: Float, isMetric: Boolean, isUpload: Boolean): String {
    return if (isUpload) {
        if (isMetric) {
            val fahrenheit = value.times(9f).div(5f).plus(32)
            String.format("%.1f", fahrenheit) // 公制上传时转换为华氏
        } else {
            String.format("%.1f", value) // 英制上传不转换
        }
    } else {
        if (isMetric) {
            val celsius = (value.minus(32)).times(5f).div(9f)
            String.format("%.1f", celsius) // 公制获取后台数据时从华氏转换为摄氏
        } else {
            String.format("%.1f", value) // 英制获取不转换
        }
    }
}

fun unitsConversion(value: Float, isMetric: Boolean, isUpload: Boolean): String {
    return if (isUpload) {
        if (isMetric) {
            val inches = value.div(2.54f)
            String.format("%.1f", inches) // 公制上传时转换为英寸
        } else {
            String.format("%.1f", value) // 英制上传不转换
        }
    } else {
        if (isMetric) {
            val centimeters = value.times(2.54f)
            String.format("%.1f", centimeters) // 公制获取后台数据时从英寸转换为厘米
        } else {
            String.format("%.1f", value) // 英制获取不转换
        }
    }
}

fun weightConversion(value: Float, isMetric: Boolean, isUpload: Boolean): String {
    return if (isUpload) {
        if (isMetric) {
            val ounces = value.div(28.3495f)
            String.format("%.1f", ounces) // 公制上传时转换为盎司
        } else {
            String.format("%.1f", value) // 英制上传不转换
        }
    } else {
        if (isMetric) {
            val grams = value.times(28.3495f)
            String.format("%.1f", grams) // 公制获取后台数据时从盎司转换为克
        } else {
            String.format("%.1f", value) // 英制获取不转换
        }
    }
}