/*
 * Copyright (c) 2022-2032 buhuiming
 * 不能修改和删除上面的版权声明
 * 此代码属于buhuiming编写，在未经允许的情况下不得传播复制
 */
package com.cl.common_base.bean

import java.util.logging.Level


/**
 * 日志信息
 *
 * @author Buhuiming
 * @date 2023年06月01日 15时54分
 */
data class LogEntity(
    val level: Level,
    val msg: String,
    var byteArray: ByteArray? = null,
    val time: Long = System.currentTimeMillis()
)