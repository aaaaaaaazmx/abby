package com.cl.modules_my.ui;

import static com.cl.common_base.ext.LogKt.logI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cl.common_base.base.BaseActivity;
import com.cl.common_base.util.StatusBarUtil;
import com.cl.common_base.widget.crop.ClipViewLayout;
import com.cl.modules_my.R;
import com.cl.modules_my.databinding.MyActivityClipImageBinding;
import com.cl.modules_my.databinding.MyProfileActivityBinding;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 裁剪Activity
 */
public class ClipImageActivity extends BaseActivity<MyActivityClipImageBinding> implements View.OnClickListener {
    private static final String TAG = "ClipImageActivity";
    private ClipViewLayout clipViewLayout1;
    private ClipViewLayout clipViewLayout2;
    private ImageView back;
    private TextView btnCancel;
    private TextView btnOk;
    /**
     * 类别 1: qq, 2: wechat（圆形 方形）
     */
    private int type;
    private int mWidth;
    private int mHeight;

    @Override
    public void observe() {

    }

    /**
     * 初始化组件
     */
    @Override
    public void initView() {
        StatusBarUtil.setDarkMode(this);
        StatusBarUtil.setTranslucent(this);
        type = getIntent().getIntExtra("type", 1);
        mWidth = getIntent().getIntExtra("width", 0);
        mHeight = getIntent().getIntExtra("height", 0);
        clipViewLayout1 = findViewById(R.id.clipViewLayout1);
        clipViewLayout2 = findViewById(R.id.clipViewLayout2);
        if (mWidth != 0 && mHeight != 0) {
            logI("width = " + mWidth + " :: height = " + mHeight);
            clipViewLayout2.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    int layoutWidth = (int) getResources().getDimension(R.dimen.ry_px_317);
                    clipViewLayout2.setClipViewWidthAndHeiht(layoutWidth, (int) (mHeight / ((float) mWidth) * layoutWidth));
                    clipViewLayout2.removeOnLayoutChangeListener(this);
                }
            });
        }

        back = findViewById(R.id.iv_back);
        btnCancel = findViewById(R.id.btn_cancel);
        btnOk = findViewById(R.id.bt_ok);
        //设置点击事件监听器
        back.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void initData() {
        Log.i(TAG, "image uri: " + getIntent().getData());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String uri = bundle.getString("uri");
            if (!TextUtils.isEmpty(uri)) {
                type = bundle.getInt("type");
                if (type == 1) {
                    clipViewLayout1.setVisibility(View.VISIBLE);
                    clipViewLayout2.setVisibility(View.GONE);
                    //设置图片资源
                    clipViewLayout1.setImageSrc1(Uri.parse(uri));

                } else {
                    clipViewLayout2.setVisibility(View.VISIBLE);
                    clipViewLayout1.setVisibility(View.GONE);
                    //设置图片资源
                    clipViewLayout2.setImageSrc2(Uri.parse(uri),()->{});
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.btn_cancel) {
            finish();
        } else if (v.getId() == R.id.bt_ok) {
            generateUriAndReturn();
        }
    }


    /**
     * 生成Uri并且通过setResult返回给打开的activity
     */
    private void generateUriAndReturn() {
        //调用返回剪切图
        Bitmap zoomedCropBitmap;
        if (type == 1) {
            zoomedCropBitmap = clipViewLayout1.clip1();
        } else {
            zoomedCropBitmap = clipViewLayout2.clip2();
        }
        if (zoomedCropBitmap == null) {
            Log.e("android", "zoomedCropBitmap == null");
            return;
        }
        Uri mSaveUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = getContentResolver().openOutputStream(mSaveUri);
                if (outputStream != null) {
                    zoomedCropBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            } catch (IOException ex) {
                Log.e("android", "Cannot open file: " + mSaveUri, ex);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Intent intent = new Intent();
            intent.setData(mSaveUri);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
