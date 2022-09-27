/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cl.common_base.util.calendar;

import static com.cl.common_base.ext.LogKt.logI;

import android.annotation.SuppressLint;
import android.net.vcn.VcnManager;

import com.cl.common_base.bean.CalendarData;
import com.cl.common_base.bean.HttpResult;
import com.cl.common_base.util.json.GSON;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.JvmStatic;

/**
 * 一些日期辅助计算工具
 */
@SuppressWarnings("all")
public final class CalendarUtil {

    private static final long ONE_DAY = 1000 * 3600 * 24;

    @SuppressLint("SimpleDateFormat")
    @JvmStatic
    public static int getDate(String formatStr, Date date) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr, Locale.getDefault());
        return Integer.parseInt(format.format(date));
    }

    /**
     * 判断一个日期是否是周末，即周六日
     *
     * @param calendar calendar
     * @return 判断一个日期是否是周末，即周六日
     */
//    public static boolean isWeekend(Calendar calendar) {
//        int week = getWeekFormCalendar(calendar);
//        return week == 0 || week == 6;
//    }

    /**
     * 是否是周日
     *
     * @param calendar
     * @return
     */
    public static boolean isSunday(com.cl.common_base.util.calendar.Calendar calendar) {
        int week = getWeekFormCalendar(calendar);
        return week == Calendar.SUNDAY - 1;
    }

    /**
     * 是否是周六
     *
     * @param calendar
     * @return
     */
    public static boolean isSaturday(com.cl.common_base.util.calendar.Calendar calendar) {
        int week = getWeekFormCalendar(calendar);
        return week == Calendar.SATURDAY - 1;
    }


    /**
     * 获取某个日期是星期几
     * 测试通过
     *
     * @param calendar 某个日期
     * @return 返回某个日期是星期几
     */
    static int getWeekFormCalendar(com.cl.common_base.util.calendar.Calendar calendar) {
        Calendar date = Calendar.getInstance();
        date.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay());
        return date.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 获取某月的天数
     *
     * @param year  年
     * @param month 月
     * @return 某月的天数
     */
    public static int getMonthDaysCount(int year, int month) {
        int count = 0;
        //判断大月份
        if (month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month == 10 || month == 12) {
            count = 31;
        }

        //判断小月
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            count = 30;
        }

        //判断平年与闰年
        if (month == 2) {
            if (isLeapYear(year)) {
                count = 29;
            } else {
                count = 28;
            }
        }
        return count;
    }


    /**
     * 是否是闰年
     *
     * @param year year
     * @return 是否是闰年
     */
    public static boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }


//    public static List<com.cl.common_base.util.calendar.Calendar> initCalendarForMonthView(int year, int month, com.cl.common_base.util.calendar.Calendar currentDate, int weekStar) {
//        Calendar date = Calendar.getInstance();
//
//        date.set(year, month - 1, 1);
//
//        int mPreDiff = getMonthViewStartDiff(year, month, weekStar);//获取月视图其实偏移量
//
//        int monthDayCount = getMonthDaysCount(year, month);//获取月份真实天数
//
//        int preYear, preMonth;
//        int nextYear, nextMonth;
//
//        int size = 42;
////        int size = 31;
//
//        List<com.cl.common_base.util.calendar.Calendar> mItems = new ArrayList<>();
//
//        int preMonthDaysCount;
//        if (month == 1) {//如果是1月
//            preYear = year - 1;
//            preMonth = 12;
//            nextYear = year;
//            nextMonth = month + 1;
//            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
//        } else if (month == 12) {//如果是12月
//            preYear = year;
//            preMonth = month - 1;
//            nextYear = year + 1;
//            nextMonth = 1;
//            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
//        } else {//平常
//            preYear = year;
//            preMonth = month - 1;
//            nextYear = year;
//            nextMonth = month + 1;
//            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
//        }
//        int nextDay = 1;
//        for (int i = 0; i < size; i++) {
//            com.cl.common_base.util.calendar.Calendar calendarDate = new com.cl.common_base.util.calendar.Calendar();
//            logI("asdasd:"  + i + " : " + mPreDiff + ": " + preYear + ": " + preMonth + ": " + preMonthDaysCount + ": " + nextYear + ": " + nextMonth + ": " + nextDay);
//            if (i < mPreDiff) { // 不是1月
//                calendarDate.setYear(preYear);
//                calendarDate.setMonth(preMonth);
//                calendarDate.setDay(preMonthDaysCount - mPreDiff + i + 1);
//            } else if (i >= monthDayCount + mPreDiff) {
//                calendarDate.setYear(nextYear);
//                calendarDate.setMonth(nextMonth);
//                calendarDate.setDay(nextDay);
//                ++nextDay;
//            } else {
//                calendarDate.setYear(year);
//                calendarDate.setMonth(month);
//                calendarDate.setCurrentMonth(true);
//                calendarDate.setDay(i - mPreDiff + 1);
//            }
//            if (calendarDate.equals(currentDate)) {
//                calendarDate.setCurrentDay(true);
//            }
//            mItems.add(calendarDate);
//        }
//        return mItems;
//    }

    public static List<com.cl.common_base.util.calendar.Calendar> initCalendarForMonthView(int year, int month, com.cl.common_base.util.calendar.Calendar currentDate, int weekStar) {
        Calendar date = Calendar.getInstance();

        date.set(year, month - 1, 1);

        int mPreDiff = getMonthViewStartDiff(year, month, weekStar);//获取月视图其实偏移量

        int monthDayCount = getMonthDaysCount(year, month);//获取月份真实天数

        int preYear, preMonth;
        int nextYear, nextMonth;

        int size = 42;
//        int size = 31;

        List<com.cl.common_base.util.calendar.Calendar> mItems = new ArrayList<>();

        int preMonthDaysCount = 0;
        if (month == 1) {//如果是1月
            preYear = year - 1;
            preMonth = 12;
            nextYear = year;
            nextMonth = month + 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        } else if (month == 12) {//如果是12月
            preYear = year;
            preMonth = month - 1;
            nextYear = year + 1;
            nextMonth = 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        } else {//平常
            preYear = year;
            preMonth = month - 1;
            nextYear = year;
            nextMonth = month + 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        }
        int nextDay = 1;
        for (int i = 0; i < size; i++) {
            com.cl.common_base.util.calendar.Calendar calendarDate = new com.cl.common_base.util.calendar.Calendar();
//            logI("asdasd:"  + i + " : " + mPreDiff + ": " + preYear + ": " + preMonth + ": " + preMonthDaysCount + ": " + nextYear + ": " + nextMonth + ": " + nextDay);
            if (i < mPreDiff) { // 这个是填写前面都空白的
                // 如果上个月为12 ，当前月为 1月，下个月为2月
                if (preMonth == 12) {
                    calendarDate.setYear(preYear);
                    calendarDate.setMonth(preMonth);
                    calendarDate.setDay(preMonthDaysCount - mPreDiff + i + 1);
                } else {
                    continue;
                }
            } else if (i >= monthDayCount + mPreDiff) { // 这个是填写后面都空白的
                // 如果不是12月份，就没必要去填写
                if (preMonth == 11) { // 上一个月是12月，那么当前月为12 ，下个月为1月
                    calendarDate.setYear(nextYear);
                    calendarDate.setMonth(nextMonth);
                    calendarDate.setDay(nextDay);
                    ++nextDay;
                } else {
                    continue;
                }
            } else {
                calendarDate.setYear(year);
                calendarDate.setMonth(month);
                calendarDate.setCurrentMonth(true);
                calendarDate.setDay(i - mPreDiff + 1);
            }
            if (calendarDate.equals(currentDate)) {
                calendarDate.setCurrentDay(true);
            }
            mItems.add(calendarDate);
        }
        return mItems;
    }


    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期所在月视图对应的起始偏移量
     * Test pass
     *
     * @param year      年
     * @param month     月
     * @param weekStart 周起始
     * @return 获取日期所在月视图对应的起始偏移量 the start diff with MonthView
     */
    static int getMonthViewStartDiff(int year, int month, int weekStart) {
        Calendar date = Calendar.getInstance();
        date.set(year, month - 1, 1, 12, 0, 0);
        int week = date.get(Calendar.DAY_OF_WEEK);
        if (weekStart == Calendar.SUNDAY) {
            return week - 1;
        }
        if (weekStart == Calendar.MONDAY) {
            return week == 1 ? 6 : week - weekStart;
        }
        return week == Calendar.SATURDAY ? 0 : week;
    }


    /**
     * 格式化月份，根据地区
     */
    public static String getMonthFromLocation(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        return sdf.format(time);
    }

    /**
     * 格式化年月日
     */
    public static String getYMDFromLocation(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        return sdf.format(time);
    }


    /**
     * 获取之前的日期
     *
     * @param num         -7 表示之前7天的日期、 7表示后面7天的日期
     * @param currentTime 当前日期
     * @return 日期
     */
    public static Date getBeforeDayStr(int num, Date currentTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(currentTime);
        instance.add(Calendar.DATE, num);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String format = simpleDateFormat.format(instance.getTime());
        return instance.getTime();
    }

    /**
     * 获取1-31天的后缀
     * *1st ,First
     * *2nd, Second
     * <p>
     * 24th,Twenty-fourth
     * 25th,Twenty-fifth
     * 26th,twenty-sixth
     * 27th,Twenty-seventh
     * 28th,twenty-eighth
     * 29th,twenty-ninth
     * 30th,Thirtieth
     * 31st,Thirty-first
     */
    public static String getDaySuffix(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        try {
            int format = Integer.parseInt(simpleDateFormat.format(date.getTime()));
            switch (format) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
                case 3:
                    return "rd";
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                    return "th";
                case 21:
                    return "st";
                case 22:
                    return "nd";
                case 23:
                    return "rd";
                case 31:
                    return "st";
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    /**
     * 计算两个时间相差多少天
     *
     * @param endDate 结束时间
     * @param nowDate 现在的时间
     * @return
     */
    public static int getDatePoor(long endDate, long nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        long ns = 1000;
        // long ns = 1000;
        // 获得两个时间的秒时间差异
        long diff = (endDate) - (nowDate);
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        long sec = diff % nd % nh % nm / ns;
        String res = "";
        if (day != 0) {
            res += day + "天";
        }
        if (hour != 0) {
            res += "  " + hour + ":" + min + ":" + sec;
        }
        return Integer.parseInt(String.valueOf(day));
    }


    /**
     * 获取统一的simpleDateFormat
     *
     * @param format
     * @return
     */
    public static SimpleDateFormat getFormat(String format) {
        return new SimpleDateFormat(format, Locale.getDefault());
    }


    /**
     * 获取今年的第一天
     */
    public static String getYearStartDay(int year) {
        SimpleDateFormat format = getFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.getTime();
        return format.format(cal.getTime()) + "";
    }

    /**
     * 获取今年的最后一天
     */
    public static String getYearEndDay(int year) {
        SimpleDateFormat format = getFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
        return format.format(cal.getTime()) + "";
    }
}
