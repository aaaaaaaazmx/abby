package com.cl.common_base.net.adapter


import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.widget.toast.ToastUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.*
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ResponseCallAdapter<T>(
    private val responseType: Type
) : CallAdapter<T, Flow<Response<T>>> {
    override fun adapt(call: Call<T>): Flow<Response<T>> {
        return flow {
            emit(
                suspendCancellableCoroutine { continuation ->
                    call.enqueue(object : Callback<T> {
                        override fun onFailure(call: Call<T>, t: Throwable) {
                            Reporter.reportCatchError(t.message, t.localizedMessage, t.toString())
                            continuation.resumeWithException(t)
                        }

                        override fun onResponse(call: Call<T>, response: Response<T>) {
                            continuation.resume(response)
                        }
                    })
                    continuation.invokeOnCancellation { call.cancel() }
                }
            )
        }
    }

    override fun responseType() = responseType
}


class BodyCallAdapter<T>(private val responseType: Type) :
    CallAdapter<T, Flow<T>> {
    override fun adapt(call: Call<T>): Flow<T> {
        return flow {
            emit(
                suspendCancellableCoroutine { continuation ->
                    call.enqueue(object : Callback<T> {
                        override fun onFailure(call: Call<T>, t: Throwable) {
                            // 捕获异常
                            // 如果是HostName异常、连接超时异常，那么不需要上报，也不需要提示
                            if (t.toString().contains("SocketTimeoutException") || t.toString().contains("UnknownHostException") || t.toString().contains("ConnectException") || t.toString().contains("Throwable")) {
                                ToastUtil.show("Server error, please contact support@heyabby.com")
                                continuation.resumeWithException(Exception("Server error, please contact support@heyabby.com"))
                                call.cancel()
                                kotlin.runCatching {
                                    Reporter.reportCatchError(
                                        t.message,
                                        t.localizedMessage,
                                        t.toString(),
                                        t.toString()
                                    )
                                }
                            } else {
                                Reporter.reportCatchError(t.message, t.localizedMessage, t.toString(), "enqueue onFailure")
                                // continuation.resumeWithException(t)
                                ToastUtil.show("Server error, please contact support@heyabby.com")
                                continuation.resumeWithException(Exception("Server error, please contact support@heyabby.com"))
                                call.cancel()
                            }

                        }

                        override fun onResponse(call: Call<T>, response: Response<T>) {
                            try {
                                /* logE("${response}")
                                 logE("${response.body()}")*/
                                if (response.body() == null || response.code() == Constants.APP_SERVER || response.code() == Constants.APP_SERVER_502) {
                                    ToastUtil.shortShow("Server error, please contact support@heyabby.com")
                                    continuation.resumeWithException(Exception("Server error, please contact support@heyabby.com"))
                                    call.cancel()
                                    return
                                }
                                logI("1323123: ${response.body()}")
                                response.body()?.let {
                                    continuation.resume(it)
                                }
                            } catch (e: Exception) {
                                Reporter.reportCatchError(
                                    e.message,
                                    e.localizedMessage,
                                    e.toString(),
                                    response.toString()
                                )
                                continuation.resumeWithException(e)
                            }
                        }
                    })
                    continuation.invokeOnCancellation { call.cancel() }
                }
            )
        }
    }

    override fun responseType() = responseType

    private fun readResponse(response: Response<T>) {

    }
}