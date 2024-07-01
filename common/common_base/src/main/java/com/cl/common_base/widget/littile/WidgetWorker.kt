package com.cl.common_base.widget.littile

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cl.common_base.R
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.safeToLong
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.calendar.CalendarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.Date

class WidgetWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val service = ServiceCreators.create(BaseApiService::class.java)

    companion object {
        const val TASK_TYPE_KEY = "TASK_TYPE"
        const val TASK_TYPE_UPDATE = "UPDATE"
        const val TASK_TYPE_CLICK = "CLICK"
    }

    @SuppressLint("RemoteViewLayout")
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override suspend fun doWork(): Result {
        val appWidgetId = inputData.getInt("appWidgetId", -1)
        val taskType = inputData.getString(TASK_TYPE_KEY) ?: return Result.failure()
        if (appWidgetId == -1) return Result.failure()

        return try {
            when (taskType) {
                TASK_TYPE_UPDATE -> handleUpdateTask(appWidgetId)
                TASK_TYPE_CLICK -> handleClickTask(appWidgetId)
                else -> Result.failure()
            }
        } catch (e: Exception) {
            logD("Exception: ${e.message}")
            Result.failure()
        }
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    private suspend fun handleUpdateTask(appWidgetId: Int): Result {
        service.controlInfo()
            .map { response ->
                if (response.code != Constants.APP_SUCCESS) {
                    Resource.DataError(response.code, response.msg)
                } else {
                    Resource.Success(response.data)
                }
            }
            .flowOn(Dispatchers.IO)
            .onStart {
                // emit(Resource.Loading)
            }
            .collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout)
                        val widgetData = resource.data
                        logD("Data error: $widgetData")
                        views.setViewVisibility(R.id.tv_login_desc, View.GONE)
                        views.setViewVisibility(R.id.rl_content, View.VISIBLE)
                        views.setTextViewText(R.id.widget_text, if (widgetData?.plantName.isNullOrBlank()) "---" else widgetData?.plantName)
                        views.setTextViewText(R.id.tv_device_type, if (widgetData?.deviceModel.isNullOrBlank()) "---" else widgetData?.deviceModel)
                        views.setTextViewText(R.id.tv_period, if (widgetData?.period.isNullOrBlank()) "---" else widgetData?.period)
                        views.setTextViewText(R.id.tv_date, if (widgetData?.plantPeriod.isNullOrBlank()) "---" else widgetData?.plantPeriod)
                        views.setTextViewText(R.id.tv_humidity, if (widgetData?.humidityCurrent.isNullOrBlank()) "---" else widgetData?.humidityCurrent)
                        views.setTextViewText(R.id.tv_grow_temp, if (widgetData?.temperatureCurrent.isNullOrBlank()) "---" else widgetData?.temperatureCurrent)
                        views.setTextViewText(R.id.tv_water_temp, if (widgetData?.waterCurrent.isNullOrBlank()) "---" else widgetData?.waterCurrent)
                        views.setTextViewText(R.id.tv_task_description, if (widgetData?.taskCurrent.isNullOrBlank()) "---" else widgetData?.taskCurrent)
                        if (widgetData?.taskTime.isNullOrBlank()) {
                            views.setTextViewText(R.id.tv_date_task, "---")
                        } else {
                            views.setTextViewText(R.id.tv_date_task, getYmdForEn(time = widgetData?.taskTime.safeToLong() * 1000))
                        }

                        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }

                    is Resource.DataError -> {
                        logD("Data error: ${resource.errorMsg}, ${resource.errorCode}")
                        if (resource.errorCode == 401) {
                            val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout)
                            views.setViewVisibility(R.id.tv_login_desc, View.VISIBLE)
                            views.setViewVisibility(R.id.rl_content, View.GONE)
                            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    }

                    is Resource.Loading -> {
                        // 处理加载状态（可选）
                    }
                }
            }
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    private suspend fun handleClickTask(appWidgetId: Int): Result {
        service.checkPlant()
            .map { response ->
                if (response.code != Constants.APP_SUCCESS) {
                    Resource.DataError(response.code, response.msg)
                } else {
                    Resource.Success(response.data)
                }
            }
            .flowOn(Dispatchers.IO)
            .onStart {
                // emit(Resource.Loading)
            }
            .collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { PlantCheckHelp().plantStatusCheck(context = applicationContext, data = it, isClearTask = false) }
                    }

                    is Resource.DataError -> {
                        logD("Data error: ${resource.errorMsg}, ${resource.errorCode}")
                    }

                    is Resource.Loading -> {
                        // 处理加载状态（可选）
                    }
                }
            }
        return Result.success()
    }

    /**
     * 获取当前年月日-- 后面跟着英文的th
     * 如 9 12th 2022
     */
    private fun getYmdForEn(dateTime: Date? = null, time: Long? = null): String {
        dateTime?.let {
            val mm = CalendarUtil.getFormat("MMM").format(dateTime.time)
            val dd = CalendarUtil.getFormat("dd").format(dateTime.time) + CalendarUtil.getDaySuffix(
                dateTime
            )
            val yyyy = CalendarUtil.getFormat("yyyy").format(dateTime.time)
            return "$mm $dd $yyyy"
        }

        time?.let {
            val mm = CalendarUtil.getFormat("MMM").format(time)
            val date = Date()
            date.time = time
            val dd = CalendarUtil.getFormat("dd").format(time) + CalendarUtil.getDaySuffix(date)
            val yyyy = CalendarUtil.getFormat("yyyy").format(time)
            return "$mm $dd $yyyy"
        }
        return ""
    }
}
