package com.cl.common_base.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.cl.common_base.R;
import com.cl.common_base.util.ViewUtils;


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


    @Override
    public void onClick(View v) {
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
        }
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
