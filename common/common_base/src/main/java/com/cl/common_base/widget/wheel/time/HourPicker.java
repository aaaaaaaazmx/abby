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
 * 小时选择
 *
 * @author lijiewen
 * @date on 2019-11-09
 */
public class HourPicker extends WheelPicker<Integer> {

    private boolean force12HourMode;
    private boolean normal12HourMode;
    private boolean isAbbyCalendar;
    private OnHourSelectedListener mOnHourSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HourPicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HourPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public HourPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("00");
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumIntegerDigits(2);
        setDataFormat(numberFormat);
        updateHour();
        setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener<Integer>() {
            @Override
            public void onWheelSelected(Integer item, int position) {
                if (mOnHourSelectedListener != null) {
                    mOnHourSelectedListener.onHourSelected(item == null ? 0 : item);
                }
            }
        });
    }


    public void setForce12HourMode(boolean is12HourMode) {
        this.force12HourMode = is12HourMode;
        updateHour();
    }

    public void setNormal12HourMode(boolean is12HourMode) {
        if (!force12HourMode) {
            this.normal12HourMode = is12HourMode;
            updateHour();
        }
    }

    public boolean is12HourMode() {
        return force12HourMode || normal12HourMode;
    }

    private void updateHour() {
        List<Integer> list = new ArrayList<>();
        if (is12HourMode()) {
            for (int i = 1; i < 13; i++) {
                list.add(i);
            }
        } else {
            for (int i = 0; i < 24; i++) {
                list.add(i);
            }
        }
        setDataList(list);
    }

    public void setSelectedHour(int hour) {
        setSelectedHour(hour, true);
        if (mOnHourSelectedListener != null) {
            mOnHourSelectedListener.onHourSelected(getDataList().get(getCurrentPosition()));
        }
    }

    public void setSelectedHour(int hour, boolean smoothScroll) {
        if (is12HourMode()) {
            if (hour == 0) {
                hour = 12;
            } else if (hour > 12) {
                hour -= 12;
            }
        }
        for (int i = 0; i < getDataList().size(); i++) {
            if (getDataList().get(i) == hour) {
                setCurrentPosition(i, smoothScroll);
                break;
            }
        }
    }

    public void setOnHourSelectedListener(OnHourSelectedListener onHourSelectedListener) {
        mOnHourSelectedListener = onHourSelectedListener;
    }

    public interface OnHourSelectedListener {
        void onHourSelected(Integer hour);
    }
}
