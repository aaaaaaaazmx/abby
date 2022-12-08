package com.cl.common_base.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cl.common_base.R;
import com.cl.common_base.ext.ViewUtils;
import com.cl.common_base.video.player.PlayerFastSeekOverlay;
import com.cl.common_base.widget.FeatureTitleBar;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoControlView;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;


/**
 * 带封面
 * Created by guoshuyu on 2017/9/3.
 */

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class SampleCoverVideo extends NormalGSYVideoPlayer {

    ImageView mCoverImage;

    String mCoverOriginUrl;

    int mCoverOriginId = 0;

    int mDefaultRes;
    private PlayerFastSeekOverlay fastSeekOverlay;
    private SeekBar mProgress;

    public SampleCoverVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SampleCoverVideo(Context context) {
        super(context);
    }

    public SampleCoverVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCoverImage = (ImageView) findViewById(R.id.thumbImage);
        fastSeekOverlay = findViewById(R.id.fast_seek_overlay);
        mProgress = findViewById(R.id.progress);

        if (mThumbImageViewLayout != null &&
                (mCurrentState == -1 || mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.setVisibility(VISIBLE);
        }
        // 中间滑动进度条
        setDialogProgressBar(ContextCompat.getDrawable(context, R.drawable.video_progress_bg));
        // 音量进度条
        setDialogVolumeProgressBar(ContextCompat.getDrawable(context, R.drawable.video_progress_volume_bg));

        // 默认显示的底部进度条
        setBottomProgressBarDrawable(ContextCompat.getDrawable(context, R.drawable.video_new_progress));
    }

    @Override
    protected void updateStartImage() {
        if (mStartButton instanceof ImageView) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(R.drawable.video_click_pause);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(R.drawable.video_click_play);
            } else {
                imageView.setImageResource(R.drawable.video_click_play);
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_cover;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {


        int id = v.getId();
        float x = event.getX();
        float y = event.getY();

        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            onClickUiToggle(event);
            startDismissControlViewTimer();
            return true;
        }

        if (id == R.id.fullscreen) {
            return false;
        }

        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    touchSurfaceDown(x, y);

                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);

                    if ((mIfCurrentIsFullscreen && mIsTouchWigetFull)
                            || (mIsTouchWiget && !mIfCurrentIsFullscreen)) {
                        if (!mChangePosition && !mChangeVolume && !mBrightness) {
                            touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
                        }
                    }
                    touchSurfaceMove(deltaX, deltaY, y);

                    break;
                case MotionEvent.ACTION_UP:

                    startDismissControlViewTimer();

                    touchSurfaceUp();


                    Debuger.printfLog(SampleCoverVideo.this.hashCode() + "------------------------------ surface_container ACTION_UP");

                    startProgressTimer();

                    //不要和隐藏虚拟按键后，滑出虚拟按键冲突
                    if (mHideKey && mShowVKey) {
                        return true;
                    }
                    break;
            }
            this.gestureDetector.onTouchEvent(event);
        } else if (id == R.id.progress) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    cancelDismissControlViewTimer();
                case MotionEvent.ACTION_MOVE:
                    cancelProgressTimer();
                    ViewParent vpdown = getParent();
                    while (vpdown != null) {
                        vpdown.requestDisallowInterceptTouchEvent(true);
                        vpdown = vpdown.getParent();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();

                    Debuger.printfLog(SampleCoverVideo.this.hashCode() + "------------------------------ progress ACTION_UP");
                    startProgressTimer();
                    ViewParent vpup = getParent();
                    while (vpup != null) {
                        vpup.requestDisallowInterceptTouchEvent(false);
                        vpup = vpup.getParent();
                    }
                    mBrightnessData = -1f;
                    break;
            }
        }

        return false;
    }

    @Override
    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
            try {
                position = getGSYVideoManager().getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
                return position;
            }
        }
        // 防止在暂停时、进度条回弹问题
        if (getGSYVideoManager().getCurrentPosition() == 0) {
            return 1;
        }
        if (position == 0 && mCurrentPosition > 0) {
            return mCurrentPosition;
        }
        return position;
    }

    private int i = 1;
    protected GestureDetector gestureDetector = new GestureDetector(getContext().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (GSYVideoManager.instance().getCurrentPosition() == 0) {
                clickStartIcon();
                return true;
            }
            touchDoubleUp(e);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!mChangePosition && !mChangeVolume && !mBrightness) {
                onClickUiToggle(e);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            touchLongPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // todo 判断当前是否是双击状态、然后在每次按下去的时候加1
            Boolean isttt = fastSeekOverlay.onDown(e);
            if (!isttt) {
                if (fastSeekOverlay.onDownNotDoubleTapping(e)) {
                    return super.onDown(e);
                }
            }
            return true;
        }
    });

    public void touchDoubleUp(MotionEvent event) {
        OrientationUtils orientationUtils = new OrientationUtils(CommonUtil.getActivityContext(getContext()), this, getOrientationOption());
        float x = event.getX();
        // 双击暂停
        int screenWidth;
        //竖屏
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            screenWidth = mScreenWidth;
        } else {
            screenWidth = mScreenHeight;
        }
        if (x > screenWidth * 0.3 && x < screenWidth * 0.6) {
            if (!mHadPlay) {
                return;
            }
            clickStartIcon();
            return;
        }
        fastSeekOverlay.startMultiDoubleTap(event, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                i = 0;
                Log.e("12312312 双击开始", "i: " + i);
                // todo 双击开始
                fastSeekOverlay.getSecondsView().stopAnimation();
                fastSeekOverlay.getSecondsView().setSeconds(0);
                ViewUtils.animate(fastSeekOverlay, true, 450);
                // extracted(orientationUtils, x, i, false);
                return null;
            }
        }, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                // todo 结束双击
                Log.e("12312312 结束双击", "i: " + i);
                fastSeekOverlay.getSecondsView().stopAnimation();
                ViewUtils.animate(fastSeekOverlay, false, 450);
                extracted(orientationUtils, x, i, true);
                i = 1;
                return null;
            }
        }, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Log.e("12312312 持续双击", "i: " + i);
                i++;
                // todo 持续双击
                fastSeekOverlay.getSecondsView().setSeconds(i * 10);
                extracted(orientationUtils, x, i, false);
                return null;
            }
        });
    }

    private void extracted(OrientationUtils orientationUtils, float x, int count, boolean isDoubleOver) {
        int screenWidth;
        //竖屏
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            screenWidth = mScreenWidth;
        } else {
            screenWidth = mScreenHeight;
        }
        // 是否取消延时操作
        if (fastSeekOverlay.isDoubleTapping() && fastSeekOverlay.isDoubleTapEnabled()) {
            fastSeekOverlay.keepInDoubleTapMode();
        }
        // 快退
        if (x <= screenWidth * 0.3) {
            Log.e("12332123", "x : " + x + "touch: " + screenWidth * 0.3 + "快退");

            //快退
            fastSeekOverlay.changeConstraints(false);
            fastSeekOverlay.getCircleClipTapView().updatePosition(true);
            fastSeekOverlay.getSecondsView().setForwarding(false);
            if (isDoubleOver) {
                // 10000 是 10 秒
                forwardOrRewind(-(count * 10000L));
            }
        }

        // 双击暂停
        if (x > screenWidth * 0.3 && x < screenWidth * 0.6) {
            if (!mHadPlay) {
                return;
            }
            clickStartIcon();
            return;
        }
        // 快进
        if (x >= screenWidth * 0.6) {
            Log.e("12332123", "x : " + x + "touch: " + screenWidth * 0.6 + "快进");

            // 展示动画
            fastSeekOverlay.changeConstraints(true);
            fastSeekOverlay.getCircleClipTapView().updatePosition(false);
            fastSeekOverlay.getSecondsView().setForwarding(true);

            //快进10
            if (isDoubleOver) {
                // 10000 是 10 秒
                forwardOrRewind(count * 10000L);
            }
        }
    }

    public void forwardOrRewind(long time) {
        int totalTimeDuration = (int) getDuration();
        int currentTime = (int) (getGSYVideoManager().getCurrentPosition() + time);
        if (currentTime > totalTimeDuration) {
            currentTime = totalTimeDuration;
        }
        String seekTime = CommonUtil.stringForTime(currentTime);
        String totalTime = CommonUtil.stringForTime(totalTimeDuration);
        this.getGSYVideoManager().seekTo(currentTime);

//        int finalCurrentTime = currentTime;
//        new Handler().postDelayed(() -> {
//            showProgressDialog(time, seekTime, finalCurrentTime, totalTime, totalTimeDuration);
//        }, 100);
//        new Handler().postDelayed(this::dismissProgressDialog, 600);
    }

    public void loadCoverImage(String url, int res) {
        mCoverOriginUrl = url;
        mDefaultRes = res;
        Glide.with(getContext().getApplicationContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(1000000)
                                .centerCrop()
                                .error(res)
                                .placeholder(res))
                .load(url)
                .into(mCoverImage);
    }

    public void loadCoverImageBy(int id, int res) {
        mCoverOriginId = id;
        mDefaultRes = res;
        mCoverImage.setImageResource(id);
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        SampleCoverVideo sampleCoverVideo = (SampleCoverVideo) gsyBaseVideoPlayer;
        if (mCoverOriginUrl != null) {
            sampleCoverVideo.loadCoverImage(mCoverOriginUrl, mDefaultRes);
        } else if (mCoverOriginId != 0) {
            sampleCoverVideo.loadCoverImageBy(mCoverOriginId, mDefaultRes);
        }
        return gsyBaseVideoPlayer;
    }


    @Override
    public GSYBaseVideoPlayer showSmallVideo(Point size, boolean actionBar, boolean statusBar) {
        //下面这里替换成你自己的强制转化
        SampleCoverVideo sampleCoverVideo = (SampleCoverVideo) super.showSmallVideo(size, actionBar, statusBar);
        sampleCoverVideo.mStartButton.setVisibility(GONE);
        sampleCoverVideo.mStartButton = null;
        return sampleCoverVideo;
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        super.cloneParams(from, to);
        SampleCoverVideo sf = (SampleCoverVideo) from;
        SampleCoverVideo st = (SampleCoverVideo) to;
        st.mShowFullAnimation = sf.mShowFullAnimation;
    }


    /**
     * 退出window层播放全屏效果
     */
    @SuppressWarnings("ResourceType")
    @Override
    protected void clearFullscreenLayout() {
        if (!mFullAnimEnd) {
            return;
        }
        mIfCurrentIsFullscreen = false;
        int delay = 0;
        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        if (mOrientationUtils != null) {
            delay = mOrientationUtils.backToProtVideo();
            mOrientationUtils.setEnable(false);
            if (mOrientationUtils != null) {
                mOrientationUtils.releaseListener();
                mOrientationUtils = null;
            }
        }

        if (!mShowFullAnimation) {
            delay = 0;
        }

        final ViewGroup vp = (CommonUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        final View oldF = vp.findViewById(getFullId());
        if (oldF != null) {
            //此处fix bug#265，推出全屏的时候，虚拟按键问题
            SampleCoverVideo gsyVideoPlayer = (SampleCoverVideo) oldF;
            gsyVideoPlayer.mIfCurrentIsFullscreen = false;
        }

        if (delay == 0) {
            backToNormal();
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    backToNormal();
                }
            }, delay);
        }

    }


    /******************* 下方两个重载方法，在播放开始前不屏蔽封面，不需要可屏蔽 ********************/
    @Override
    public void onSurfaceUpdated(Surface surface) {
        super.onSurfaceUpdated(surface);
        if (mThumbImageViewLayout != null && mThumbImageViewLayout.getVisibility() == VISIBLE) {
            mThumbImageViewLayout.setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void setViewShowState(View view, int visibility) {
        if (view == mThumbImageViewLayout && visibility != VISIBLE) {
            return;
        }
        super.setViewShowState(view, visibility);
    }

    @Override
    public void onSurfaceAvailable(Surface surface) {
        super.onSurfaceAvailable(surface);
        if (GSYVideoType.getRenderType() != GSYVideoType.TEXTURE) {
            if (mThumbImageViewLayout != null && mThumbImageViewLayout.getVisibility() == VISIBLE) {
                mThumbImageViewLayout.setVisibility(INVISIBLE);
            }
        }
    }

    /******************* 下方重载方法，在播放开始不显示底部进度和按键，不需要可屏蔽 ********************/

    protected boolean byStartedClick;

    @Override
    protected void onClickUiToggle(MotionEvent e) {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE);
            return;
        }
        byStartedClick = true;
        super.onClickUiToggle(e);

    }

    @Override
    protected void changeUiToNormal() {
        super.changeUiToNormal();
        byStartedClick = false;
    }

    @Override
    protected void changeUiToPreparingShow() {
        super.changeUiToPreparingShow();
        Debuger.printfLog("Sample changeUiToPreparingShow");
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mStartButton, INVISIBLE);
    }

    @Override
    protected void changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow();
        Debuger.printfLog("Sample changeUiToPlayingBufferingShow");
        if (!byStartedClick) {
            setViewShowState(mBottomContainer, INVISIBLE);
            setViewShowState(mStartButton, INVISIBLE);
        }
    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        Debuger.printfLog("Sample changeUiToPlayingShow");
        if (!byStartedClick) {
            setViewShowState(mBottomContainer, INVISIBLE);
            setViewShowState(mStartButton, INVISIBLE);
        }
    }

    /**
     * 是否展示拖动弹窗
     */
    private boolean isShowProgressDialog = true;

    public void setIsShowProgressBar(boolean isShowProgressDialog) {
        this.isShowProgressDialog = isShowProgressDialog;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        // 滚动seekBar时添加时间的监听
        float prioss = progress;
        if (fromUser && isShowProgressDialog) {
            setDialogProgressColor(Color.parseColor("#53CF8D"), -1);
            long duration = getGSYVideoManager().getCurrentPosition();
            int totalTimeDuration = (int) getDuration();
            long currentTime = (long) (prioss * 0.01 * totalTimeDuration);
            String seekTime = CommonUtil.stringForTime(currentTime);
            String totalTime = CommonUtil.stringForTime(totalTimeDuration);
            if (duration > currentTime) {
                prioss = -1;
            } else {
                prioss = 1;
            }
            float finalPrioss = prioss;
            new Handler().postDelayed(() -> {
                // time = 在当前的时间的基础上增加的时间、或者减少的时间 long、用来判断时快进还是快退， seektime = 格式化的时间 HH：mm，
                // finalCurrentTime = 当前时间+加增加的时间 long， totalTime = 总时间 HH：mm， totalTimeDuration = 总时间 long
                showProgressDialog(finalPrioss, seekTime, currentTime, totalTime, totalTimeDuration);
            }, 100);
            new Handler().postDelayed(this::dismissProgressDialog, 600);
        }
    }

    @Override
    public void startAfterPrepared() {
        super.startAfterPrepared();
        Debuger.printfLog("Sample startAfterPrepared");
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mStartButton, INVISIBLE);
        setViewShowState(mBottomProgressBar, VISIBLE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        byStartedClick = true;
        super.onStartTrackingTouch(seekBar);
    }
}
