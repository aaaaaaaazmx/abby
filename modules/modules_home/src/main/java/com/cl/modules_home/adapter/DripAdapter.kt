package com.cl.modules_home.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.TimePickerPop
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
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


            tvTitle.text = context.getString(com.cl.common_base.R.string.home_profilessss, "${holder.layoutPosition + 1}")

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
                                item.everyStartTime = if (time.toInt() == 24) 12 else time.toInt()
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
                                item.everyEndTime = if (time.toInt() == 24) 12 else time.toInt()
                            }
                        }, chooseTime = item.turnOffHour ?: 12)
                    ).show()
                }
            }

            etTurnTime.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false  // 防止递归调用

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 不需要处理
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 不需要处理
                }

                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return  // 避免递归调用

                    val inputStr = s.toString()
                    val inputValue = inputStr.toIntOrNull()

                    if (inputValue != null) {
                        isUpdating = true  // 设置标志，避免递归
                        item.turnOnSecond = inputValue
                        etTurnTime.setText(inputStr)
                        etTurnTime.setSelection(etTurnTime.text.length)  // 将光标移动到文本末尾
                        isUpdating = false  // 重置标志
                    } else {
                        // 输入值为空或非数字，可以根据需求处理
                        // 例如，将 item.turnOnSecond 设置为默认值或不做任何操作
                    }
                }
            })


            etTurnMin.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false  // 防止递归调用

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 不需要处理
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 不需要处理
                }

                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return

                    val inputStr = s.toString()
                    val inputValue = inputStr.toIntOrNull()

                    if (inputValue != null) {
                        isUpdating = true
                        item.everyMinute = inputValue
                        etTurnMin.setText(inputStr)
                        etTurnMin.setSelection(etTurnMin.text.length)  // 将光标移动到文本末尾
                        isUpdating = false
                    } else {
                        // 输入为空或非数字，可以选择不更新或设置为默认值
                        // 例如：item.everyMinute = 10
                    }
                }
            })


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