package com.cl.common_base.widget.wheel.temperature;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.cl.common_base.constants.Constants;
import com.cl.common_base.util.Prefs;
import com.cl.common_base.widget.wheel.WheelPicker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * 温度调节范围：50-100，默认值：70，调节间隔：1
 * 温度选择器
 */
public class TempValuePicker extends WheelPicker<String> {

    private OnScopeSelectedListener onSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempValuePicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempValuePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public TempValuePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("50");
        updateHour();
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                if (onSelectedListener != null) {
                    onSelectedListener.onTempValueSelected(position);
                }
            }
        });
    }

    private void updateHour() {
        boolean isMetric = Prefs.INSTANCE.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
        List<String> list = new ArrayList<>();
        if (isMetric) {
            for (int i = 10; i <= 38; i++) {
                list.add(i + "");
            }
            setDataList(list);
            return;
        }
        // 展示50-100的区间
        for (int i = 50; i < 101; i++) {
            list.add(i + "");
        }
        setDataList(list);
    }

    // 设置值
    // 设置进来的有可能是摄氏度，也有可能是华氏度
    public void setSelectedScope(String value, boolean smootScroll) {
        List<String> dataList = getDataList();
        int i = Integer.parseInt(value);
        boolean isMetric = Prefs.INSTANCE.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
        int tempValue = i;
        if (isMetric) {
            try {
                // 转成摄氏度\四舍五入
                // (1°F − 32) × 5/9
                // String result1 = String.format("%.2f", d);
                tempValue = new BigDecimal(i).subtract(new BigDecimal("32")).multiply(new BigDecimal("5")).divide(new BigDecimal("9"), 0, BigDecimal.ROUND_HALF_UP).intValue();
                /*tempValue = Integer.parseInt(String.format("%d", i1.intValue()));*/
            } catch (Exception e) {
                tempValue = i;
            }
        } else {
            tempValue = i;
        }
        for (int i1 = 0; i1 < dataList.size(); i1++) {
            if (dataList.get(i1).equals("" + tempValue)) {
                setCurrentPosition(i1, smootScroll);
                break;
            }
        }
    }


    public void setTempValueSelectedListener(OnScopeSelectedListener onTempValueSelectedListener) {
        onSelectedListener = onTempValueSelectedListener;
    }

    public interface OnScopeSelectedListener {
        void onTempValueSelected(int index);
    }
}