package com.cl.common_base.ext

import com.alibaba.android.arouter.BuildConfig
import com.orhanobut.logger.Logger

/**
 *  author: wangyb
 *  date: 3/29/21 6:01 PM
 *  description: 日志工具类
 */

private const val VERBOSE = 1
private const val DEBUG = 2
private const val INFO = 3
private const val WARN = 4
private const val ERROR = 5

private val level = if (BuildConfig.DEBUG) VERBOSE else VERBOSE

fun logV(tag: String) {
    if (level <= VERBOSE) {
        Logger.v(tag)
    }
}

fun logD(obj: Any?) {
    if (level <= DEBUG) {
        Logger.d(obj)
    }
}


fun logI(tag: String) {
    if (level <= INFO) {
        Logger.i(tag)
    }
}


fun logE(tag: String) {
    if (level <= ERROR) {
        Logger.e(tag)
    }
}
