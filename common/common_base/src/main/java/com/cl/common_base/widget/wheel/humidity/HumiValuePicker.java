package com.cl.common_base.widget.wheel.humidity;

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
 * 湿度调节范围：0-100，默认值：40%，调节间隔：5（例如，5%、10%等）
 * 湿度选择器
 */
public class HumiValuePicker extends WheelPicker<String> {

    private OnScopeSelectedListener onSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HumiValuePicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HumiValuePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HumiValuePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("50");
        updateHour();
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                if (onSelectedListener != null) {
                    onSelectedListener.onHumiValueSelected(position);
                }
            }
        });
    }

    private void updateHour() {
        List<String> list = new ArrayList<>();
        // 一个for循环 0-100 间隔为5
        for (int i = 0; i < 101; i++) {
            if (i % 5 == 0) {
                list.add(i + "");
            }
        }
        setDataList(list);
    }

    // 设置值
    public void setSelectedScope(String value, boolean smootScroll) {
        List<String> dataList = getDataList();
        int i = Integer.parseInt(value);
        for (int i1 = 0; i1 < dataList.size(); i1++) {
            if (i == Integer.parseInt(dataList.get(i1))) {
                setCurrentPosition(i1, smootScroll);
                break;
            }
        }
    }


    public void setHumiValueSelectedListener(OnScopeSelectedListener onHumiValueSelectedListener) {
        onSelectedListener = onHumiValueSelectedListener;
    }

    public interface OnScopeSelectedListener {
        void onHumiValueSelected(int index);
    }
}
