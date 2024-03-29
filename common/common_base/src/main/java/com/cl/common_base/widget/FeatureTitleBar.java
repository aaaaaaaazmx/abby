package com.cl.common_base.widget;

import static com.cl.common_base.widget.slidetoconfirmlib.Util.dp2px;
import static com.cl.common_base.widget.slidetoconfirmlib.Util.sp2px;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.cl.common_base.R;
import com.cl.common_base.click.UncheckViewOnClick;
import com.cl.common_base.ext.DensityKt;
import com.cl.common_base.util.ViewUtils;
import com.cl.common_base.widget.toast.ToastUtil;


/**
 * 功能标题栏
 */
public class FeatureTitleBar extends LinearLayout implements View.OnClickListener {

    /**
     * 浅色主题
     */
    public static final int THEME_LIGHT = 1;
    /**
     * 深色主题
     */
    public static final int THEME_DARK = -1;

    private ImageView ivLeft;
    private TextView tvTitle, tvLeftName;
    private TextView tvRight;
    private ImageView ivRight;
    private ConstraintLayout clTitle;
    private OnClickListener leftClickListener;
    private OnClickListener rightClickListener;
    private OnClickListener quickClickListener;

    public FeatureTitleBar(Context context) {
        this(context, null);
    }

    public FeatureTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeatureTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FeatureTitleBar);
        String title = typedArray.getString(R.styleable.FeatureTitleBar_title);
        typedArray.recycle();
        View view = LayoutInflater.from(context).inflate(R.layout.in_title, null);
        ivLeft = view.findViewById(R.id.iv_back);
        clTitle = view.findViewById(R.id.cl_title);
        tvTitle = view.findViewById(R.id.tv_title);
        tvRight = view.findViewById(R.id.tv_right);
        ivRight = view.findViewById(R.id.tv_right_img);
        tvLeftName = view.findViewById(R.id.tv_left_name);
        tvLeftName.setOnClickListener(this);
        ivLeft.setOnClickListener(this);
        clTitle.setOnClickListener(this);
        tvRight.setOnClickListener(this);
        ivRight.setOnClickListener(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER & Gravity.CENTER_VERTICAL;
        // todo 添加间距
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            view.setFitsSystemWindows(true);
            setBackgroundColor(Color.WHITE);
            setFitsSystemWindows(true);
        }
        addView(view, lp);
        setTitle(title);
//        setTheme(THEME_DARK);
    }


    @UncheckViewOnClick
    @Override
    public void onClick(View v) {
        isFastClick();
        if (v.getId() == R.id.iv_back || v.getId() == R.id.tv_left_name) {
            if (leftClickListener != null) {
                leftClickListener.onClick(v);
            } else {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
            }
        } else if (v.getId() == R.id.tv_right || v.getId() == R.id.tv_right_img) {
            if (rightClickListener != null) {
                rightClickListener.onClick(v);
            }
        } else if (v.getId() == R.id.cl_title) {
            // 快速点击
            if (quickClickListener != null && isFastClick()) {
                quickClickListener.onClick(v);
            }
        }
    }

    /**
     * 快速点击
     */
    // 两次点击间隔不能少于1000ms
    private static final int FAST_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) <= FAST_CLICK_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }


    /**
     * 设置深浅色
     *
     * @param theme
     * @return
     */
    /*public FeatureTitleBar setTheme(int theme) {
        if (theme == THEME_LIGHT) {
            ivLeft.setImageResource(R.drawable.ic_back_white);
            tvTitle.setTextColor(ResourceLoader.getColor(getContext(), R.color.white));
            tvRight.setTextColor(ResourceLoader.getColor(getContext(), R.color.white));
//            statusBar.setTheme(RyStatusBar.THEME_LIGHT);
        } else {
            ivLeft.setImageResource(R.drawable.ic_back_black);
            tvTitle.setTextColor(ResourceLoader.getColor(getContext(), R.color.textMainLight));
//            tvTitle.setTextColor(ResourceLoader.getColorByAttr(getContext(), R.attr.attrTextMain));
//            tvRight.setTextColor(ResourceLoader.getColorByAttr(getContext(), R.attr.attrTextMain));
//            statusBar.setTheme(RyStatusBar.THEME_DARK);
        }
        return this;
    }*/


    /**
     * 设置深浅色
     *
     * @param theme
     * @return
     */
    /*@SuppressLint("ResourceAsColor")
    public FeatureTitleBar setThemeFlip(int theme) {
        if (theme == THEME_LIGHT) {
            clTitle.setBackgroundColor(ResourceLoader.getColor(getContext(), R.color.white));
            ivLeft.setImageResource(R.drawable.ic_back_black);
            tvTitle.setTextColor(ResourceLoader.getColor(getContext(), R.color.textMainLight));
//            statusBar.setTheme(RyStatusBar.THEME_LIGHT);
        } else {
            clTitle.setBackgroundColor(ResourceLoader.getColor(getContext(), R.color.bgPageDark));
            ivLeft.setImageResource(R.drawable.ic_back_white);
            tvTitle.setTextColor(ResourceLoader.getColor(getContext(), R.color.white));
            tvRight.setTextColor(ResourceLoader.getColor(getContext(), R.color.white));
//            tvTitle.setTextColor(ResourceLoader.getColorByAttr(getContext(), R.attr.attrTextMain));
//            tvRight.setTextColor(ResourceLoader.getColorByAttr(getContext(), R.attr.attrTextMain));
//            statusBar.setTheme(RyStatusBar.THEME_DARK);
        }
        return this;
    }*/


    /**
     * 设置标题
     *
     * @param title
     * @return
     */
    public FeatureTitleBar setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public FeatureTitleBar setTitleColor(int colors) {
        tvTitle.setTextColor(ContextCompat.getColor(getContext(), colors));
        return this;
    }

    /**
     * 设置标题栏背景色
     *
     * @param resId
     * @return
     */
    public FeatureTitleBar setTitleBackgroundRes(int resId) {
        tvTitle.setBackgroundResource(resId);
        return this;
    }


    /**
     * 设置标题
     *
     * @param visible
     * @return
     */
    public FeatureTitleBar setLeftVisible(boolean visible) {
        ViewUtils.setVisible(visible, ivLeft);
        return this;
    }


    /**
     * 设置左边文案
     */
    public FeatureTitleBar setLeftText(String text) {
        ViewUtils.setGone(ivLeft);
        ViewUtils.setVisible(tvLeftName);
        tvLeftName.setText(text);
        return this;
    }

    /**
     * 设置左边文案，并且加粗
     */
    public FeatureTitleBar setLeftText(String text, boolean isBold) {
        ViewUtils.setGone(ivLeft);
        ViewUtils.setVisible(tvLeftName);
        tvLeftName.setText(text);
        if (isBold) {
            tvLeftName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return this;
    }

    /**
     * 设置坐标标题图标
     */
    public FeatureTitleBar setLeftImageRes(int res) {
        ivLeft.setImageResource(res);
        return this;
    }

    /**
     * 设置右边文字是否显示
     *
     * @param visible
     * @return
     */
    public FeatureTitleBar setRightButtonTextVisible(boolean visible) {
        ViewUtils.setVisible(visible, tvRight);
        return this;
    }


    /**
     * 设置右边文字按钮
     *
     * @param text
     * @return
     */
    public FeatureTitleBar setRightButtonText(String text) {
        ivRight.setVisibility(GONE);
        tvRight.setText(text);
        tvRight.setVisibility(VISIBLE);
        return this;
    }

    public FeatureTitleBar setRightButtonTextSize(float size) {
        tvRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public FeatureTitleBar setRightButtonTextBack(int res) {
        ivRight.setVisibility(GONE);
        tvRight.setBackground(getResources().getDrawable(res));
        tvRight.setVisibility(VISIBLE);
        ConstraintLayout.MarginLayoutParams params = (ConstraintLayout.MarginLayoutParams) tvRight.getLayoutParams();
        params.setMargins(0, 0, DensityKt.dp2px(10), 0); // 你可以按需替换这些值
        tvRight.setLayoutParams(params);
        return this;
    }

    public FeatureTitleBar setRightButtonTextHeight(float height) {
        ConstraintLayout.MarginLayoutParams params = (ConstraintLayout.MarginLayoutParams) tvRight.getLayoutParams();
        params.height = DensityKt.dp2px(height);
        tvRight.setLayoutParams(params);
        return this;
    }

    /**
     * 设置右边文字按钮颜色
     *
     * @param color
     * @return
     */
    public FeatureTitleBar setRightButtonTextColor(int color) {
        tvRight.setTextColor(color);
        return this;
    }

    /**
     * 设置右边文字图片
     *
     * @param resId
     * @return
     */
    public FeatureTitleBar setRightButtonImg(int resId) {
        tvRight.setVisibility(GONE);
        ivRight.setImageResource(resId);
        ivRight.setVisibility(VISIBLE);
        return this;
    }

    public  FeatureTitleBar setRightButtonImg(int resId, boolean isShow) {
        tvRight.setVisibility(GONE);
        ivRight.setImageResource(resId);
        ivRight.setVisibility(isShow ? VISIBLE : GONE);
        return this;
    }


    /**
     * 设置返回键点击事件
     *
     * @param onClickListener
     * @return
     */
    public FeatureTitleBar setLeftClickListener(OnClickListener onClickListener) {
        this.leftClickListener = onClickListener;
        return this;
    }


    /**
     * 设置右边点击事件
     *
     * @param onClickListener
     * @return
     */
    public FeatureTitleBar setRightClickListener(OnClickListener onClickListener) {
        this.rightClickListener = onClickListener;
        return this;
    }

    /**
     * 快速点击
     */
    public FeatureTitleBar setQuickClickListener(OnClickListener onClickListener) {
        this.quickClickListener = onClickListener;
        return this;
    }


    /**
     * 设置右边是否可用
     *
     * @return
     */
    public FeatureTitleBar enableRightButton(boolean enable) {
        this.tvRight.setEnabled(enable);
        this.ivRight.setEnabled(enable);
        return this;
    }

    public String getTitleText() {
        return tvTitle.getText().toString();
    }
}