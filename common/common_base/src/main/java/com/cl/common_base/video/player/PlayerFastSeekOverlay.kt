package com.cl.common_base.video.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.END
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.START
import androidx.constraintlayout.widget.ConstraintSet
import com.cl.common_base.R

class PlayerFastSeekOverlay(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    var secondsView: SecondsView
    var circleClipTapView: CircleClipTapView
    var rootConstraintLayout: ConstraintLayout

    var wasForwarding: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.player_fast_seek_overlay, this, true)

        secondsView = findViewById(R.id.seconds_view)
        circleClipTapView = findViewById(R.id.circle_clip_tap_view)
        rootConstraintLayout = findViewById(R.id.root_constraint_layout)

        addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            circleClipTapView.updateArcSize(view)
        }
    }

    var performListener: PerformListener? = null

    fun performListener(listener: PerformListener?) = apply {
        performListener = listener
    }

    var seekSecondsSupplier: () -> Int = { 0 }

    fun seekSecondsSupplier(supplier: (() -> Int)?) = apply {
        seekSecondsSupplier = supplier ?: { 0 }
    }

    // Indicates whether this (double) tap is the first of a series
    // Decides whether to call performListener.onAnimationStart or not
    var initTap: Boolean = false

    //    override fun onDoubleTapStarted(portion: DisplayPortion) {
    //        if (DEBUG)
    //            Log.d(TAG, "onDoubleTapStarted called with portion = [$portion]")
    //
    //        initTap = false
    //
    //        secondsView.stopAnimation()
    //    }
    //
    //    override fun onDoubleTapProgressDown(portion: DisplayPortion) {
    //        val shouldForward: Boolean =
    //            performListener?.getFastSeekDirection(portion)?.directionAsBoolean ?: return
    //
    //        if (DEBUG)
    //            Log.d(
    //                TAG,
    //                "onDoubleTapProgressDown called with " +
    //                        "shouldForward = [$shouldForward], " +
    //                        "wasForwarding = [$wasForwarding], " +
    //                        "initTap = [$initTap], "
    //            )
    //
    //        /*
    //         * Check if a initial tap occurred or if direction was switched
    //         */
    //        if (!initTap || wasForwarding != shouldForward) {
    //            // Reset seconds and update position
    //            secondsView.seconds = 0
    //            changeConstraints(shouldForward)
    //            circleClipTapView.updatePosition(!shouldForward)
    //            secondsView.setForwarding(shouldForward)
    //
    //            wasForwarding = shouldForward
    //
    //            if (!initTap) {
    //                initTap = true
    //            }
    //        }
    //
    //        performListener?.onDoubleTap()
    //
    //        secondsView.seconds += seekSecondsSupplier.invoke()
    //        performListener?.seek(forward = shouldForward)
    //    }
    //
    //    override fun onDoubleTapFinished() {
    //        if (DEBUG)
    //            Log.d(TAG, "onDoubleTapFinished called with initTap = [$initTap]")
    //
    //        if (initTap) performListener?.onDoubleTapEnd()
    //        initTap = false
    //
    //        secondsView.stopAnimation()
    //    }

    fun changeConstraints(forward: Boolean) {
        val constraintSet = ConstraintSet()
        with(constraintSet) {
            clone(rootConstraintLayout)
            clear(secondsView.id, if (forward) START else END)
            connect(
                secondsView.id, if (forward) END else START,
                PARENT_ID, if (forward) END else START
            )
            secondsView.startAnimation()
            applyTo(rootConstraintLayout)
        }
    }

    interface PerformListener {
        fun onDoubleTap()
        fun onDoubleTapEnd()

        /**
         * Determines if the playback should forward/rewind or do nothing.
         */
        // @NonNull
        // fun getFastSeekDirection(portion: DisplayPortion): FastSeekDirection
        // fun seek(forward: Boolean)

        enum class FastSeekDirection(val directionAsBoolean: Boolean?) {
            NONE(null),
            FORWARD(true),
            BACKWARD(false);
        }
    }

    companion object {
        const val TAG = "PlayerFastSeekOverlay"
        val DEBUG = "LLL"
        private const val DOUBLE_TAP_DELAY = 550L
    }


    var isDoubleTapping: Boolean = false


    // 双击之间的延迟
    private var doubleTapDelay = DOUBLE_TAP_DELAY
    private val doubleTapHandler: Handler = Handler(Looper.getMainLooper())
    private val doubleTapRunnable = Runnable {
        isDoubleTapping = false
        // todo 结束双击
        onDoubleTapFinished?.invoke()
    }

    private var onDoubleTapFinished: (() -> Unit)? = null
    private var onDoubleTapProgressDown: (() -> Unit)? = null

    // 时间监听
    fun startMultiDoubleTap(
        e: MotionEvent,
        onDoubleTapStarted: (() -> Unit?)? = null,
        onDoubleTapFinished: (() -> Unit)? = null,
        onDoubleTapProgressDown: (() -> Unit)? = null
    ) {
        if (!isDoubleTapping) {
            keepInDoubleTapMode()
            Log.e("123123123", "isDoubleTapping: ${isDoubleTapping}")
            // todo 开始双击回调
            onDoubleTapStarted?.invoke()
            // todo 双击结束回调
            this@PlayerFastSeekOverlay.onDoubleTapFinished = onDoubleTapFinished
            // todo 持续双击
            this@PlayerFastSeekOverlay.onDoubleTapProgressDown = onDoubleTapProgressDown
            // doubleTapControls?.onDoubleTapStarted(getDisplayPortion(e))
        }
    }

    fun keepInDoubleTapMode() {
        isDoubleTapping = true
        doubleTapHandler.removeCallbacks(doubleTapRunnable)
        doubleTapHandler.postDelayed(doubleTapRunnable, doubleTapDelay)
    }

    val isDoubleTapEnabled: Boolean
        get() = doubleTapDelay > 0

    fun onDown(e: MotionEvent): Boolean {
        if (isDoubleTapping && isDoubleTapEnabled) {
            // 持续双击
            onDoubleTapProgressDown?.invoke()
            // doubleTapControls?.onDoubleTapProgressDown(getDisplayPortion(e))
            return true
        }
        return false
    }

    open fun onDownNotDoubleTapping(e: MotionEvent): Boolean {
        return false // do not call super.onDown(e) by default, overridden for popup player
    }

}
