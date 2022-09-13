package com.cl.common_base.widget.ripple

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cl.common_base.ext.logI

class RippleLifecycleAdapter(private val lifecycle: BaseLifecycle) : LifecycleObserver {


    /**
     * onResume
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifecycleResume() {
        logI("onLifecycleResume")
        lifecycle.onResume()
    }


    /**
     * onPause
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onLifecyclePause() {
        logI("onLifecyclePause")
        lifecycle.onPause()
    }


    /**
     * onDestroy
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleDestroy() {
        logI("onLifecycleDestroy")
        lifecycle.onDestroy()
    }
}