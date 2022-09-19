package com.cl.common_base.widget.wheel.time;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.cl.common_base.widget.wheel.WheelPicker;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 分钟选择
 *
 * @author lijiewen
 * @date on 2019-11-09
 */
public class MinutePicker extends WheelPicker<Integer> {
    private OnMinuteSelectedListener mOnMinuteSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public MinutePicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public MinutePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public MinutePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("00");
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumIntegerDigits(2);
        setDataFormat(numberFormat);
        updateMinute();
        setOnWheelChangeListener(new OnWheelChangeListener<Integer>() {
            @Override
            public void onWheelSelected(Integer item, int position) {
                if (mOnMinuteSelectedListener != null) {
                    mOnMinuteSelectedListener.onMinuteSelected(item);
                }
            }
        });
    }

    private void updateMinute() {
        List<Integer> list = new ArrayList<>();
//        for (int i = 0; i < 60; i++) {
//            list.add(i);
//        }

        // 变成了0 12 30 45 的叠加
        for (int i = 0; i < 4; i ++) {
            switch (i) {
                case 0 :
                    list.add(0);
                    break;
                case 1 :
                    list.add(15);
                    break;
                case 2 :
                    list.add(30);
                    break;
                case 3 :
                    list.add(45);
                    break;
            }
        }
        setDataList(list);
    }

    public void setSelectedMinute(int hour) {
        setSelectedMinute(hour, true);
    }

    public void setSelectedMinute(int hour, boolean smootScroll) {
        setCurrentPosition(hour, smootScroll);
    }

    public void setOnMinuteSelectedListener(OnMinuteSelectedListener onMinuteSelectedListener) {
        mOnMinuteSelectedListener = onMinuteSelectedListener;
    }

    public interface OnMinuteSelectedListener {
        void onMinuteSelected(int hour);
    }
}
