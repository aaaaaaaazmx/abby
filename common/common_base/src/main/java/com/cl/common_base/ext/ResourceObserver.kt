package com.cl.common_base.ext

import androidx.lifecycle.Observer

open class ResourceObserver<T> : Observer<Resource<T>> {
    private var success: (Resource.Success<T>.() -> Unit)? = null
    private var error: (Resource.DataError<T>.(errorMsg: String?, code: Int?) -> Unit)? = null
    private var loading: (Resource.Loading<T>.() -> Unit)? = null

    fun success(s: Resource.Success<T>.() -> Unit) {
        success = s
    }

    fun error(e: Resource.DataError<T>.(errorMsg: String?, code: Int?) -> Unit) {
        error = e
    }

    fun loading(l: Resource.Loading<T>.() -> Unit) {
        loading = l
    }

    override fun onChanged(resource: Resource<T>) {
        when (resource) {
            is Resource.Success -> {
                success?.invoke(resource)
            }
            is Resource.DataError -> {
                error?.invoke(resource, resource.errorMsg, resource.errorCode)
            }
            is Resource.Loading -> loading?.invoke(resource)
        }
    }
}

fun <T> resourceObserver(init: ResourceObserver<T>.() -> Unit): ResourceObserver<T> {
    val observer = ResourceObserver<T>()
    init(observer)
    return observer
}