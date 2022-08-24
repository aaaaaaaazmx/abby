package com.cl.common_base.ext

import android.content.Context
import com.cl.common_base.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * <h2> 日期帮助类 </h2>
 *
 * @version 1.0
 * @since 2020年07月16日 15:46
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object DateHelper {

    //默认时间格式
    const val DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"

    //默认时间格式
    const val DATE_PATTERN_2 = "yyyy年MM月dd日 HH时mm分"

    //默认时区(东八区)
    const val DATE_TIME_ZONE = "GMT+08:00"

    // 默认年月日
    const val DATE_DEFAULT_PATTERN = "yyyy-MM-dd"

    /**
     * 获取指定日期[date]以东八区时区[DATE_TIME_ZONE]转化日期到时间戳, 需要传入日期的时间格式[pattern]
     *
     * @param date      日期
     * @param pattern 时间格式
     *
     * @return 时间戳
     */
    @JvmStatic
    @Deprecated(
        "在Kotlin中有更好的方法来将String转化为时间戳, 此方法不再维护",
        ReplaceWith(
            "date.toTimestampOrZero(pattern)",
            "com.goldentec.android.tools.util.StringUtils"
        ),
        DeprecationLevel.WARNING
    )
    fun getTimestamp(date: String, pattern: String): Long {
        return getTimestamp(date, pattern, DATE_TIME_ZONE)
    }

    /**
     * 获取指定日期[date]以东八区时区[DATE_TIME_ZONE]转化日期到时间戳, 需要传入日期的时间格式[pattern]
     */
    @Deprecated(
        "在Kotlin中有更好的方法来将String转化为时间戳, 此方法不再维护",
        ReplaceWith(
            "this.toTimestampOrZero(pattern)",
            "com.goldentec.android.tools.util.StringUtils"
        ),
        DeprecationLevel.WARNING
    )
    fun String.getTimestampEx(pattern: String): Long {
        return getTimestamp(this, pattern)
    }

    /**
     * 获取指定格式时间的时间戳
     *
     * @param dateStr 时间字符串
     * @param pattern 时间格式
     * @return 时间戳
     */
    @JvmStatic
    @Deprecated(
        "在Kotlin中有更好的方法来将String转化为时间戳, 此方法不再维护",
        ReplaceWith(
            "dateStr.toTimestampOrZero(pattern, timeZone = TimeZone.getTimeZone(timeZone))",
            "com.goldentec.android.tools.util.StringUtils"
        ),
        DeprecationLevel.WARNING
    )
    fun getTimestamp(dateStr: String, pattern: String, timeZone: String): Long {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.CHINA)
            sdf.timeZone = TimeZone.getTimeZone(timeZone)
            val date: Date? = sdf.parse(dateStr)
            date?.run { return time }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 获取当天指定小时时间
     *
     * @return 当前指定小时的时间
     */
    @JvmStatic
    fun setHourGetDayTime(hour: Int): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = hour
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        return cal.time.time
    }

    /**
     * 获取当天指定小时日期
     *
     * @return 当前指定小时的日期
     */
    @JvmStatic
    fun setHourGetDayDate(hour: Int): String {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = hour
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        return formatTime(cal.time.time)
    }

    /**
     * 指定地区为中国[Locale.CHINA]格式化时间戳[timestamp]到默认时间格式[DATE_PATTERN]
     *
     * @param timestamp 时间戳
     * @return 格式化后的时间
     */
    @JvmStatic
    fun formatTime(timestamp: Long): String {
        return formatTime(timestamp, DATE_PATTERN, Locale.CHINA)
    }

    /**
     * 指定地区为中国[Locale.CHINA]格式化时间戳[timestamp]到指定格式[pattern]
     *
     * @param timestamp     时间戳
     * @param pattern       时间格式
     * @return 格式化后的时间
     */
    @JvmStatic
    fun formatTime(timestamp: Long, pattern: String): String {
        return formatTime(timestamp, pattern, Locale.CHINA)
    }

    /**
     * 指定地区[locale]指定时间格式[pattern]转化时间戳[timestamp]到需要的格式, 默认为毫秒级别的时间戳格式
     *
     * @param timestamp     时间戳
     * @param pattern       时间格式
     * @param locale        地区
     * @return 格式化后的时间
     */
    @JvmStatic
    fun formatTime(timestamp: Long, pattern: String, locale: Locale): String {
        return formatTime(timestamp, pattern, TimeUnit.MILLISECONDS, locale)
    }

    /**
     * 指定地区格式化时间戳, 传入时间戳[time]、需要得到的时间格式[pattern]、时间戳类型[timeUnit]、地区[locale], 返回格式化后的时间[String]
     */
    fun formatTime(time: Long, pattern: String, timeUnit: TimeUnit, locale: Locale): String {
        val tempTime = timeUnit.toMillis(time)
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(tempTime)
    }

    /**
     * 指定地区以默认方式转化时间格式
     *
     * @param date 日期
     * @return 新的日期格式格式化后的时间
     */
    @JvmStatic
    fun formatTime(date: String): String {
        return formatTime(date, DATE_PATTERN, DATE_PATTERN_2)
    }

    /**
     * @param date       日期
     * @param patternOld 旧的日期格式
     * @param patternNew 新的日期格式
     * @return 新的日期格式格式化后的时间
     */
    @JvmStatic
    fun formatTime(date: String, patternOld: String, patternNew: String): String {
        try {
            var formatter = SimpleDateFormat(patternOld, Locale.CHINA)
            formatter.isLenient = false
            val newDate: Date? = formatter.parse(date)
            formatter = SimpleDateFormat(patternNew, Locale.CHINA)
            newDate?.let {
                return formatter.format(it)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 获取time2与time1的差值
     *
     * @return 差值转化后的日期时间字符串
     */
    @JvmStatic
    fun getDistanceTime(time1: Long, time2: Long): String {
        val day: Long
        val hour: Long
        val min: Long
        val sec: Long

        val diff: Long = if (time1 < time2)
            time2 - time1
        else
            time1 - time2

        day = diff / (24 * 60 * 60 * 1000)
        hour = diff / (60 * 60 * 1000) - day * 24
        min = diff / (60 * 1000) - day * 24 * 60 - hour * 60
        sec = diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60
        if (day != 0L) return day.toString() + "天" + hour + "小时" + min + "分钟" + sec + "秒"
        if (hour != 0L) return hour.toString() + "小时" + min + "分钟" + sec + "秒"
        if (min != 0L) return min.toString() + "分钟" + sec + "秒"
        return if (sec != 0L) sec.toString() + "秒" else "0秒"
    }

    fun Long.getDistanceTimeEx(time2: Long): String {
        return getDistanceTime(this, time2)
    }


    // 获取今天是周几
    private fun getDayOfWeek(dateTime: String? = null): Int {
        val cal = Calendar.getInstance()
        if (dateTime == null) {
            cal.time = Date(System.currentTimeMillis())
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            var date: Date?
            try {
                date = sdf.parse(dateTime)
            } catch (e: ParseException) {
                date = null
                e.printStackTrace()
            }
            if (date != null) {
                cal.time = Date(date.time)
            }
        }
        return cal[Calendar.DAY_OF_WEEK]
    }

    fun week(dateTime: String? = null, context: Context): String {
        var week = ""
        when (getDayOfWeek(dateTime)) {
            1 -> week = context.getString(R.string.sunday)
            2 -> week = context.getString(R.string.monday)
            3 -> week = context.getString(R.string.tuesday)
            4 -> week = context.getString(R.string.wednesday)
            5 -> week = context.getString(R.string.thursday)
            6 -> week = context.getString(R.string.friday)
            7 -> week = context.getString(R.string.saturday)
        }
        return week
    }

    // 获取时间差
    fun getTimeDifference(): Int {
        val timeZone = TimeZone.getDefault()
        val id = timeZone.id //获取时区id，如“Asia/Shanghai”
        val name = timeZone.displayName //获取名字，如“”
        val shotName = timeZone.getDisplayName(false, TimeZone.SHORT) //获取名字，如“GMT+08:00”
        return (timeZone.rawOffset) * 1000
    }
}