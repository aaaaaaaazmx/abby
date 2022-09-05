package com.cl.common_base.util.databinding

import android.view.View

/**
 * 设置单次点击事件监听器，避免连续快速点击造成两次重复操作。
 *
 * @param duration 点击时间间隔
 * @param onSingleClick 点击事件回调，快速连续点击将被忽略掉
 */
fun View.setOnSingleClickListener(duration: Int = 2000, onSingleClick: (view: View) -> Unit) {
    setOnClickListener(object: OnSingleClickListener(duration) {
        override fun onSingleClick(view: View) {
            onSingleClick.invoke(view)
        }
    })
}

abstract class OnSingleClickListener(val duration: Int) : View.OnClickListener {
    private var lastClickTime = -1L

    final override fun onClick(view: View) {
        lastClickTime = if (lastClickTime < 0) {
            onSingleClick(view)
            System.currentTimeMillis()
        } else {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > duration) {
                onSingleClick(view)
            }
            now
        }
    }

    /**
     * 点击操作回调。
     *
     * @param view 当前点击操作的视图
     */
    abstract fun onSingleClick(view: View)
}