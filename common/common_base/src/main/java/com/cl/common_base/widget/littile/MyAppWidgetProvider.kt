package com.cl.common_base.widget.littile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.constants.RouterPath
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
class MyAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                scheduleWorkManager(context, appWidgetId)
            }
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        val intent = Intent(context, MyAppWidgetProvider::class.java).apply {
            action = "com.example.mywidget.CLICK"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.rl_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    private fun scheduleWorkManager(context: Context, appWidgetId: Int) {
        val workRequest = PeriodicWorkRequestBuilder<WidgetWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf(
                "appWidgetId" to appWidgetId,
                WidgetWorker.TASK_TYPE_KEY to WidgetWorker.TASK_TYPE_UPDATE
            ))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WidgetWorker$appWidgetId",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun startWorker(context: Context, appWidgetId: Int) {
        val workRequest = OneTimeWorkRequestBuilder<WidgetWorker>()
            .setInputData(workDataOf(
                "appWidgetId" to appWidgetId,
                WidgetWorker.TASK_TYPE_KEY to WidgetWorker.TASK_TYPE_CLICK
            ))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "WidgetClickWorker$appWidgetId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // 点击
        if (intent.action == "com.example.mywidget.CLICK") {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                // startWorker(context, appWidgetId)
                ARouter.getInstance().build(RouterPath.Welcome.PAGE_SPLASH)
                    .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .navigation()
            }
        }
        // 刷新
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, MyAppWidgetProvider::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.cl.common_base.widget.littile.ACTION_UPDATE_WIDGET"
    }
}
