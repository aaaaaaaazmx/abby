package com.cl.common_base.widget;

import static com.cl.common_base.ext.LogKt.logI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.cl.common_base.R;

/**
 * 功能设置项
 * switch
 */
public class FeatureItemSwitch extends FrameLayout implements View.OnClickListener {

    private AppCompatImageView ivItemIcon;

    private ImageView itemValueEndDrawable;
    private TextView tvItemTitle;
    private TextView tvItemHint;
    private SwitchButton swToggle;


    public FeatureItemSwitch(Context context) {
        this(context, null);
    }

    public FeatureItemSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeatureItemSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FeatureItemSwitch);
        Drawable itemIcon = typedArray.getDrawable(R.styleable.FeatureItemSwitch_itemIcon);
        float itemIconWidth = typedArray.getDimension(R.styleable.FeatureItemView_itemIconWidth, 0);
        float itemIconHeight = typedArray.getDimension(R.styleable.FeatureItemView_itemIconHeight, 0);
        String itemTitle = typedArray.getString(R.styleable.FeatureItemSwitch_itemTitle);
        String itemHint = typedArray.getString(R.styleable.FeatureItemSwitch_itemHint);
        boolean itemChecked = typedArray.getBoolean(R.styleable.FeatureItemSwitch_itemChecked, false);
        int itemBackgroundColor = typedArray.getColor(R.styleable.FeatureItemSwitch_itemBackgroundColor, Color.WHITE);
        boolean isItemTitleBold = typedArray.getBoolean(R.styleable.FeatureItemSwitch_itemTitleBold, false);
        Drawable itemValueEndImg = typedArray.getDrawable(R.styleable.FeatureItemSwitch_itemValueEndImg);
        typedArray.recycle();

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_switch, null);
        ivItemIcon = itemView.findViewById(R.id.fis_item_icon);
        tvItemTitle = itemView.findViewById(R.id.fis_item_title);
        tvItemTitle.setOnClickListener(this);
        tvItemHint = itemView.findViewById(R.id.fis_item_hint);
        swToggle = itemView.findViewById(R.id.fis_item_switch);
        itemValueEndDrawable = itemView.findViewById(R.id.fis_end_drawable);
        itemView.findViewById(R.id.ff_end_drawable).setOnClickListener(this);
        itemValueEndDrawable.setOnClickListener(this);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(itemView, lp);

        setItemIcon(itemIcon);
        setItemIconWidth(itemIconWidth);
        setItemIconHeight(itemIconHeight);
        setItemTitle(itemTitle);
        setItemHint(itemHint);
        setItemSwitch(itemChecked);
        setItemBackgroundColor(itemBackgroundColor);
        setItemTitleBold(isItemTitleBold);
        setTitleValueEndDrawable(itemValueEndImg);

    }

    public FeatureItemSwitch setTitleValueEndDrawable(Drawable drawable) {
        logI("123123");
        itemValueEndDrawable.setVisibility(View.VISIBLE);
        if (drawable == null) {
        } else {
            itemValueEndDrawable.setImageDrawable(drawable);
        }
        return this;
    }

    public FeatureItemSwitch setItemTitleBold(boolean isBold) {
        if (isBold) {
            tvItemTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return this;
    }


    public FeatureItemSwitch setItemIcon(Drawable drawable) {
        if (drawable == null) {
            ivItemIcon.setVisibility(GONE);
        } else {
            ivItemIcon.setVisibility(VISIBLE);
            ivItemIcon.setImageDrawable(drawable);
        }
        return this;
    }

    public FeatureItemSwitch setItemIconWidth(float size) {
        if (size > 0) {
            ViewGroup.LayoutParams params = ivItemIcon.getLayoutParams();
            params.width = (int) size;
        }
        return this;
    }

    public FeatureItemSwitch setItemIconHeight(float size) {
        if (size > 0) {
            ViewGroup.LayoutParams params = ivItemIcon.getLayoutParams();
            params.height = (int) size;
        }
        return this;
    }


    public FeatureItemSwitch setItemIconRes(int resId) {
        if (resId <= 0) {
            ivItemIcon.setVisibility(GONE);
        } else {
            ivItemIcon.setVisibility(VISIBLE);
            ivItemIcon.setImageResource(resId);
        }
        return this;
    }

    public FeatureItemSwitch setItemTitle(String itemTitle) {
        tvItemTitle.setText(itemTitle);
        return this;
    }

    public FeatureItemSwitch setItemHint(String hint) {
        if (!TextUtils.isEmpty(hint)) {
            tvItemHint.setText(hint);
            tvItemHint.setVisibility(VISIBLE);
        }
        return this;
    }

    public FeatureItemSwitch setItemSwitch(boolean itemSwitch) {
        swToggle.setChecked(itemSwitch);
        return this;
    }

    /**
     * 是否选中
     *
     * @return
     */
    public FeatureItemSwitch setItemChecked(boolean checked) {
        setItemChecked(checked, false, false);
        return this;
    }

    @BindingAdapter("isItemCheck")
    public static void setIsItemCheck(FeatureItemSwitch view, boolean isOffline) {
        view.setItemChecked(isOffline);
    }

    /**
     * 是否选中
     *
     * @return
     */
    public FeatureItemSwitch setItemChecked(boolean checked, boolean hasEffect, boolean hasEvent) {
        if (hasEffect) {
            if (hasEvent) {
                swToggle.setChecked(checked);
            } else {
                swToggle.setCheckedNoEvent(checked);
            }
        } else {
            if (hasEvent) {
                swToggle.setCheckedImmediately(checked);
            } else {
                swToggle.setCheckedImmediatelyNoEvent(checked);
            }
        }
        return this;
    }

    /**
     * 是否选中
     *
     * @return
     */
    public boolean isItemChecked() {
        return swToggle.isChecked();
    }


    /*public FeatureItemSwitch setSwitchClickListener(OnClickListener onClickListener) {
        swToggle.setOnClickListener(onClickListener);
        return this;
    }*/

    public FeatureItemSwitch setSwitchCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        swToggle.setOnCheckedChangeListener(onCheckedChangeListener);
        return this;
    }


    public void setItemBackgroundColor(int colorRes) {
        setBackgroundColor(colorRes);
    }


    private OnClickListener pointClickListener;

    public FeatureItemSwitch setPointClickListener(OnClickListener onClickListener) {
        this.pointClickListener = onClickListener;
        return this;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ff_end_drawable) {
            if (pointClickListener != null) {
                pointClickListener.onClick(view);
            }
        } if (view.getId() == R.id.fis_end_drawable) {
            if (pointClickListener != null) {
                pointClickListener.onClick(view);
            }
        } if (view.getId() == R.id.fis_item_title) {
            if (pointClickListener != null) {
                pointClickListener.onClick(view);
            }
        }
    }
}