package com.cl.common_base.web;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cl.common_base.R;
import com.cl.common_base.util.StatusBarUtil;
import com.cl.common_base.widget.FeatureTitleBar;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

/**
 * 加载网页Activity
 */
public class BaseWebActivity extends AppCompatActivity {
    protected AgentWeb mAgentWeb;
    private LinearLayout mLinearLayout;
    private FeatureTitleBar mToolbar;
    private AlertDialog mAlertDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setColor(this, Color.TRANSPARENT, 0);
        StatusBarUtil.setLightMode(this);
        StatusBarUtil.transparentNavBar(this);

        setContentView(R.layout.activity_web);

        mLinearLayout =  this.findViewById(R.id.container);
        mToolbar =  this.findViewById(R.id.ft_bar);
        mToolbar.setTitle(getName());
        // 是否显示购物车按钮
        mToolbar.setRightButtonImg(R.mipmap.contact_car_bg, showCar()).setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到购物车
                mAgentWeb.getUrlLoader().loadUrl("https://heyabby.com/cart");
            }
        });
        mToolbar.setLeftClickListener(v -> {
            // 返回键
            finish();
//                showDialog();
        });


        long p = System.currentTimeMillis();

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl() //拦截找不到相关页面的Scheme
                .createAgentWeb()
                .ready()
                .go(getUrl());

        //mAgentWeb.getUrlLoader().loadUrl(getUrl());

        long n = System.currentTimeMillis();
        Log.i("Info", "init used time:" + (n - p));


    }

    private com.just.agentweb.WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //do you  work
            Log.i("Info", "BaseWebActivity onPageStarted");
        }
    };
    private com.just.agentweb.WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            // 这个是从网页里面获取标题展示
            if (TextUtils.isEmpty(getName())) {
                mToolbar.setTitle(title);
            }
        }
    };

    public String getUrl() {
        return "https://m.jd.com/";
    }

    // 获取名字
    public String getName() {
        return "";
    }

    public boolean showCar() {
        return false;
    }

    private void showDialog() {

        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this)
                    .setMessage("您确定要关闭该页面吗?")
                    .setNegativeButton("再逛逛", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mAlertDialog != null) {
                                mAlertDialog.dismiss();
                            }
                        }
                    })//
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (mAlertDialog != null) {
                                mAlertDialog.dismiss();
                            }
                            BaseWebActivity.this.finish();
                        }
                    }).create();
        }
        mAlertDialog.show();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("Info", "onResult:" + requestCode + " onResult:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mAgentWeb.destroy();
        mAgentWeb.getWebLifeCycle().onDestroy();
    }
}