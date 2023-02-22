package com.cl.common_base.widget.wheel.time;

import static android.content.Intent.ACTION_TIME_CHANGED;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.cl.common_base.R;


/**
 * 时间选择器
 *
 * @author lijiewen
 * @date on 2019-10-13
 */
public class TimePicker extends LinearLayout implements ScopePicker.OnScopeSelectedListener, ScopeTimePicker.OnScopeTimeSelectedListener, MinutePicker.OnMinuteSelectedListener {

    private ScopePicker scopePicker;
    private View vLine;
    private ScopeTimePicker mHourPicker;
    private MinutePicker mMinutePicker;
    private OnTimeSelectedListener mOnTimeSelectedListener;
    private int scope;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public TimePicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_picker_time, this);
        initChild();
        initAttrs(context, attrs);
//        mHourPicker.setBackgroundDrawable(getBackground());
//        mMinutePicker.setBackgroundDrawable(getBackground());
    }

    @Override
    public void onScopeSelected(int index) {
        scope = index;
        onTimeSelected();
    }

    @Override
    public void onMinuteSelected(int hour) {
        onTimeSelected();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimePicker);
        int textSize = a.getDimensionPixelSize(R.styleable.TimePicker_itemTextSize, getResources().getDimensionPixelSize(R.dimen.WheelItemTextSize));
        int textColor = a.getColor(R.styleable.TimePicker_itemTextColor, Color.BLACK);
        boolean isTextGradual = a.getBoolean(R.styleable.TimePicker_textGradual, true);
        boolean isCyclic = a.getBoolean(R.styleable.TimePicker_wheelCyclic, false);
        int halfVisibleItemCount = a.getInteger(R.styleable.TimePicker_halfVisibleItemCount, 2);
        int selectedItemTextColor = a.getColor(R.styleable.TimePicker_selectedTextColor, Color.BLACK);
        int selectedItemTextSize = a.getDimensionPixelSize(R.styleable.TimePicker_selectedTextSize,
                getResources().getDimensionPixelSize(R.dimen.WheelSelectedItemTextSize));
        int itemWidthSpace = a.getDimensionPixelSize(R.styleable.TimePicker_itemWidthSpace,
                getResources().getDimensionPixelOffset(R.dimen.WheelItemWidthSpace));
        int itemHeightSpace = a.getDimensionPixelSize(R.styleable.TimePicker_itemHeightSpace,
                getResources().getDimensionPixelOffset(R.dimen.WheelItemHeightSpace));
        boolean isZoomInSelectedItem = a.getBoolean(R.styleable.TimePicker_zoomInSelectedItem, true);
        boolean force12Hour = a.getBoolean(R.styleable.TimePicker_force12Hour, true);
        boolean isShowCurtain = a.getBoolean(R.styleable.TimePicker_wheelCurtain, false);
        int curtainColor = a.getColor(R.styleable.TimePicker_wheelCurtainColor, Color.TRANSPARENT);
        boolean isShowCurtainBorder = a.getBoolean(R.styleable.TimePicker_wheelCurtainBorder, false);
        int curtainBorderColor = a.getColor(R.styleable.TimePicker_wheelCurtainBorderColor, Color.GRAY);
        a.recycle();

        setTextSize(textSize);
        setTextColor(textColor);
        setTextGradual(isTextGradual);
        setCyclic(isCyclic);
        setHalfVisibleItemCount(halfVisibleItemCount);
        setSelectedItemTextColor(selectedItemTextColor);
        setSelectedItemTextSize(selectedItemTextSize);
        setItemWidthSpace(itemWidthSpace);
        setItemHeightSpace(itemHeightSpace);
        setZoomInSelectedItem(isZoomInSelectedItem);
        setShowCurtain(isShowCurtain);
        setCurtainColor(curtainColor);
        setShowCurtainBorder(isShowCurtainBorder);
        setCurtainBorderColor(curtainBorderColor);
        setForce12HourMode(force12Hour);
    }

    private void initChild() {
        scopePicker = findViewById(R.id.scopePicker);
        vLine = findViewById(R.id.v_line);
        scopePicker.setOnHourSelectedListener(this);
        mHourPicker = findViewById(R.id.hourPicker_layout_time);
        mHourPicker.setOnHourSelectedListener(this);
        mMinutePicker = findViewById(R.id.minutePicker_layout_time);
        mMinutePicker.setOnMinuteSelectedListener(this);

        refreshHourMode();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(systemTimeChangedListener);
    }


    private void initListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        getContext().registerReceiver(systemTimeChangedListener, intentFilter);
    }


    private BroadcastReceiver systemTimeChangedListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_TIME_CHANGED.equals(intent.getAction())) {
                /*logI("groot-TimePicker", "system time has changed");*/
                refreshHourMode();
            }
        }
    };


    private void refreshHourMode() {
        // mHourPicker.setNormal12HourMode(isSystem12Hour());
        // ViewUtils.setVisible(mHourPicker.is12HourMode(), scopePicker, vLine);
    }


    private boolean isSystem12Hour() {
        ContentResolver cv = getContext().getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
        return ("12".equals(strTimeFormat));
    }


    private void onTimeSelected() {
        if (mOnTimeSelectedListener != null) {
            mOnTimeSelectedListener.onTimeSelected(getHour(), getMinute());
        }
    }


    /**
     * Sets time.
     *
     * @param hour   the year
     * @param minute the month
     */
    public void setTime(int hour, int minute) {
        setTime(hour, minute, true);
    }

    /**
     * Sets time.
     *
     * @param hour         the year
     * @param minute       the month
     * @param smoothScroll the smooth scroll
     */
    public void setTime(int hour, int minute, boolean smoothScroll) {
        scopePicker.setSelectedScope(hour >= 12 ? 1 : 0, false);
        mHourPicker.setSelectedScope(hour, smoothScroll);
        mMinutePicker.setSelectedMinute(minute, smoothScroll);
    }

    /**
     * Gets hour.
     *
     * @return the hour
     */
    public int getHour() {
        //统一返回24小时制
        String hour = mHourPicker.getDataList().get(mHourPicker.getCurrentPosition());
        int scopes = Integer.parseInt(hour.split(":")[0]);
        if (true) {
            if (scope == 0 && scopes == 12) {
                scopes = 0;
            } else if (scope == 1 && scopes < 12) {
                scopes += 12;
            }
        }
        return scopes;
    }


    /**
     * Gets minuute.
     *
     * @return the minute
     */
    public int getMinute() {
        return mMinutePicker.getDataList().get(mMinutePicker.getCurrentPosition());
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        if (mHourPicker != null) {
            mHourPicker.setBackgroundColor(color);
        }
        if (mMinutePicker != null) {
            mMinutePicker.setBackgroundColor(color);
        }
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        if (mHourPicker != null) {
            mHourPicker.setBackgroundResource(resid);
        }
        if (mMinutePicker != null) {
            mMinutePicker.setBackgroundResource(resid);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        if (mHourPicker != null) {
            mHourPicker.setBackgroundDrawable(background);
        }
        if (mMinutePicker != null) {
            mMinutePicker.setBackgroundDrawable(background);
        }
    }

    public ScopeTimePicker getHourPicker() {
        return mHourPicker;
    }

    public MinutePicker getMinutePicker() {
        return mMinutePicker;
    }

    /**
     * 一般列表的文本颜色
     *
     * @param textColor 文本颜色
     */
    public void setTextColor(int textColor) {
        scopePicker.setTextColor(textColor);
        mHourPicker.setTextColor(textColor);
        mMinutePicker.setTextColor(textColor);
    }

    /**
     * 一般列表的文本大小
     *
     * @param textSize 文字大小
     */
    public void setTextSize(int textSize) {
//        scopePicker.setTextSize((int) ResourceLoader.getDimen(getContext(), R.dimen.textSize16));
        mHourPicker.setTextSize(textSize);
        mMinutePicker.setTextSize(textSize);
    }

    /**
     * 设置被选中时候的文本颜色
     *
     * @param selectedItemTextColor 文本颜色
     */
    public void setSelectedItemTextColor(int selectedItemTextColor) {
        scopePicker.setSelectedItemTextColor(selectedItemTextColor);
        mHourPicker.setSelectedItemTextColor(selectedItemTextColor);
        mMinutePicker.setSelectedItemTextColor(selectedItemTextColor);
    }

    /**
     * 设置被选中时候的文本大小
     *
     * @param selectedItemTextSize 文字大小
     */
    public void setSelectedItemTextSize(int selectedItemTextSize) {
//        scopePicker.setSelectedItemTextSize((int) ResourceLoader.getDimen(getContext(), R.dimen.textSize16));
        mHourPicker.setSelectedItemTextSize(selectedItemTextSize);
        mMinutePicker.setSelectedItemTextSize(selectedItemTextSize);
    }


    /**
     * 设置显示数据量的个数的一半。
     * 为保证总显示个数为奇数,这里将总数拆分，itemCount = mHalfVisibleItemCount * 2 + 1
     *
     * @param halfVisibleItemCount 总数量的一半
     */
    public void setHalfVisibleItemCount(int halfVisibleItemCount) {
        mHourPicker.setHalfVisibleItemCount(halfVisibleItemCount);
        mMinutePicker.setHalfVisibleItemCount(halfVisibleItemCount);
    }

    /**
     * Sets item width space.
     *
     * @param itemWidthSpace the item width space
     */
    public void setItemWidthSpace(int itemWidthSpace) {
        mHourPicker.setItemWidthSpace(itemWidthSpace);
        mMinutePicker.setItemWidthSpace(itemWidthSpace);
    }

    /**
     * 设置两个Item之间的间隔
     *
     * @param itemHeightSpace 间隔值
     */
    public void setItemHeightSpace(int itemHeightSpace) {
        mHourPicker.setItemHeightSpace(itemHeightSpace);
        mMinutePicker.setItemHeightSpace(itemHeightSpace);
    }


    /**
     * Set zoom in center item.
     *
     * @param zoomInSelectedItem the zoom in center item
     */
    public void setZoomInSelectedItem(boolean zoomInSelectedItem) {
        scopePicker.setZoomInSelectedItem(zoomInSelectedItem);
        mHourPicker.setZoomInSelectedItem(zoomInSelectedItem);
        mMinutePicker.setZoomInSelectedItem(zoomInSelectedItem);
    }

    /**
     * 设置是否循环滚动。
     * set wheel cyclic
     *
     * @param cyclic 上下边界是否相邻
     */
    public void setCyclic(boolean cyclic) {
        mHourPicker.setCyclic(cyclic);
        mMinutePicker.setCyclic(cyclic);
    }

    /**
     * 设置文字渐变，离中心越远越淡。
     * Set the text color gradient
     *
     * @param textGradual 是否渐变
     */
    public void setTextGradual(boolean textGradual) {
        mHourPicker.setTextGradual(textGradual);
        mMinutePicker.setTextGradual(textGradual);
    }


    /**
     * 设置中心Item是否有幕布遮盖
     * set the center item curtain cover
     *
     * @param showCurtain 是否有幕布
     */
    public void setShowCurtain(boolean showCurtain) {
        mHourPicker.setShowCurtain(showCurtain);
        mMinutePicker.setShowCurtain(showCurtain);
    }

    /**
     * 设置幕布颜色
     * set curtain color
     *
     * @param curtainColor 幕布颜色
     */
    public void setCurtainColor(int curtainColor) {
        mHourPicker.setCurtainColor(curtainColor);
        mMinutePicker.setCurtainColor(curtainColor);
    }

    /**
     * 设置幕布是否显示边框
     * set curtain border
     *
     * @param showCurtainBorder 是否有幕布边框
     */
    public void setShowCurtainBorder(boolean showCurtainBorder) {
        mHourPicker.setShowCurtainBorder(showCurtainBorder);
        mMinutePicker.setShowCurtainBorder(showCurtainBorder);
    }

    /**
     * 幕布边框的颜色
     * curtain border color
     *
     * @param curtainBorderColor 幕布边框颜色
     */
    public void setCurtainBorderColor(int curtainBorderColor) {
        mHourPicker.setCurtainBorderColor(curtainBorderColor);
        mMinutePicker.setCurtainBorderColor(curtainBorderColor);
    }

    /**
     * 设置选择器的指示器文本
     * set indicator text
     *
     * @param hourText   小时指示器文本
     * @param minuteText 分钟指示器文本
     */
    public void setIndicatorText(String hourText, String minuteText) {
        mHourPicker.setIndicatorText(hourText);
        mMinutePicker.setIndicatorText(minuteText);
    }

    /**
     * 设置指示器文字的颜色
     * set indicator text color
     *
     * @param textColor 文本颜色
     */
    public void setIndicatorTextColor(int textColor) {
        mHourPicker.setIndicatorTextColor(textColor);
        mMinutePicker.setIndicatorTextColor(textColor);
    }

    /**
     * 设置指示器文字的大小
     * indicator text size
     *
     * @param textSize 文本大小
     */
    public void setIndicatorTextSize(int textSize) {
        mHourPicker.setTextSize(textSize);
        mMinutePicker.setTextSize(textSize);
    }


    public void setForce12HourMode(boolean is12HourMode) {
        // mHourPicker.setForce12HourMode(is12HourMode);
        // ViewUtils.setVisible(mHourPicker.is12HourMode(), scopePicker, vLine);
    }


    /**
     * Sets on date selected listener.
     *
     * @param onTimeSelectedListener the on time selected listener
     */
    public void setOnTimeSelectedListener(OnTimeSelectedListener onTimeSelectedListener) {
        mOnTimeSelectedListener = onTimeSelectedListener;
    }

    /**
     * The interface On date selected listener.
     */
    public interface OnTimeSelectedListener {
        /**
         * On time selected.
         *
         * @param hour   the hour
         * @param minute the minute
         */
        void onTimeSelected(int hour, int minute);
    }

    @Override
    public void onScopeTimeSelected(int index) {
        onTimeSelected();
    }
}
