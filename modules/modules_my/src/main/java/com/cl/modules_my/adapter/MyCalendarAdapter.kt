package com.cl.modules_my.adapter

import android.graphics.Color
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.util.calendar.Calendar
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyCalendarItemBinding
import com.cl.modules_my.databinding.MyCalendarTitleItemBinding
import com.cl.modules_my.databinding.MyTroubleItemBinding
import com.cl.modules_my.repository.MyTroubleData
import okio.blackholeSink
import java.time.temporal.TemporalAccessor


/**
 * 日历
 */
class MyCalendarAdapter(data: MutableList<Calendar>?) :
    BaseMultiItemQuickAdapter<Calendar, BaseViewHolder>(data) {

    init {
        addItemType(Calendar.KEY_TITLE, R.layout.my_calendar_title_item)
        addItemType(Calendar.KEY_CONTENT, R.layout.my_calendar_item)
    }


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            Calendar.KEY_TITLE -> {
                val binding = DataBindingUtil.bind<MyCalendarTitleItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            Calendar.KEY_CONTENT -> {
                val binding = DataBindingUtil.bind<MyCalendarItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: Calendar) {
        when (holder.itemViewType) {
            Calendar.KEY_TITLE -> {}
            Calendar.KEY_CONTENT -> {
                // 设置文字颜色
                val year = item.year
                val month = item.month
                val day = item.day
                val textDateDay = holder.getView<TextView>(R.id.text_date_day)
                kotlin.runCatching {
                    val curr = data.first { it.isCurrentDay }
                    if (year < curr.year) {
                        textDateDay.setTextColor(
                            ContextCompat.getColor(
                                context,
                                com.cl.common_base.R.color.calendarGray
                            )
                        )
                    }

                    if (year > curr.year) {
                        textDateDay.setTextColor(Color.BLACK)
                    }

                    if (year == curr.year) {
                        if (month < curr.month) {
                            textDateDay.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    com.cl.common_base.R.color.calendarGray
                                )
                            )
                        }

                        if (month > curr.month) {
                            textDateDay.setTextColor(Color.BLACK)
                        }

                        if (month == curr.month) {
                            if (day < curr.day) {
                                textDateDay.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        com.cl.common_base.R.color.calendarGray
                                    )
                                )
                            }

                            if (day > curr.day) {
                                textDateDay.setTextColor(Color.BLACK)
                            }
                        }
                    }
                }


                // 测试背景颜色的绘制
                // 后台返回数据，可以用区间来循环做 通过下标
                val llRoot = holder.getView<RelativeLayout>(R.id.rl_root)
                if (item.day == 1 && item.year == 2022 && item.month == 9) {
                    // 判断是否是最后一个
                    if (CalendarUtil.isSaturday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_first_and_circle)
                    } else {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_first)
                    }
                }
                else if (item.day == 7 && item.year == 2022 && item.month == 9) {
                    if (CalendarUtil.isSunday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_end_and_circle)
                    } else {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_end)
                    }
                }
                else  if (item.day == 2 && item.year == 2022 && item.month == 9) {
                    if (CalendarUtil.isSaturday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_rtb_circle)
                    } else if (CalendarUtil.isSunday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_ltb_circle)
                    } else {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal)
                    }
                }
                else if (item.day == 3 && item.year == 2022 && item.month == 9) {
                    if (CalendarUtil.isSaturday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_rtb_circle)
                    } else if (CalendarUtil.isSunday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_ltb_circle)
                    } else {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal)
                    }                }
                else if (item.day == 4 && item.year == 2022 && item.month == 9) {
                    if (CalendarUtil.isSaturday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_rtb_circle)
                    } else if (CalendarUtil.isSunday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_ltb_circle)
                    } else {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal)
                    }                }
                else if (item.day == 5 && item.year == 2022 && item.month == 9) {
                    if (CalendarUtil.isSaturday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_rtb_circle)
                    } else if (CalendarUtil.isSunday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_ltb_circle)
                    } else {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal)
                    }                }
                else  if (item.day == 6 && item.year == 2022 && item.month == 9) {
                    if (CalendarUtil.isSaturday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_rtb_circle)
                    } else if (CalendarUtil.isSunday(item)) {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal_and_ltb_circle)
                    } else {
                        llRoot.setBackgroundResource(com.cl.common_base.R.drawable.bg_calendar_normal)
                    }
                } else {
                    llRoot.background = null
                }


            }
        }
    }


//    override fun convert(
//        holder: BaseDataBindingHolder<MyTroubleItemBinding>,
//        item: MyTroubleData.Bean
//    ) {
//        holder.dataBinding?.apply {
//            data = item
//            executePendingBindings()
//        }
//    }
}