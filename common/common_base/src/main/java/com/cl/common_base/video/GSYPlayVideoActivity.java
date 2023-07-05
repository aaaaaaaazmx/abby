package com.cl.common_base.video;

import static com.cl.common_base.ext.LogKt.logI;

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

import androidx.core.view.ViewCompat;

import com.cl.common_base.R;
import com.cl.common_base.base.BaseActivity;
import com.cl.common_base.databinding.BaseActivityPlayVideoBinding;
import com.cl.common_base.video.listener.OnTransitionListener;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;


/**
 * 视频播放
 */
public class GSYPlayVideoActivity extends BaseActivity<BaseActivityPlayVideoBinding> {

    public final static String IMG_TRANSITION = "IMG_TRANSITION";
    public final static String TRANSITION = "TRANSITION";
    OrientationUtils orientationUtils;

    private boolean isTransition;

    private Transition transition;


    @Override
    public void initView() {
        isTransition = getIntent().getBooleanExtra(TRANSITION, true);

        // 消息
        String videoUrl = getIntent().getStringExtra("url");
        logI("12312312 videoUrl = " + videoUrl);

        if (!TextUtils.isEmpty(videoUrl)) {
            binding.detailPlayer.setUp(videoUrl, false, "");
            binding.detailPlayer.startPlayLogic();
        }

        // String url = "https://res.exexm.com/cw_145225549855002";

        //String url = "http://7xse1z.com1.z0.glb.clouddn.com/1491813192";
        //需要路径的
        //binding.detailPlayer.setUp(url, true, new File(FileUtils.getPath()), "");

        //增加封面
//        ImageView imageView = new ImageView(this);
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setImageResource(R.mipmap.xxx1);
//        binding.detailPlayer.setThumbImageView(imageView);

        //增加title
        binding.detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        //binding.detailPlayer.setShowPauseCover(false);

        //binding.detailPlayer.setSpeed(2f);

        //设置返回键
        binding.detailPlayer.getBackButton().setVisibility(View.VISIBLE);

        //设置旋转
        orientationUtils = new OrientationUtils(this, binding.detailPlayer);

        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        binding.detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                orientationUtils.resolveByClick();
            }
        });

        //binding.detailPlayer.setBottomProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_progress));
        //binding.detailPlayer.setDialogVolumeProgressBar(getResources().getDrawable(R.drawable.video_new_volume_progress_bg));
        //binding.detailPlayer.setDialogProgressBar(getResources().getDrawable(R.drawable.video_new_progress));
        //binding.detailPlayer.setBottomShowProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_seekbar_progress),
        //getResources().getDrawable(R.drawable.video_new_seekbar_thumb));
        //binding.detailPlayer.setDialogProgressColor(getResources().getColor(R.color.colorAccent), -11);

        //是否可以滑动调整
        binding.detailPlayer.setIsTouchWiget(true);

        //设置返回按键功能
        binding.detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
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
        binding.detailPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.detailPlayer.onVideoResume();
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
            binding.detailPlayer.getFullscreenButton().performClick();
            return;
        }
        //释放所有
        binding.detailPlayer.setVideoAllCallBack(null);
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
            ViewCompat.setTransitionName(binding.detailPlayer, IMG_TRANSITION);
            addTransitionListener();
            startPostponedEnterTransition();
        } else {
            binding.detailPlayer.startPlayLogic();
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
                    binding.detailPlayer.startPlayLogic();
                    transition.removeListener(this);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void observe() {

    }

    @Override
    public void initData() {

    }
}