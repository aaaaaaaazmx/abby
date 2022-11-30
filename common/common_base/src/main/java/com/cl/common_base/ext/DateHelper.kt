package com.cl.common_base.ext

import android.content.Context
import androidx.annotation.Size
import com.cl.common_base.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
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
     * 指定地区为中国[Locale.getDefault()]格式化时间戳[timestamp]到默认时间格式[DATE_PATTERN]
     *
     * @param timestamp 时间戳
     * @return 格式化后的时间
     */
    @JvmStatic
    fun formatTime(timestamp: Long): String {
        return formatTime(timestamp, DATE_PATTERN, Locale.getDefault())
    }

    /**
     * 指定地区为中国[Locale.getDefault()]格式化时间戳[timestamp]到指定格式[pattern]
     *
     * @param timestamp     时间戳
     * @param pattern       时间格式
     * @return 格式化后的时间
     */
    @JvmStatic
    fun formatTime(timestamp: Long, pattern: String): String {
        return formatTime(timestamp, pattern, Locale.getDefault())
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
            var formatter = SimpleDateFormat(patternOld, Locale.getDefault())
            formatter.isLenient = false
            val newDate: Date? = formatter.parse(date)
            formatter = SimpleDateFormat(patternNew, Locale.getDefault())
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

    /**
     * 获取时区
     */
    fun getTimeZOneNumber(): Int {
        val dt: LocalDateTime = LocalDateTime.now()
        val zone: ZoneId = ZoneId.systemDefault()
        val zdt: ZonedDateTime = dt.atZone(zone)
        val offset: ZoneOffset = zdt.offset
        val out = java.lang.String.format("%s offset(seconds) %s", zone, offset.totalSeconds)
        return offset.totalSeconds
    }

    val MILLISECOND: Long = 1
    val SECOND = MILLISECOND * 1000
    val MINUTE = SECOND * 60
    val HOURS = MINUTE * 60
    val DAY = HOURS * 24

    val YMDHMS = "yyyy-MM-dd HH:mm:ss"
    val YMDHM = "yyyy-MM-dd HH:mm"
    val YMD = "yyyy-MM-dd"

    var sdf: SimpleDateFormat? = null

    private fun getDateFormat(patter: String): SimpleDateFormat {
        if (sdf == null)
            sdf = SimpleDateFormat(patter, Locale.getDefault())
        sdf!!.applyPattern(patter)
        return sdf!!
    }

    private fun getCalendar() = Calendar.getInstance(Locale.getDefault())

    private fun getCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = date
        return calendar
    }

    /**
     * 获取时间戳长度
     *
     * @param timestamp
     * @return
     */
    private fun getTimestampLength(timestamp: Long): Long {
        val dateLength = timestamp + "".length
        var result: Long = 1
        for (i in 0 until 13 - dateLength) {
            result *= 10
        }
        return result
    }

    fun formatToStr(timestamp: Long): String = formatToStr(timestamp, YMDHMS)

    fun formatToStr(timestamp: Long, patter: String): String = getDateFormat(patter).format(timestamp * getTimestampLength(timestamp))

    fun formatToStr(date: Date): String = formatToStr(date, YMDHMS)

    fun formatToStr(date: Date, patter: String): String = getDateFormat(patter).format(date)

    /**
     * 默认为24小时制
     *
     * @param l
     * @return
     */
    fun formatToDate(l: Long): Date = formatToDate(l, YMDHMS)

    fun formatToDate(l: Long, patter: String): Date = try {
        getDateFormat(patter).parse(formatToStr(l, patter))
    } catch (e: ParseException) {
        Date()
    }

    fun formatToDate(dateStr: String): Date = formatToDate(dateStr, YMDHMS)

    fun formatToDate(dateStr: String, patter: String): Date = try {
        getDateFormat(patter).parse(dateStr)
    } catch (e: ParseException) {
        Date()
    }

    /**
     * 默认为24小时制
     *
     * @param date
     * @return
     */
    fun formatToLong(date: Date): Long = date.time

    fun formatToLong(dateStr: String): Long = formatToLong(dateStr, YMDHMS)

    fun formatToLong(dateStr: String, patter: String): Long = try {
        getDateFormat(patter).parse(dateStr).time
    } catch (e: ParseException) {
        getCurrentTimeInMillis()
    }

    /**
     * timestamp1 在 timestamp2 之前
     * timestamp1<timestamp2
     * @param timestamp1
     * @param timestamp2
     */
    fun before(timestamp1: Long, timestamp2: Long): Boolean = timestamp1 < timestamp2


    fun before(timestamp1: String, timestamp2: String) = before(timestamp1, timestamp2, YMDHMS)

    /**
     * timestamp1 在 timestamp2 之前
     * timestamp1<timestamp2
     * @param timestamp1
     * @param timestamp2
     */
    fun before(timestamp1: String, timestamp2: String, patter: String): Boolean = formatToDate(timestamp1, patter).before(formatToDate(timestamp2, patter))

    /**
     * timestamp1 在 timestamp2 之前
     * timestamp1<timestamp2
     * @param timestamp1
     * @param timestamp2
     */
    fun before(timestamp1: Date, timestamp2: Date): Boolean = timestamp1.before(timestamp2)

    /**
     * timestamp1 在 timestamp2 之后
     * timestamp1>timestamp2
     * @param timestamp1
     * @param timestamp2
     */
    fun after(timestamp1: Long, timestamp2: Long): Boolean = timestamp1 > timestamp2

    fun after(timestamp1: String, timestamp2: String) = after(timestamp1, timestamp2, YMDHMS)

    /**
     * timestamp1 在 timestamp2 之后
     * timestamp1>timestamp2
     * @param timestamp1
     * @param timestamp2
     */
    fun after(timestamp1: String, timestamp2: String, patter: String): Boolean = formatToDate(timestamp1, patter).after(formatToDate(timestamp2, patter))

    /**
     * timestamp1 在 timestamp2 之后
     * timestamp1>timestamp2
     * @param timestamp1
     * @param timestamp2
     */
    fun after(timestamp1: Date, timestamp2: Date): Boolean = timestamp1.after(timestamp2)


    /**
     * 比较时间大小
     * @return 最小的时间
     */
    fun compareBefore(vararg timestamps: Long): Long {
        var temp = -1L
        if (timestamps.isNotEmpty()) {
            temp = timestamps[0]
            (1 until timestamps.size - 1)
                .asSequence()
                .filter { temp > timestamps[it] }
                .forEach { temp = timestamps[it] }
        }
        return temp
    }


    /**
     * 比较时间大小
     * @return 最小的时间
     */
    fun compareBefore(patter: String, @Size(min = 1) vararg timestamps: String): String {
        var temp = ""
        if (timestamps.isNotEmpty()) {
            temp = timestamps[0]
            (1 until timestamps.size - 1)
                .asSequence()
                .filter { formatToDate(timestamps[it], patter).before(formatToDate(timestamps[it], patter)) }
                .forEach { temp = timestamps[it] }
        }
        return temp
    }

    /**
     * 比较时间大小
     * @return 最小的时间
     */
    fun compareBefore(@Size(min = 1) vararg timestamps: Date): Date {
        var temp = getCalendar().time
        if (timestamps.isNotEmpty()) {
            temp = timestamps[0]
            (1 until timestamps.size - 1)
                .asSequence()
                .filter { temp.after(timestamps[it]) }
                .forEach { temp = timestamps[it] }
        }
        return temp
    }

    /**
     * 比较时间大小
     * @return 最大的时间
     */
    fun compareAfter(@Size(min = 1) vararg timestamps: Long): Long {
        var temp = -1L
        if (timestamps.isNotEmpty()) {
            temp = timestamps[0]
            (1 until timestamps.size - 1)
                .asSequence()
                .filter { temp < timestamps[it] }
                .forEach { temp = timestamps[it] }
        }
        return temp
    }

    /**
     * 比较时间大小
     * @return 最大的时间
     */
    fun compareAfter(patter: String, vararg timestamps: String): String {
        var temp = ""
        if (timestamps.isNotEmpty()) {
            temp = timestamps[0]
            (1 until timestamps.size - 1)
                .asSequence()
                .filter { formatToDate(timestamps[it], patter).before(formatToDate(timestamps[it], patter)) }
                .forEach { temp = timestamps[it] }
        }
        return temp
    }

    /**
     * 比较时间大小
     * @return 最大的时间
     */
    fun compareAfter(vararg timestamps: Date): Date {
        var temp = getCalendar().time
        if (timestamps.isNotEmpty()) {
            temp = timestamps[0]
            (1 until timestamps.size - 1)
                .asSequence()
                .filter { temp.before(timestamps[it]) }
                .forEach { temp = timestamps[it] }
        }
        return temp
    }

    /**
     * 格式化秒表
     * 有天的 02 11：11：12，534
     * 有时的 11：11：12，534
     * 有分的 11：12，534
     * 有秒的 11：12，534
     * 有毫秒 11：12，534
     * @param timestamp 单位毫秒
     */
    fun formatDateStopwatch(timestamp: Long): String {
        return if (timestamp > 0) {
            val day = timestamp / DAY
            val hours = (timestamp - DAY * day) / HOURS
            val minute = (timestamp - DAY * day - HOURS * hours) / MINUTE
            val second = (timestamp - DAY * day - HOURS * hours - MINUTE * minute) / SECOND
            val millisecond = (timestamp - DAY * day - HOURS * hours - MINUTE * minute - SECOND * second) / MILLISECOND
            val tempDay = if (day in 0..9) "0$day" else day.toString()
            val tempHours = if (hours in 0..9) "0$hours" else hours.toString()
            val tempMinute = if (minute in 0..9) "0$minute" else minute.toString()
            val tempSecond = if (second in 0..9) "0$second" else second.toString()
            val tempMillisecond = if (millisecond in 0..9) "00$millisecond" else if (millisecond in 10..99) "0$millisecond" else millisecond.toString()

            return when {
                day > 0 -> String.format("%s %s:%s:%s,%s", tempDay, tempHours, tempMinute, tempSecond, tempMillisecond)
                hours > 0 -> String.format("%s:%s:%s,%s", tempHours, tempMinute, tempSecond, tempMillisecond)
                else -> String.format("%s:%s,%s", tempMinute, tempSecond, tempMillisecond)
            }
        } else "00:00,00"
    }

    /**
     * 格式化时间差
     *
     * @param timestamp
     * @param postfix 前缀
     * @param postfix 后缀
     */
    fun formatDateDifference(timestamp: Long, prefix: String, postfix: String): String {

        val day = timestamp / DAY
        val hour = (timestamp - DAY * day) / HOURS
        val minute = (timestamp - DAY * day - HOURS * hour) / MINUTE
        val second = (timestamp - DAY * day - HOURS * hour - MINUTE * minute) / SECOND

        return when {
            day > 0 -> String.format("$prefix%s天%s时%s分%s秒$postfix", day, hour, minute, second)
            hour > 0 -> String.format("$prefix%s时%s分%s秒$postfix", hour, minute, second)
            minute > 0 -> String.format("$prefix%s分%s秒$postfix", minute, second)
            else -> String.format("$prefix%s秒$postfix", second)
        }
    }

    /**
     * 转换成多少时间前
     */
    fun convert(startDate: Long): String? {
        val endTime = System.currentTimeMillis()     //获取毫秒数
        val timeDifference = endTime - startDate;
        val second = timeDifference / 1000;    //计算秒
        if (second < 60) {
            return "$second second ago" //根据需要可以写成刚刚。
        } else {
            val minute = second / 60
            if (minute < 60) {
                return "$minute minutes ago"
            } else {
                val hour = minute / 60
                if (hour < 24) {
                    return "$hour hours ago"
                } else {
                    val day = hour / 24
                    if (day < 7) {
                        return "$day days ago"
                    } else {
                        return if (formatTime(startDate, "yyyy") == formatTime(endTime, "yyyy")) {
                            formatTime(startDate, "MMdd")
                        } else {
                            formatTime(startDate, "MMdd yyyy")
                        }
                        /*  var month = day / 30
                        if (month < 12) {
                            return "$month 月前"
                        } else {
                            var year = month / 12
                            return "$year 年前"
                        }*/
                    }

                }
            }
        }
    }


    fun getCurrentTimeInMillis() = getCalendar().timeInMillis
    fun getCurrentYear() = getCalendar().get(Calendar.YEAR)
    fun getCurrentMonth() = getCalendar().get(Calendar.MONTH) + 1
    fun getCurrentDay() = getCalendar().get(Calendar.DAY_OF_MONTH)
    fun getCurrentHour() = getCalendar().get(Calendar.HOUR_OF_DAY)
    fun getCurrentMinute() = getCalendar().get(Calendar.MINUTE)
    fun getCurrentSecond() = getCalendar().get(Calendar.SECOND)

    fun getTimeInMillis(date: Date) = date.time
    fun getYear(date: Date) = getCalendar(date).get(Calendar.YEAR)
    fun getMonth(date: Date) = getCalendar(date).get(Calendar.MONTH) + 1
    fun getDay(date: Date) = getCalendar(date).get(Calendar.DAY_OF_MONTH)
    fun getHour(date: Date) = getCalendar(date).get(Calendar.HOUR_OF_DAY)
    fun getMinute(date: Date) = getCalendar(date).get(Calendar.MINUTE)
    fun getSecond(date: Date) = getCalendar(date).get(Calendar.SECOND)

}