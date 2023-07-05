package com.cl.common_base.widget.wheel.time;

import static com.cl.common_base.ext.LogKt.logI;

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
                    mOnMinuteSelectedListener.onMinuteSelected(item == null ? 0 : item);
                }
            }
        });
    }

    private void updateMinute() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                list.add(i);
            }
        }
        setDataList(list);
    }

    public void setSelectedMinute(int minute) {
        setSelectedMinute(minute, true);
        if (mOnMinuteSelectedListener != null) {
            mOnMinuteSelectedListener.onMinuteSelected(getDataList().get(getCurrentPosition()));
        }
    }

    public void setSelectedMinute(int minute, boolean smootScroll) {
        String min = String.valueOf(minute);
        boolean isLessThanFive = min.endsWith("1") || min.endsWith("2") || min.endsWith("3") || min.endsWith("4");
        boolean isGreaterThanFive = min.endsWith("6") || min.endsWith("7") || min.endsWith("8") || min.endsWith("9");
        if (min.length() == 1) {
            if (isLessThanFive) {
                min = "0";
            } else if (isGreaterThanFive) {
                min = "10";
            }
        } else {
            if (isLessThanFive) {
                String first = min.substring(0, 1);
                min = first + "0";
            } else if (isGreaterThanFive) {
                String first = min.substring(0, 1);
                min = (Integer.parseInt(first) + 1) + "0";
            }
        }

        for (int i = 0; i < getDataList().size(); i++) {
            if (getDataList().get(i) == Integer.parseInt(min)) {
                setCurrentPosition(i, smootScroll);
                break;
            }
        }
    }

    public void setOnMinuteSelectedListener(OnMinuteSelectedListener onMinuteSelectedListener) {
        mOnMinuteSelectedListener = onMinuteSelectedListener;
    }

    public interface OnMinuteSelectedListener {
        void onMinuteSelected(int hour);
    }
}