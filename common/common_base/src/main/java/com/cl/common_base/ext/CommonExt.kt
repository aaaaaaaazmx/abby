package com.cl.common_base.ext

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode


fun Any?.toStringOrEmpty(): String {
    return this?.toString() ?: ""
}

fun Any?.safeToBigDecimal(): BigDecimal {
    return if (this.isCanToBigDecimal()) {
        kotlin.runCatching {
            BigDecimal("$this")
        }.getOrElse { BigDecimal.ZERO }
    } else {
        BigDecimal.ZERO
    }
}

fun Any?.safeToInt(): Int {
    return this?.let {
        if (this is String) {
            this.safeToBigDecimal().toInt()
        } else {
            this.toString().safeToBigDecimal().toInt()
        }
    } ?: 0
}

fun Any?.safeToDouble(): Double {
    return this?.let {
        if (this is String) {
            this.safeToBigDecimal().toDouble()
        } else {
            this.toString().safeToBigDecimal().toDouble()
        }
    } ?: 0.0
}

// toFloat
fun Any?.safeToFloat(): Float {
    return this?.let {
        if (this is String) {
            this.safeToBigDecimal().toFloat()
        } else {
            this.toString().safeToBigDecimal().toFloat()
        }
    } ?: 0f
}

fun Any?.safeToLong(): Long {
    return this?.let {
        if (this is String) {
            this.safeToBigDecimal().toLong()
        } else {
            this.toString().safeToBigDecimal().toLong()
        }
    } ?: 0L
}


/**
 * 将对象转化为String, 若对象转化失败则返回空字符串, 若对象为小数, 则去除小数末位的0.
 */
fun Any?.toStringNotZeroOrEmpty(): String {
    if (this.isCanToBigDecimal()) {
        val decimal = BigDecimal(this.toString())
        if (decimal == -decimal) {
            return "0"
        }
        return decimal.stripTrailingZeros().toPlainString()
    }
    return this?.toString() ?: ""
}

/**
 * 将对象转化为String, 若对象转化失败则返回默认值[default], 若对象为小数, 则去除小数末位的0.
 */
fun Any?.toStringNotZeroOrElse(default: String): String {
    if (this.isCanToBigDecimal()) {
        val decimal = BigDecimal(this.toString())
        if (decimal == -decimal) {
            return "0"
        }
        return decimal.stripTrailingZeros().toPlainString()
    }
    return this?.toString() ?: default
}

/** 是否能转化为BigDecimal. */
fun Any?.isCanToBigDecimal(): Boolean {
    return when (this) {
        is BigInteger -> true
        is CharArray -> true
        is Double -> true
        is Int -> true
        is Long -> true
        is String -> true
        is Float -> true
        else -> false
    }
}

/**
 * 输出价钱样式的字符串，去掉多余的0，默认最多输出2位小数, 截断第三位小数
 */
@JvmOverloads
fun BigDecimal.toPriceString(
    scale: Int = 2,
    roundingMode: RoundingMode = RoundingMode.DOWN
): String = setScale(scale, roundingMode).stripTrailingZeros().toPlainString()

/** 对2个参数判空. */
inline fun <T1 : Any, T2 : Any, R : Any> letMultiple(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

/** 对2个参数判空. */
inline fun <T1 : Any, R : Any> Any?.let(p1: T1?, block: (Any, T1) -> R?): R? {
    return if (this != null && p1 != null) block(this, p1) else null
}

/** 对3个参数判空. */
inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> letMultiple(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

/** 对3个参数判空. */
inline fun <T1 : Any, T2 : Any, R : Any> Any?.let(p1: T1?, p2: T2?, block: (Any, T1, T2) -> R?): R? {
    return if (this != null && p1 != null && p2 != null) block(this, p1, p2) else null
}

/** 对4个参数判空. */
inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> letMultiple(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}

/** 对4个参数判空. */
inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> Any?.let(p1: T1?, p2: T2?, p3: T3?, block: (Any, T1, T2, T3) -> R?): R? {
    return if (this != null && p1 != null && p2 != null && p3 != null) block(this, p1, p2, p3) else null
}

/** 对5个参数判空. */
inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> letMultiple(p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, block: (T1, T2, T3, T4, T5) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(p1, p2, p3, p4, p5) else null
}

/** 对5个参数判空. */
inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> Any?.let(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (Any, T1, T2, T3, T4) -> R?): R? {
    return if (this != null && p1 != null && p2 != null && p3 != null && p4 != null) block(this, p1, p2, p3, p4) else null
}

fun String.equalsIgnoreCase(other: String) = this.lowercase() == other.lowercase()