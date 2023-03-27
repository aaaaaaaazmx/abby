package com.cl.common_base.widget.wheel.time;

import static com.cl.common_base.ext.DateHelper.formatTime;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.cl.common_base.widget.wheel.WheelPicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 上下午选择
 *
 * @author lijiewen
 * @date on 2019-11-09
 */
public class ScopeTimePicker extends WheelPicker<String> {

    private OnScopeTimeSelectedListener onSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public ScopeTimePicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public ScopeTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public ScopeTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("00:00");
        updateHour();
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                if (onSelectedListener != null) {
                    onSelectedListener.onScopeTimeSelected(position);
                }
            }
        });
    }

    private void updateHour() {
        List<String> list = new ArrayList<>();
        if (true) {
            for (int i = 1; i < 13; i++) {
                list.add(i + ":00");
            }
        } else {
            for (int i = 0; i < 24; i++) {
                list.add(i + ":00");
            }
        }
        setDataList(list);
    }

    public void setSelectedScope(int scope) {
        setSelectedScope(scope, true);
    }

    // scope 24小时
    public void setSelectedScope(int scope, boolean smootScroll) {
        List<String> dataList = getDataList();

        if (scope > 12) {
            for (String s : dataList) {
                String hour = s.split(":")[0];
                if (hour.equals((scope - 12)+"")) {
                    setCurrentPosition(dataList.indexOf(s), smootScroll);
                    return;
                }
            }

        }
        if (scope <= 12) {
            for (String s : dataList) {
                String hour = s.split(":")[0];
                if (hour.equals(scope+"")) {
                    setCurrentPosition(dataList.indexOf(s), smootScroll);
                    return;
                }
            }
        }
        setCurrentPosition(scope, smootScroll);
    }

    public void setOnHourSelectedListener(OnScopeTimeSelectedListener onHourSelectedListener) {
        onSelectedListener = onHourSelectedListener;
    }

    public interface OnScopeTimeSelectedListener {
        void onScopeTimeSelected(int index);
    }
}
