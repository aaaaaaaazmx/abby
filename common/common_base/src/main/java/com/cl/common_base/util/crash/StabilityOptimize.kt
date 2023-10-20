package com.cl.common_base.util.crash

import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.bhm.demo.util.JavaAirBagConfig
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter

object StabilityOptimize {
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    fun setUpJavaAirBag(configList: List<JavaAirBagConfig>) {
        logI("Java 安全气囊已开启")
        val preDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        preDefaultExceptionHandler?.let {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                handleException(it, configList, thread, exception)
                if (thread == Looper.getMainLooper().thread) {
                    while (true) {
                        try {
                            Looper.loop()
                        } catch (e: Throwable) {
                            handleException(
                                it,
                                configList,
                                Thread.currentThread(),
                                e
                            )
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