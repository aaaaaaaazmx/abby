package com.cl.common_base.widget.wheel.temperature;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inspector.StaticInspectionCompanionProvider;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.load.model.ResourceLoader;
import com.cl.common_base.R;
import com.cl.common_base.constants.Constants;
import com.cl.common_base.util.Prefs;

import java.math.BigDecimal;
import java.security.PublicKey;

public class TemperaturePick extends LinearLayout implements TempScopePicker.OnScopeSelectedListener, TempUnitPicker.OnScopeSelectedListener, TempValuePicker.OnScopeSelectedListener {


    private TempScopePicker scopePicker;
    private View vLine;
    private TempValuePicker mHourPicker;
    private TempUnitPicker mMinutePicker;
    private int scope;


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public TemperaturePick(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public TemperaturePick(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public TemperaturePick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_picker_temp, this);
        initChild();
        initAttrs(context, attrs);
//        mHourPicker.setBackgroundDrawable(getBackground());
//        mMinutePicker.setBackgroundDrawable(getBackground());
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
        // setForce12HourMode(force12Hour);
    }

    private void initChild() {
        scopePicker = findViewById(R.id.scopePicker);
        vLine = findViewById(R.id.v_line);
        scopePicker.setTempScopeSelectedListener(this);
        mHourPicker = findViewById(R.id.valuePicker_layout);
        mHourPicker.setTempValueSelectedListener(this);
        mMinutePicker = findViewById(R.id.unitPicker_layout);
        mMinutePicker.setTempUnitSelectedListener(this);

        // refreshHourMode();
    }

    @Override
    public void onScopeSelected(int index) {
        scope = index;
        onTimeSelected();
    }

    /**
     * 设置选中
     *
     * @param scope
     * @param value
     */
    public void setChooseData(int scope, String value) {
        scopePicker.setSelectedScope(scope); // 0-1
        mHourPicker.setSelectedScope(value, false);
        mMinutePicker.setSelectedScope(0, false);
    }

    public int getScope() {
        return scopePicker.getCurrentPosition();
    }

    // 返回的是华氏度
    public String value() {
        // 有可能是华氏度
        String value = mHourPicker.getDataList().get(mHourPicker.getCurrentPosition());
        boolean isM = Prefs.INSTANCE.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
        if (isM) {
            // 转成华氏度\四舍五入
            int celsius = Integer.parseInt(value);
            /*double fahrenheit = (celsius * 1.8) + 32;*/
            // 截取整数
            BigDecimal fahrenheit = new BigDecimal(celsius).multiply(new BigDecimal("1.8")).add(new BigDecimal("32"));
            String roundedNum = fahrenheit.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            return roundedNum;
        } else {
            return value;
        }
    }

    @Override
    public void onTempUnitSelected(int index) {
        onTimeSelected();
    }

    @Override
    public void onTempValueSelected(int index) {
        onTimeSelected();
    }

    private void onTimeSelected() {
        if (mOnTempSelectedListener != null) {
            String hour = mHourPicker.getDataList().get(mHourPicker.getCurrentPosition());
            String min = mMinutePicker.getDataList().get(mMinutePicker.getCurrentPosition());
            String scope = scopePicker.getDataList().get(scopePicker.getCurrentPosition());
            mOnTempSelectedListener.onTempSelected(scope, hour, min);
        }
    }

    /**
     * Sets on date selected listener.
     *
     * @param onTempSelectedListener the on time selected listener
     */
    private OnTempSelectedListener mOnTempSelectedListener;

    public void setOnTempSelectedListener(OnTempSelectedListener onTempSelectedListener) {
        mOnTempSelectedListener = onTempSelectedListener;
    }

    /**
     * The interface On date selected listener.
     */
    public interface OnTempSelectedListener {
        /**
         * On time selected.
         *
         * @param scope the scope
         * @param value the value
         * @param unit  the unit
         */
        void onTempSelected(String scope, String value, String unit);
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
        scopePicker.setSelectedItemTextSize(selectedItemTextSize);
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

}