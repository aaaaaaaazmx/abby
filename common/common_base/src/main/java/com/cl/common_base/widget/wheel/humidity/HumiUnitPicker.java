package com.cl.common_base.widget.wheel.humidity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.cl.common_base.constants.Constants;
import com.cl.common_base.util.Prefs;
import com.cl.common_base.widget.wheel.WheelPicker;

import java.util.ArrayList;
import java.util.List;


/**
 * 湿度单位选择器
 */
public class HumiUnitPicker extends WheelPicker<String> {

    private OnScopeSelectedListener onSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HumiUnitPicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HumiUnitPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HumiUnitPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("%");
        updateHour();
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                if (onSelectedListener != null) {
                    onSelectedListener.onHumiUnitSelected(position);
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void updateHour() {
        List<String> list = new ArrayList<>();
        list.add("%");
        setDataList(list);
    }

    public interface OnScopeSelectedListener {
        void onHumiUnitSelected(int index);
    }
}