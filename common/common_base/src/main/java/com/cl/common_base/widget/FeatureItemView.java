package com.cl.common_base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.load.model.ResourceLoader;
import com.cl.common_base.R;
import com.cl.common_base.ext.DensityKt;
import com.cl.common_base.util.ViewUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;


/**
 * 功能设置项
 */
public class FeatureItemView extends FrameLayout implements View.OnClickListener {

    private ImageView ivItemIcon;
    private ImageView ivItemImage, itemValueEndDrawable;
    private TextView tvItemTitle, tvItemImage;
    private TextView tvItemHint;
    private TextView tvItemValue;
    private View vRedDot;
    private ImageView ivItemArrow;
    public SvTextView svtText;

    public FeatureItemView(Context context) {
        this(context, null);
    }

    public FeatureItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeatureItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FeatureItemView);
        Drawable itemIcon = typedArray.getDrawable(R.styleable.FeatureItemView_itemIcon);
        Drawable itemImg = typedArray.getDrawable(R.styleable.FeatureItemView_itemImg);
        float itemIconWidth = typedArray.getDimension(R.styleable.FeatureItemView_itemIconWidth, 0);
        float itemIconHeight = typedArray.getDimension(R.styleable.FeatureItemView_itemIconHeight, 0);
        String itemTitle = typedArray.getString(R.styleable.FeatureItemView_itemTitle);
        String itemHint = typedArray.getString(R.styleable.FeatureItemView_itemHint);
        int itemTitleColor = typedArray.getColor(R.styleable.FeatureItemView_itemTitleColor, Color.BLACK);
        String itemContent = typedArray.getString(R.styleable.FeatureItemView_itemValue);
        int itemContentColor = typedArray.getColor(R.styleable.FeatureItemView_itemValueColor, Color.parseColor("#161B19"));
        int itemBackgroundColor = typedArray.getColor(R.styleable.FeatureItemView_itemBackgroundColor, Color.WHITE);
        float itemContentSize = typedArray.getDimension(R.styleable.FeatureItemView_itemValueSize, 0);
        boolean hideArrow = typedArray.getBoolean(R.styleable.FeatureItemView_hideArrow, false);
        boolean isItemTitleBold = typedArray.getBoolean(R.styleable.FeatureItemView_itemTitleBold, false);
        typedArray.recycle();

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_view, null);
        ivItemIcon = itemView.findViewById(R.id.fiv_item_icon);
        ivItemImage = itemView.findViewById(R.id.fiv_item_img);
        itemValueEndDrawable = itemView.findViewById(R.id.item_value_end_drawable);
        itemValueEndDrawable.setOnClickListener(this);
        tvItemImage = itemView.findViewById(R.id.nohead_show);
        tvItemTitle = itemView.findViewById(R.id.fiv_item_title);
        tvItemHint = itemView.findViewById(R.id.fiv_item_hint);
        tvItemValue = itemView.findViewById(R.id.fiv_item_value);
        vRedDot = itemView.findViewById(R.id.fiv_red_dot);
        ivItemArrow = itemView.findViewById(R.id.fiv_item_switch);
        svtText = itemView.findViewById(R.id.svt_text);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(itemView, lp);

        setItemIcon(itemIcon);
        setItemImg(itemImg);
        setItemIconWidth(itemIconWidth);
        setItemIconHeight(itemIconHeight);
        setItemTitle(itemTitle);
        setItemHint(itemHint);
        setItemTitleColor(itemTitleColor);
        setItemValue(itemContent);
        setItemValueColor(itemContentColor);
        setItemValueSize(itemContentSize);
        setHideArrow(hideArrow);
        setItemTitleBold(isItemTitleBold);
        setItemBackgroundColor(itemBackgroundColor);
    }


    public FeatureItemView setItemIcon(Drawable drawable) {
        if (drawable == null) {
            ivItemIcon.setVisibility(GONE);
        } else {
            ivItemIcon.setVisibility(VISIBLE);
            ivItemIcon.setImageDrawable(drawable);
        }
        return this;
    }

    public FeatureItemView setItemIconRes(int resId) {
        if (resId <= 0) {
            ivItemIcon.setVisibility(GONE);
        } else {
            ivItemIcon.setVisibility(VISIBLE);
            ivItemIcon.setImageResource(resId);
        }
        return this;
    }

    public FeatureItemView setItemImg(Drawable drawable) {
        if (drawable == null) {
            ivItemImage.setVisibility(GONE);
        } else {
            ivItemImage.setVisibility(VISIBLE);
            ivItemImage.setImageDrawable(drawable);
        }
        return this;
    }

    public FeatureItemView setItemImgRes(int resId) {
        if (resId <= 0) {
            ivItemImage.setVisibility(GONE);
        } else {
            ivItemImage.setVisibility(VISIBLE);
            ivItemImage.setImageResource(resId);
        }
        return this;
    }

    /**
     * 加载网络图片
     */
    public FeatureItemView setImageForUrl(String url, boolean isCircle) {
        // 显示出来
        ivItemImage.setVisibility(VISIBLE);
        tvItemImage.setVisibility(GONE);
        if (isCircle) {
            Glide.with(this).load(url).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(ivItemImage);//标准圆形图片。
        } else {
            Glide.with(this).load(url).into(ivItemImage);
        }
        return this;
    }

    /**
     * 显示文字按钮
     */
    public FeatureItemView setTvItemImage(String name) {
        tvItemImage.setVisibility(VISIBLE);
        ivItemImage.setVisibility(GONE);
        if (!TextUtils.isEmpty(name)) {
            tvItemImage.setText(name);
        }
        return this;
    }

    public FeatureItemView setImageForUri(Uri uri, boolean isCircle) {
        // 显示出来
        ivItemImage.setVisibility(VISIBLE);
        if (isCircle) {
            Glide.with(this).load(uri).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(ivItemImage);//标准圆形图片。
        } else {
            Glide.with(this).load(uri).into(ivItemImage);
        }
        return this;
    }

    public FeatureItemView setItemIconWidth(float size) {
        if (size > 0) {
            ViewGroup.LayoutParams params = ivItemIcon.getLayoutParams();
            params.width = (int) size;
        }
        return this;
    }

    public FeatureItemView setItemIconHeight(float size) {
        if (size > 0) {
            ViewGroup.LayoutParams params = ivItemIcon.getLayoutParams();
            params.height = (int) size;
        }
        return this;
    }

    public FeatureItemView setItemTitle(String title) {
        tvItemTitle.setText(title);
        return this;
    }

    /**
     * 设置左边文案，并且加粗
     */
    public FeatureItemView setItemTitle(String title, boolean isBold) {
        tvItemTitle.setText(title);
        if (isBold) {
            tvItemTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return this;
    }

    public FeatureItemView setItemTitleColor(int color) {
        tvItemTitle.setTextColor(color);
        return this;
    }

    public FeatureItemView setItemHint(String hint) {
        if (!TextUtils.isEmpty(hint)) {
            tvItemHint.setText(hint);
            tvItemHint.setVisibility(VISIBLE);
        }
        return this;
    }


    public FeatureItemView setItemValue(String itemValue) {
        tvItemValue.setText(itemValue);
        return this;
    }

    // 设置箭头改为圆角
    public FeatureItemView setSvText(String itemValue) {
        svtText.setText(itemValue);
        ViewUtils.setVisible(svtText);
        ViewUtils.setGone(tvItemValue);
        ViewUtils.setGone(ivItemArrow);
        return this;
    }

    public FeatureItemView setSvTextSize(float svTextSize) {
        svtText.setTextSize(DensityKt.px2dp(svTextSize));
        return this;
    }

    public String getSvTextValue() {
        return svtText.getText().toString();
    }

    /**
     * 设置字符串最大显示长度
     */
    public FeatureItemView setItemValue(String itemValue, int maxEms) {
        tvItemValue.setMaxEms(maxEms);
        tvItemValue.setMaxLines(1);
        tvItemValue.setEllipsize(TextUtils.TruncateAt.END);
        tvItemValue.setText(itemValue);
        return this;
    }

    /**
     * 设置文字,并且设置颜色
     */
    public FeatureItemView setItemValueWithColor(String itemValue, String color) {
        tvItemValue.setText(itemValue);
        tvItemValue.setTextColor(Color.parseColor(color));
        return this;
    }

    public FeatureItemView setItemValueVisible(boolean visible) {
        if (tvItemValue != null) {
            tvItemValue.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public String getItemValue() {
        return tvItemValue.getText().toString();
    }

    public FeatureItemView setItemValueColor(int color) {
        tvItemValue.setTextColor(color);
        return this;
    }

    public FeatureItemView setItemValueSize(float size) {
        if (size > 0) {
            tvItemValue.setTextSize(size);
        }
        return this;
    }


    public FeatureItemView setShowRedDot(boolean show) {
        vRedDot.setVisibility(show ? VISIBLE : GONE);
        return this;
    }

    public FeatureItemView setHideArrow(boolean hide) {
        ivItemArrow.setVisibility(hide ? GONE : VISIBLE);
        return this;
    }

    public FeatureItemView setItemTitleBold(boolean isBold) {
        if (isBold) {
            tvItemTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return this;
    }

    public FeatureItemView setImageArrow(int res) {
        ivItemArrow.setImageResource(res);
        return this;
    }

    public FeatureItemView setItemHintVisible(boolean visible) {
        tvItemHint.setVisibility(visible ? VISIBLE : GONE);
        return this;
    }

    @BindingAdapter("isOffline")
    public static void setTextForDeviceOffline(FeatureItemView view, boolean isOffline) {
        if (isOffline) {
            view.setEnable(false);
            view.tvItemTitle.setTextColor(Color.parseColor("#979797"));
            view.ivItemArrow.setBackgroundResource(R.mipmap.base_ic_arrow_right);
        } else {
            view.setEnable(true);
            view.tvItemTitle.setTextColor(Color.BLACK);
            view.ivItemArrow.setBackgroundResource(R.mipmap.iv_right);
        }

    }

    public FeatureItemView setEnable(boolean enable) {
        setClickable(enable);
        super.setEnabled(enable);
        tvItemTitle.setEnabled(enable);
        tvItemValue.setEnabled(enable);
        return this;
    }

    public FeatureItemView setTitleValueEndDrawable(Drawable drawable) {
        itemValueEndDrawable.setVisibility(View.VISIBLE);
        if (drawable == null) {
        } else {
            itemValueEndDrawable.setImageDrawable(drawable);
        }
        return this;
    }


    public void setItemBackgroundColor(int colorRes) {
        setBackgroundColor(colorRes);
    }

    private OnClickListener pointClickListener;

    public FeatureItemView setPointClickListener(OnClickListener onClickListener) {
        this.pointClickListener = onClickListener;
        return this;
    }

    @Override
    public void onClick(View view) {
       if (view.getId() == R.id.item_value_end_drawable) {
           if (pointClickListener != null) {
               pointClickListener.onClick(view);
           }
       }
    }
}
