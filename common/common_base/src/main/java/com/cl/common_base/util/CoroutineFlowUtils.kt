package com.cl.common_base.util
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.*

object CoroutineFlowUtils {

    // 默认的全局作用域（非 GlobalScope，使用自定义作用域）
    private val defaultScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * 提交任务并自动选择适当的作用域
     * @param context 上下文，可以是 Activity、Fragment、ViewModel 或 null
     * @param task 需要执行的后台任务
     * @param onSuccess 任务执行成功的回调
     * @param onError 错误回调
     */
    private fun <T> launchTask(
        scope: CoroutineScope? = null, // 传入的上下文，可以是 Activity/Fragment/ViewModel 或 null
        task: suspend () -> T,    // 需要执行的后台任务
        dispatcher: CoroutineDispatcher = Dispatchers.IO, // 线程切换
        onSuccess: ((T) -> Unit)? = null,  // 任务执行成功后的回调
        onError: (Throwable) -> Unit = { it.printStackTrace() }  // 错误时的回调
    ) {
        // 启动协程
        (scope ?: defaultScope).launch {
            try {
                val result = withContext(dispatcher) { task() }  // 在指定线程执行任务
                withContext(Dispatchers.Main) {
                    onSuccess?.invoke(result)  // 切回主线程，执行成功回调
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)  // 切回主线程，执行错误回调
                }
            }
        }
    }

    fun <T> executeInBackground(
        scope: CoroutineScope? = null, // 传入的上下文，可以是 Activity/Fragment/ViewModel 或 null
        task: suspend () -> T,  // 需要执行的后台任务
        dispatcher: CoroutineDispatcher = Dispatchers.IO, // 默认在 IO 线程执行
        onSuccess: ((T) -> Unit)? = null,  // 任务执行成功后的回调
        onError: (Throwable) -> Unit = { it.printStackTrace() }  // 错误回调
    ) {
        // 在指定的后台线程执行任务
        launchTask(
            scope = scope,  // 使用默认作用域
            task = task,
            dispatcher = dispatcher,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun <T> executeInMainThread(
        scope: CoroutineScope? = null, // 传入的上下文，可以是 Activity/Fragment/ViewModel 或 null
        task: suspend () -> T,  // 需要在主线程执行的任务
        onSuccess: ((T) -> Unit)? = null,  // 成功回调
        onError: (Throwable) -> Unit = { it.printStackTrace() }  // 错误回调
    ) {
        // 在主线程执行任务
        launchTask(
            scope = scope,  // 使用默认作用域
            task = task,
            dispatcher = Dispatchers.Main,  // 确保在主线程执行
            onSuccess = onSuccess,
            onError = onError
        )
    }



    /**
     * 支持链式操作的 map 方法，类似 Flow 的 map 操作
     */
    fun <T, R> mapFlow(
        flow: Flow<T>,  // 输入流
        transform: suspend (T) -> R  // 转换操作
    ): Flow<R> {
        return flow.map { transform(it) }  // 变换流数据
    }

    /**
     * 支持链式操作的 flatMap 方法，类似 Flow 的 flatMap 操作
     */
    fun <T, R> flatMapFlow(
        flow: Flow<T>,  // 输入流
        transform: suspend (T) -> Flow<R>  // 转换成新的流
    ): Flow<R> {
        return flow.flatMapConcat { transform(it) }  // 扁平化转换流
    }

    /**
     * 取消工具类中的所有任务（针对没有生命周期感知的场景）
     */
    fun cancelAllTasks() {
        defaultScope.coroutineContext[Job]?.cancel()  // 取消自定义作用域的所有协程
    }

    /**
     * 在后台线程执行任务并返回 Flow
     * @param task 后台任务
     * @param dispatcher 指定执行任务的线程
     */
    fun <T> launchTaskFlow(
        task: suspend () -> T,  // 后台任务
        dispatcher: CoroutineDispatcher = Dispatchers.IO,  // 默认在 IO 线程执行
    ): Flow<T> {
        return flow {
            // 在指定线程执行任务
            val result = withContext(dispatcher) { task() }
            emit(result)  // 发射结果
        }
    }

    /**
     * 收集 Flow 并在主线程中执行回调
     */
    suspend fun <T> collectInMainThread(
        flow: Flow<T>,  // 要收集的流
        onSuccess: (T) -> Unit,  // 成功回调
        onError: (Throwable) -> Unit = { it.printStackTrace() },  // 错误回调
    ) {
        flow
            .catch { error -> onError(error) }  // 错误处理
            .flowOn(Dispatchers.Main)  // 切换到主线程处理 UI 更新
            .collect { value -> onSuccess(value) }  // 收集结果并处理
    }

}


