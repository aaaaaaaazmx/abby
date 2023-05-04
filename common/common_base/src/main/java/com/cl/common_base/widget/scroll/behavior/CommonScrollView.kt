package com.cl.common_base.widget.scroll.behavior

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollView
import com.cl.common_base.widget.scroll.behavior.NestedScrollBehavior

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/26
 */
@RequiresApi(Build.VERSION_CODES.CUPCAKE)
class CommonScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BehavioralScrollView(context, attrs, defStyleAttr) {

    var behavior: NestedScrollBehavior? = null

    override fun handleDispatchTouchEvent(e: MotionEvent): Boolean? {
        return behavior?.handleDispatchTouchEvent(e) ?: super.handleDispatchTouchEvent(e)
    }

    override fun handleTouchEvent(e: MotionEvent): Boolean? {
        return behavior?.handleTouchEvent(e) ?: super.handleTouchEvent(e)
    }

    override fun handleNestedPreScrollFirst(scroll: Int, type: Int): Boolean? {
        return behavior?.handleNestedPreScrollFirst(scroll, type) ?: super.handleNestedPreScrollFirst(scroll, type)
    }

    override fun handleNestedScrollFirst(scroll: Int, type: Int): Boolean? {
        return behavior?.handleNestedScrollFirst(scroll, type) ?: super.handleNestedScrollFirst(scroll, type)
    }

    override fun handleScrollSelf(scroll: Int, type: Int): Boolean? {
        return behavior?.handleScrollSelf(scroll, type) ?: super.handleScrollSelf(scroll, type)
    }
}
