package com.cl.common_base.util.json

import kotlinx.coroutines.*

import kotlinx.coroutines.*

object AsyncLazy {
    fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>> {
        return lazy {
            CoroutineScope(Dispatchers.Default).async(start = CoroutineStart.LAZY) {
                block.invoke(this)
            }
        }
    }
}
