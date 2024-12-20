package com.cl.common_base.util.crash

import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.bhm.demo.util.JavaAirBagConfig
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter

object StabilityOptimize {
    // 记录上次异常时间，用于控制异常处理频率
    private var lastHandleTime = 0L

    // 最小异常处理间隔(毫秒)
    private const val MIN_HANDLE_INTERVAL = 100L

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    fun setUpJavaAirBag(configList: List<JavaAirBagConfig>) {
        logI("Java 安全气囊已开启")
        Thread.getDefaultUncaughtExceptionHandler()?.let {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                val currentTime = System.currentTimeMillis()
                // 如果距离上次异常处理时间太短，就不重复处理
                if (currentTime - lastHandleTime >= MIN_HANDLE_INTERVAL) {
                    // 记录和上报异常
                    handleException(it, configList, thread, exception)
                    lastHandleTime = currentTime
                }

                if (thread == Looper.getMainLooper().thread) {
                    while (true) {
                        try {
                            // 给消息循环一个小的休眠时间，降低CPU占用
                            Thread.sleep(16)
                            Looper.loop()
                        } catch (e: Throwable) {
                            val now = System.currentTimeMillis()
                            // 控制异常处理频率
                            if (now - lastHandleTime >= MIN_HANDLE_INTERVAL) {
                                handleException(it, configList, Thread.currentThread(), e)
                                lastHandleTime = now
                            }
                        }
                    }
                }
            }
        }
    }

    /*fun setUpNativeAirBag(signal: Int, soName: String, backtrace: String) {
        Log.i("StabilityOptimize", "Native 安全气囊已开启")
        StabilityNativeLib().openNativeAirBag(signal, soName, backtrace)
    }*/

    private fun handleException(
        preDefaultExceptionHandler: Thread.UncaughtExceptionHandler,
        configList: List<JavaAirBagConfig>,
        thread: Thread,
        exception: Throwable
    ) {
        val stackTraceElement = exception.stackTrace[0]
        // 通过Bugly上报异常
        logI("Java Crash 已捕获")
        logI(
            """
                FATAL EXCEPTION: ${thread.name}
                ${exception.message ?: ""}
                CrashBy: ${exception.javaClass.name}===>${exception.message}
                crash:   ${stackTraceElement.className}===>${stackTraceElement.methodName}===>${stackTraceElement.lineNumber}===>${exception.message}
            """.trimIndent()
        )
        val errorType = """
                FATAL EXCEPTION: ${thread.name}
                ${exception.message ?: ""}
                CrashBy: ${exception.javaClass.name}===>${exception.message}
                crash:   ${stackTraceElement.className}===>${stackTraceElement.methodName}===>${stackTraceElement.lineNumber}===>${exception.message}
            """.trimIndent()
        Reporter.reportCatchError(errorType, exception.message, exception.message, "Crash_Bug")
        /*if (configList.any { isStackTraceMatching(exception, it) }) {
            val stackTraceElement = exception.stackTrace[0]
            // 通过Bugly上报异常
            logI("Java Crash 已捕获")
            logI("""
                FATAL EXCEPTION: ${thread.name}
                ${exception.message ?: ""}
                CrashBy: ${exception.javaClass.name}===>${exception.message}
                crash:   ${stackTraceElement.className}===>${stackTraceElement.methodName}===>${stackTraceElement.lineNumber}===>${exception.message}
            """.trimIndent())
            val errorType = """
                FATAL EXCEPTION: ${thread.name}
                ${exception.message ?: ""}
                CrashBy: ${exception.javaClass.name}===>${exception.message}
                crash:   ${stackTraceElement.className}===>${stackTraceElement.methodName}===>${stackTraceElement.lineNumber}===>${exception.message}
            """.trimIndent()
            Reporter.reportCatchError(errorType, exception.message, exception.message, "Crash_Bug")
           *//* Log.w("StabilityOptimize", "Java Crash 已捕获")
            Log.w("StabilityOptimize", "FATAL EXCEPTION: ${thread.name}")
            Log.w("StabilityOptimize", exception.message ?: "")*//*
        } else {
            Log.w("StabilityOptimize", "Java Crash 未捕获，交给原有 ExceptionHandler 处理")
            preDefaultExceptionHandler.uncaughtException(thread, exception)
        }*/
    }

    private fun isStackTraceMatching(exception: Throwable, config: JavaAirBagConfig): Boolean {
        val stackTraceElement = exception.stackTrace[0]
        return true
        /*return config.crashType == exception.javaClass.name
                && config.crashMessage == exception.message
                && config.crashClass == stackTraceElement?.className
                && config.crashMethod == stackTraceElement.methodName
                && (config.crashAndroidVersion == 0 || (config.crashAndroidVersion == Build.VERSION.SDK_INT))*/
    }
}