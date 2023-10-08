/*
 * Copyright (c) 2022-2032 buhuiming
 * 不能修改和删除上面的版权声明
 * 此代码属于buhuiming编写，在未经允许的情况下不得传播复制
 */
package com.bhm.ble.callback


/**
 * 写回调
 *
 * @author Buhuiming
 * @date 2023年05月26日 15时54分
 */
open class BleWriteCallback : BleBaseCallback() {

    private var writeSuccess: ((current: Int, total: Int, justWrite: ByteArray) -> Unit)? = null

    private var writeFail: ((current: Int, total: Int, throwable: Throwable) -> Unit)? = null

    private var writeComplete: ((allSuccess: Boolean) -> Unit)? = null

    fun onWriteFail(value: ((current: Int, total: Int, throwable: Throwable) -> Unit)) {
        writeFail = value
    }

    fun onWriteSuccess(value: ((current: Int, total: Int, justWrite: ByteArray) -> Unit)) {
        writeSuccess = value
    }

    fun onWriteComplete(value: ((allSuccess: Boolean) -> Unit)) {
        writeComplete = value
    }

    open fun callWriteFail(current: Int, total: Int, throwable: Throwable) {
        launchInMainThread {
            writeFail?.invoke(current, total, throwable)
        }
    }

    open fun callWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
        launchInMainThread {
            writeSuccess?.invoke(current, total, justWrite)
        }
    }

    open fun callWriteComplete(allSuccess: Boolean) {
        launchInMainThread {
            writeComplete?.invoke(allSuccess)
        }
    }
}