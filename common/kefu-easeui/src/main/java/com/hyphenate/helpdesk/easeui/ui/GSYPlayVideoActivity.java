package com.hyphenate.helpdesk.easeui.ui;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;

import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.Message;
import com.hyphenate.helpdesk.R;
import com.hyphenate.helpdesk.easeui.video.SampleVideo;
import com.hyphenate.helpdesk.easeui.video.listener.OnTransitionListener;
import com.hyphenate.util.UriUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;


/**
 * 视频播放
 */
public class GSYPlayVideoActivity extends BaseActivity {

    public final static String IMG_TRANSITION = "IMG_TRANSITION";
    public final static String TRANSITION = "TRANSITION";
    OrientationUtils orientationUtils;

    private boolean isTransition;

    private Transition transition;

    private SampleVideo detailPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar(Color.BLACK);
        setContentView(R.layout.activity_play_video);
        detailPlayer = findViewById(R.id.detail_player);
        isTransition = getIntent().getBooleanExtra(TRANSITION, true);
        initView();
    }

    private void initView() {
        // 消息
        Message message = getIntent().getParcelableExtra("msg");
        String videoUrl = getIntent().getStringExtra("url");
        if (null != message) {
            EMVideoMessageBody messageBody = (EMVideoMessageBody)message.body();
            Uri localFilePath = messageBody.getLocalUri();
            if(UriUtils.isFileExistByUri(this, localFilePath)) {
                // 找到了路径
                // 播放视频
                detailPlayer.setUp(localFilePath.toString(), true, "");
                detailPlayer.startPlayLogic();
            }
        }
        if (null == message && !TextUtils.isEmpty(videoUrl)) {
            detailPlayer.setUp(videoUrl, true, "");
            detailPlayer.startPlayLogic();
        }

        String url = "https://res.exexm.com/cw_145225549855002";

        //String url = "http://7xse1z.com1.z0.glb.clouddn.com/1491813192";
        //需要路径的
        //detailPlayer.setUp(url, true, new File(FileUtils.getPath()), "");

        //增加封面
//        ImageView imageView = new ImageView(this);
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setImageResource(R.mipmap.xxx1);
//        detailPlayer.setThumbImageView(imageView);

        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        //detailPlayer.setShowPauseCover(false);

        //detailPlayer.setSpeed(2f);

        //设置返回键
        detailPlayer.getBackButton().setVisibility(View.VISIBLE);

        //设置旋转
        orientationUtils = new OrientationUtils(this, detailPlayer);

        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                orientationUtils.resolveByClick();
            }
        });

        //detailPlayer.setBottomProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_progress));
        //detailPlayer.setDialogVolumeProgressBar(getResources().getDrawable(R.drawable.video_new_volume_progress_bg));
        //detailPlayer.setDialogProgressBar(getResources().getDrawable(R.drawable.video_new_progress));
        //detailPlayer.setBottomShowProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_seekbar_progress),
        //getResources().getDrawable(R.drawable.video_new_seekbar_thumb));
        //detailPlayer.setDialogProgressColor(getResources().getColor(R.color.colorAccent), -11);

        //是否可以滑动调整
        detailPlayer.setIsTouchWiget(true);

        //设置返回按键功能
        detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //过渡动画
        initTransition();
        
    }
    @Override
    protected void onPause() {
        super.onPause();
        detailPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        detailPlayer.onVideoResume();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            detailPlayer.getFullscreenButton().performClick();
            return;
        }
        //释放所有
        detailPlayer.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
        if (isTransition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onBackPressed();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    // overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                }
            }, 100);
        }
    }


    private void initTransition() {
        if (isTransition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            ViewCompat.setTransitionName(detailPlayer, IMG_TRANSITION);
            addTransitionListener();
            startPostponedEnterTransition();
        } else {
            detailPlayer.startPlayLogic();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean addTransitionListener() {
        transition = getWindow().getSharedElementEnterTransition();
        if (transition != null) {
            transition.addListener(new OnTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    detailPlayer.startPlayLogic();
                    transition.removeListener(this);
                }
            });
            return true;
        }
        return false;
    }

}
