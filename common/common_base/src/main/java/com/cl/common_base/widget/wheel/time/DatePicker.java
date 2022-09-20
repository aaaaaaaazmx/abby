package com.cl.common_base.widget.wheel.time;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.cl.common_base.util.calendar.CalendarUtil;
import com.cl.common_base.widget.wheel.WheelPicker;

import java.sql.Time;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 日期选择
 *
 * @author lijiewen
 * @date on 2019-11-09
 */
public class DatePicker extends WheelPicker<String> {

    private OnDateSelectedListener mOnDateSelectedListener;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public DatePicker(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemMaximumWidthText("00");
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumIntegerDigits(2);
//        setDataFormat(numberFormat);
        updateHour();
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                if (mOnDateSelectedListener != null) {
                    // 转化成年月日
                    if (timeLong.isEmpty()) return;
                    mOnDateSelectedListener.onDateSelected(CalendarUtil.getFormat("yyyy-MM-dd").format(timeLong.get(position)), timeLong.get(position));
                }
            }
        });
    }

    /**
     * 设置当前的日期
     */
    private long millisecond;

    public void setCurrentDate(long millisecond) {
        this.millisecond = millisecond;
        updateHour();
    }

    List<Long> timeLong = new ArrayList<>();
    private void updateHour() {
        List<String> list = new ArrayList<>();
        // 当前的时间毫秒
        // 找到前三天、后三天、加今天
        for (int i = 0; i < 7; i++) {
            String daySuffix = null;
            String format = null;

            if (i < 3) {
                Date beforeDayStr = CalendarUtil.getBeforeDayStr(-3 + i, new Date(millisecond)); // 前面的日期
                daySuffix = CalendarUtil.getDaySuffix(beforeDayStr); // 月份后面的后缀
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = CalendarUtil.getFormat("MMM dd");
                format = simpleDateFormat.format(beforeDayStr.getTime());
                timeLong.add(beforeDayStr.getTime());
            }
            if (i == 3) {
                // 插入今天
                daySuffix = CalendarUtil.getDaySuffix(new Date()); // 月份后面的后缀
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = CalendarUtil.getFormat("MMM dd");
                format = simpleDateFormat.format(new Date().getTime());
                timeLong.add(new Date().getTime());
            }

            if (i > 3) {
                Date beforeDayStr = CalendarUtil.getBeforeDayStr(i - 3, new Date(millisecond)); // 前面的日期
                daySuffix = CalendarUtil.getDaySuffix(beforeDayStr); // 月份后面的后缀
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = CalendarUtil.getFormat("MMM dd");
                format = simpleDateFormat.format(beforeDayStr.getTime());
                timeLong.add(beforeDayStr.getTime());
            }
            if (null != format && null != daySuffix) {
                list.add(format + "" + daySuffix);
            }
        }
        setDataList(list);
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onHourSelectedListener) {
        mOnDateSelectedListener = onHourSelectedListener;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String hour, long time);
    }
}
