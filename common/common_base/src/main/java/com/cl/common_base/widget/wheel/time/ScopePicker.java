package com.cl.common_base.widget.wheel.time;

import android.content.Context;
import android.util.AttributeSet;

import com.bumptech.glide.load.model.ResourceLoader;
import com.cl.common_base.R;
import com.cl.common_base.widget.wheel.WheelPicker;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下午选择
 *
 * @author lijiewen
 * @date on 2019-11-09
 */
public class ScopePicker extends WheelPicker<String> {

    private OnScopeSelectedListener onSelectedListener;

    public ScopePicker(Context context) {
        this(context, null);
    }

    public ScopePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScopePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("AM");
        updateHour();
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                if (onSelectedListener != null) {
                    onSelectedListener.onScopeSelected(position);
                }
            }
        });
    }

    private void updateHour() {
        List<String> list = new ArrayList<>();
        list.add("AM");
        list.add("PM");
        setDataList(list);
    }

    public void setSelectedScope(int scope) {
        setSelectedScope(scope, true);
    }

    public void setSelectedScope(int scope, boolean smootScroll) {
        setCurrentPosition(scope, smootScroll);
    }

    public void setOnHourSelectedListener(OnScopeSelectedListener onHourSelectedListener) {
        onSelectedListener = onHourSelectedListener;
    }

    public interface OnScopeSelectedListener {
        void onScopeSelected(int index);
    }
}