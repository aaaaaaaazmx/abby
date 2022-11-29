package com.cl.common_base.easeui.ui

import android.view.View
import com.cl.common_base.R
import com.cl.common_base.ext.logI
import com.cl.common_base.video.SampleCoverVideo


fun SampleCoverVideo.videoUiHelp(url: String, tag: Int? = 0)  {
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
    fullscreenButton.setOnClickListener { startWindowFullscreen(context, false, true) }
    //防止错位设置
    playTag = "$tag"
    isLockLand = true
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
}