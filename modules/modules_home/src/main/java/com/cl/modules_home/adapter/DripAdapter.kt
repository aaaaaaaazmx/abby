package com.cl.modules_home.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.TimePickerPop
import com.cl.common_base.util.ViewUtils
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeDripItemBinding
import com.cl.modules_home.databinding.HomeJoinItemBinding
import com.cl.modules_home.request.DripListData

class DripAdapter(item: MutableList<DripListData.DripData>?) :
    BaseQuickAdapter<DripListData.DripData, BaseDataBindingHolder<HomeDripItemBinding>>(R.layout.home_drip_item, item) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseDataBindingHolder<HomeDripItemBinding>, item: DripListData.DripData) {
        holder.dataBinding?.apply {
            executePendingBindings()

            tvTitle.text = "Profile ${holder.layoutPosition + 1}"

            ivClose.visibility = if (holder.layoutPosition > 0) View.VISIBLE else View.INVISIBLE

            tvAirPumpDesc.setOnClickListener {
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_DRIP)
            }
            tvSeconds.setOnClickListener {
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_DRIP)
            }
            tvStart.setOnClickListener {
                // 时间开启
                xpopup(context) {
                    asCustom(
                        TimePickerPop(context, onConfirmAction = { time, timeMis ->
                            runCatching {
                                // 返回的是24小时制度。
                                val hour = if (time.toInt() == 0) 12 else time.toInt()

                                if (hour > 12) {
                                    tvStart.text = "${hour - 12}:00 PM"
                                } else if (hour < 12) {
                                    tvStart.text = "${hour}:00 AM"
                                } else if (hour == 12) {
                                    tvStart.text = "12:00 AM"
                                }
                                item.turnOnHour = hour
                            }

                        }, chooseTime = item.turnOnHour ?: 12)
                    ).show()
                }
            }

            tvEnd.setOnClickListener {
                xpopup(context) {
                    asCustom(
                        TimePickerPop(context, onConfirmAction = { time, timeMis ->
                            runCatching {
                                val hour = if (time.toInt() == 0) 12 else time.toInt()
                                if (hour > 12) {
                                    tvEnd.text = "${hour - 12}:00 PM"
                                } else if (hour < 12) {
                                    tvEnd.text = "${hour}:00 AM"
                                } else if (hour == 12) {
                                    tvEnd.text = "12:00 AM"
                                }
                                // 赋值给他
                                item.turnOffHour = hour
                            }
                        }, chooseTime = item.turnOffHour ?: 12)
                    ).show()
                }
            }

            etTurnTime.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    // 当焦点失去时，进行10-120的判断
                    runCatching {
                        val inputValue = etTurnTime.text.toString().toIntOrNull()
                        if (inputValue != null) {
                            if (inputValue < 5) {
                                etTurnTime.setText("5")
                            } else if (inputValue > 30) {
                                etTurnTime.setText("30")
                            }
                        }
                    }
                }
            }

            etTurnMin.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    // 当焦点失去时，进行10-120的判断
                    runCatching {
                        val inputValue = etTurnMin.text.toString().toIntOrNull()
                        if (inputValue != null) {
                            if (inputValue < 10) {
                                etTurnMin.setText("10")
                            } else if (inputValue > 120) {
                                etTurnMin.setText("120")
                            }
                        }
                    }
                }
            }


            etTurnTime.setText(item.turnOnSecond.toString())
            etTurnMin.setText(item.everyMinute.toString())
            // 后台返回的是24小时、需要转换为12小时制度的数字

            // {
            //  "deviceId": "6c55de1155d3127455yqsa",
            //  "turnOnSecond": 10,
            //  "everyMinute": 10,
            //  "everyStartTime": 6,
            //  "everyEndTime": 18,
            //  "status": false
            //}

            val startTime = when (item.everyStartTime) {
                0 -> 12
                12 -> 24
                else -> item.everyStartTime ?: 12
            }

            val endTime = when (item.everyEndTime) {
                0 -> 12
                12 -> 24
                else -> item.everyEndTime ?: 12
            }

            // 将12 AM和12 PM的情况单独处理
            tvStart.text =
                if (startTime == 0) "12 AM" else if (startTime == 24) "12 PM" else "${if (startTime > 12) startTime - 12 else startTime} ${if (startTime > 12) "PM" else "AM"}"
            item.turnOnHour = startTime
            tvEnd.text =
                if (endTime == 0) "12 AM" else if (endTime == 24) "12 PM" else "${if (endTime > 12) endTime - 12 else endTime} ${if (endTime > 12) "PM" else "AM"}"
            item.turnOffHour = endTime
        }
    }
}