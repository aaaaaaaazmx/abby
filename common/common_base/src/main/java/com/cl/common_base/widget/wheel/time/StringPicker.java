package com.cl.common_base.widget.wheel.time;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.cl.common_base.widget.wheel.WheelPicker;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用String类型选
 *
 * @author lijiewen
 * @date on 2019-11-09
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class StringPicker extends WheelPicker<String> {

    private OnStringSelectedListener onSelectedListener;

    public StringPicker(Context context) {
        this(context, null);
    }

    public StringPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public StringPicker(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public void setStringList(List<String> list) {
        setDataList(list);
    }

    public void setSelectedScope(int scope) {
        setSelectedScope(scope, true);
    }

    public void setSelectedScope(int scope, boolean smootScroll) {
        setCurrentPosition(scope, smootScroll);
    }

    public void setOnStringSelectedListener(OnStringSelectedListener onHourSelectedListener) {
        onSelectedListener = onHourSelectedListener;
    }

    public interface OnStringSelectedListener {
        void onScopeSelected(int index);
    }
}