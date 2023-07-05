package com.cl.common_base.video

import android.app.Activity
import android.content.Context
import android.view.View
import com.cl.common_base.R
import com.cl.common_base.ext.logI
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer


fun SampleCoverVideo.videoUiHelp(url: String, tag: Int? = -1) {
    // 第一帧显示的图
    loadCoverImage(url, R.mipmap.placeholder)
    // 设置全屏按钮
    enlargeImageRes = R.drawable.video_shrink_bg
    shrinkImageRes = R.drawable.video_enlarge_bg
    setUp(url, true, null, null, "")
    // 隐藏标题
    titleTextView.visibility = View.GONE
    // 隐藏返回键
    backButton.visibility = View.GONE
    //设置全屏按键功能
    /*fullscreenButton.setOnClickListener { startWindowFullscreen(context, false, true) }*/
    //防止错位设置
    playTag = "$tag"
    logI("layou: $tag")
    playPosition = tag!!
    //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，这个标志为和 setLockLand 冲突，需要和 orientationUtils 使用
    isAutoFullWithSize = true
    //音频焦点冲突时是否释放
    isReleaseWhenLossAudio = false
    //全屏动画
    isShowFullAnimation = false
    //小屏时不触摸滑动
    setIsTouchWiget(false)
    // 暂停状态下显示封面
    isShowPauseCover = true

    /* 新的 */
    //设置全屏按键功能
    fullscreenButton.setOnClickListener { resolveFullBtn(context, this, orientationUtils) }
    // 是否开启自动旋转
    isRotateViewAuto = false
    // false = 竖屏、 true = 横屏
    isLockLand = false
    // 系统旋转相关
    isRotateWithSystem = false
    // 回调
    setVideoAllCallBack(object : GSYSampleCallBack() {
        override fun onClickStartIcon(url: String, vararg objects: Any) {
            super.onClickStartIcon(url, *objects)
        }

        override fun onPrepared(url: String, vararg objects: Any) {
            super.onPrepared(url, *objects)
            Debuger.printfLog("onPrepared")
            val full: Boolean = currentPlayer.isIfCurrentIsFullscreen
            if (!currentPlayer.isIfCurrentIsFullscreen) {
                GSYVideoManager.instance().isNeedMute = false
            }
            if (currentPlayer.isIfCurrentIsFullscreen) {
                GSYVideoManager.instance().setLastListener(this@videoUiHelp)
            }
            val curPlayer = objects[1] as StandardGSYVideoPlayer
            // itemPlayer = holder.gsyVideoPlayer
            // isPlay = true
            //重力全屏工具类
            initOrientationUtils(this@videoUiHelp, full, context)
            onPreparedd()
        }

        override fun onQuitFullscreen(url: String, vararg objects: Any) {
            super.onQuitFullscreen(url, *objects)
            // isFull = false
            GSYVideoManager.instance().isNeedMute = false
            onQuitFullscreenn()
        }

        override fun onEnterFullscreen(url: String, vararg objects: Any) {
            super.onEnterFullscreen(url, *objects)
            GSYVideoManager.instance().isNeedMute = false
            // isFull = true
            currentPlayer.titleTextView.text = objects[0] as String
        }

        override fun onAutoComplete(url: String, vararg objects: Any) {
            super.onAutoComplete(url, *objects)
            // curPlayer = null
            // itemPlayer = null
            // isPlay = false
            // isFull = false
            onAutoCompletee()
        }
    })
}

private fun resolveFullBtn(context: Context, gsyVideoPlayer: SampleCoverVideo, orientationUtils: OrientationUtils?) {
    orientationUtils?.let {
        resolveFull()
    }
    gsyVideoPlayer.startWindowFullscreen(context, false, true)
}

private fun resolveFull() {
    //直接横屏
    // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
    // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
    orientationUtils?.resolveByClick()
}

private fun onPreparedd() {
    if (orientationUtils == null) {
        return
    }
    //开始播放了才能旋转和全屏
    orientationUtils?.isEnable = true
}

private fun onQuitFullscreenn() {
    // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
    // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
    orientationUtils?.backToProtVideo()
}

private fun onAutoCompletee() {
    orientationUtils?.isEnable = false
    orientationUtils?.releaseListener()
    orientationUtils = null
    // isPlay = false
}

var orientationUtils: OrientationUtils? = null
private fun initOrientationUtils(standardGSYVideoPlayer: StandardGSYVideoPlayer, full: Boolean, context: Context) {
    orientationUtils = OrientationUtils(context as Activity?, standardGSYVideoPlayer)
    //是否需要跟随系统旋转设置
    //orientationUtils.setRotateWithSystem(false);
    orientationUtils?.isEnable = false
    orientationUtils?.isLand = if (full) 1 else 0
}

