package com.cl.common_base.widget.wheel.temperature;

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
 * 温度单位选择器
 */
public class TempUnitPicker extends WheelPicker<String> {

    private OnScopeSelectedListener onSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempUnitPicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempUnitPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempUnitPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("°C");
        updateHour();
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                if (onSelectedListener != null) {
                    onSelectedListener.onTempUnitSelected(position);
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void updateHour() {
        boolean isMetric = Prefs.INSTANCE.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
        List<String> list = new ArrayList<>();
        if (!isMetric) {
            list.add("℉");
        } else {
            list.add("°C");
        }
        setDataList(list);
    }

    public void setSelectedScope(int scope, boolean smootScroll) {
        boolean isMetric = Prefs.INSTANCE.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
        List<String> list = new ArrayList<>();
        if (!isMetric) {
            list.add("℉");
        } else {
            list.add("°C");
        }
        setDataList(list);
        setCurrentPosition(scope, smootScroll);
    }

    public void setTempUnitSelectedListener(OnScopeSelectedListener onTempUnitSelectedListener) {
        onSelectedListener = onTempUnitSelectedListener;
    }

    public interface OnScopeSelectedListener {
        void onTempUnitSelected(int index);
    }
}