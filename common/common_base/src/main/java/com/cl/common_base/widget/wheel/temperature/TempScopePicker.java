package com.cl.common_base.widget.wheel.temperature;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.cl.common_base.widget.wheel.WheelPicker;

import java.util.ArrayList;
import java.util.List;


/**
 * 温度大于小于区间选择
 */
public class TempScopePicker extends WheelPicker<String> {

    private OnScopeSelectedListener onSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempScopePicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempScopePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempScopePicker(Context context, AttributeSet attrs, int defStyleAttr) {
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
        list.add("≥");
        list.add("≤");
        setDataList(list);
    }

    public void setSelectedScope(int scope) {
        setSelectedScope(scope, true);
    }

    public void setSelectedScope(int scope, boolean smootScroll) {
        setCurrentPosition(scope, smootScroll);
    }

    public void setTempScopeSelectedListener(OnScopeSelectedListener onTempScopeSelectedListener) {
        onSelectedListener = onTempScopeSelectedListener;
    }

    public interface OnScopeSelectedListener {
        void onScopeSelected(int index);
    }
}