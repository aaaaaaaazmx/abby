package com.cl.common_base.widget.ripple

import java.lang.ref.WeakReference

class RippleLifecycle(view: RippleView) : BaseLifecycle {
    private val reference = WeakReference(view)
    override fun onResume() {
        reference.get()?.onResume()
    }

    override fun onPause() {
        reference.get()?.onPause()
    }

    override fun onDestroy() {
        reference.get()?.onStop()
    }
}