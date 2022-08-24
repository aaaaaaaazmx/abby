package com.cl.common_base.handler;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 全局handler
 *
 * @author lijiewen
 * @date on 2019/4/12
 */
public class GlobalHandler {

    private static Handler sGlobalUiHandler;
    private static Handler sGlobalWorkerHandler;
    private static ExecutorService sGlobalThreadPool;
    private static MessageHandlerThread sGlobalWorkerThread;


    public static Handler getGlobalUiHandler() {
        //主线程
        if (sGlobalUiHandler == null) {
            sGlobalUiHandler = new Handler(Looper.getMainLooper());
        }
        return sGlobalUiHandler;
    }

    public static Handler getGlobalWorkerHandler() {
        //工作线程
        if (sGlobalWorkerThread == null) {
            sGlobalWorkerThread = new MessageHandlerThread("GlobalWorker");
            sGlobalWorkerThread.start();
            sGlobalWorkerHandler = new Handler(sGlobalWorkerThread.getLooper());
        }
        return sGlobalWorkerHandler;
    }

    public static ExecutorService getGlobalThreadPool() {
        //线程池
        if (sGlobalThreadPool == null) {
            sGlobalThreadPool = Executors.newCachedThreadPool();
        }
        return sGlobalThreadPool;
    }


}
