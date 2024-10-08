package com.cl.modules_home.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.widget.TextView
import androidx.databinding.BindingConversion
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToLong
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.setVisible
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseStringPickPop
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.STEP
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.STEP_NOW
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.TEMPLATE_ID
import com.cl.modules_home.databinding.HomeTaskSetActivityBinding
import com.cl.modules_home.request.EnvSaveReq
import com.cl.modules_home.request.SaveTaskReq
import com.cl.modules_home.request.Task
import com.cl.modules_home.viewmodel.ProModeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class ProModeTaskSetActivity : BaseActivity<HomeTaskSetActivityBinding>() {

    @Inject
    lateinit var viewModel: ProModeViewModel


    private val step by lazy {
        intent.getStringExtra(STEP)
    }

    private val templateId by lazy {
        intent.getStringExtra(TEMPLATE_ID)
    }

    // stepShow
    private val stepShow by lazy {
        intent.getStringExtra(STEP_NOW)
    }

    // 结束时间 Long 类型  没有x1000
    private val endTime by lazy {
        intent.getLongExtra(ProModeEnvActivity.END_TIME, 0L)
    }

    // 周期开始时间
    private val startTime by lazy {
        intent.getLongExtra(ProModeEnvActivity.START_TIME, 0L)
    }

    private val taskData by lazy {
        intent.getSerializableExtra(ProModeEnvActivity.TASK_DATA) as? Task
    }

    private val daysList by lazy {
        mutableListOf("1", "2", "3", "4", "5", "6", "7")
    }

    private val copyTaskDataForUpload by lazy {
        taskData ?: Task(
            recurringTask = false,
            recurringDay = "1",
            endTime = endTime,
            taskTime = startTime,
        )
    }


    @SuppressLint("SetTextI18n")
    override fun initView() {
        // 是否循环任务
        binding.recurringTaskSwitch.isItemChecked = copyTaskDataForUpload.recurringTask
        binding.clRoot.setVisible(copyTaskDataForUpload.recurringTask)
        // 循环天数
        if (copyTaskDataForUpload.recurringDay.isNullOrBlank()) {
            binding.tvDay.text = "[1]"
            copyTaskDataForUpload.recurringDay = "1"
        } else {
            binding.tvDay.text = "[${copyTaskDataForUpload.recurringDay}]"
        }
        // 循环结束时间
        binding.tvDate.text = getYmdForEn(time = endTime * 1000L)
        // 周期任务
        binding.etEmail.text = copyTaskDataForUpload.taskName
        // 任务时间
        if (!copyTaskDataForUpload.week.isNullOrEmpty() && !copyTaskDataForUpload.day.isNullOrEmpty()) {
            binding.etEmails.text = "${getYmdForEn(time = copyTaskDataForUpload.taskTime * 1000L)} (Week ${copyTaskDataForUpload.week} Day ${copyTaskDataForUpload.day})"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun observe() {
        viewModel.apply {
            taskConfigurationList.observe(this@ProModeTaskSetActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (data?.list.isNullOrEmpty()) return@success
                    val task = data?.list?.get(0)
                    // 说明是空的，是新增的
                    if (copyTaskDataForUpload.taskName.isNullOrEmpty() && copyTaskDataForUpload.taskType.isNullOrEmpty() && copyTaskDataForUpload.taskId.isNullOrEmpty()) {
                        copyTaskDataForUpload.taskName = task?.taskName
                        copyTaskDataForUpload.taskType = task?.taskType
                        copyTaskDataForUpload.taskTime = task?.taskTime ?: 0
                        copyTaskDataForUpload.week = task?.week
                        copyTaskDataForUpload.day = task?.day

                        // 周期任务
                        binding.etEmail.text = task?.taskName
                        // task时间
                        binding.etEmails.text = "${getYmdForEn(time = copyTaskDataForUpload.taskTime * 1000L)} (Week ${task?.week} Day ${task?.day})"
                    }
                }
            })

            saveTask.observe(this@ProModeTaskSetActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    finish()
                }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        // 请求周期任务列表
        viewModel.getTaskConfigurationList(EnvSaveReq(step = step, templateId = templateId))

        // 是否打开循环任务
        binding.recurringTaskSwitch.setSwitchClickListener {
            binding.clRoot.setVisible(binding.recurringTaskSwitch.isItemChecked)
            copyTaskDataForUpload.recurringTask = binding.recurringTaskSwitch.isItemChecked
        }
        // 修改循环天数
        binding.tvDay.setSafeOnClickListener {
            runCatching {
                xpopup(this@ProModeTaskSetActivity) {
                    isDestroyOnDismiss(false)
                    isDestroyOnDismiss(false)
                    asCustom(
                        BaseStringPickPop(this@ProModeTaskSetActivity,
                            "Day",
                            selectIndex = daysList.indexOf(copyTaskDataForUpload.recurringDay?.ifEmpty { "1" }),
                            listString = daysList.toMutableList(),
                            confirmAction = {
                                binding.tvDay.text = "[${it + 1}]"
                                copyTaskDataForUpload.recurringDay = "${it + 1}"
                            })
                    ).show()
                }
            }
        }
        // 修改循环周期截止日期
        binding.tvDate.setSafeOnClickListener {
            copyTaskDataForUpload.endTime = showDatePickerDialog(this@ProModeTaskSetActivity, binding.tvDate, startTime * 1000L, endTime * 1000L) {
                copyTaskDataForUpload.endTime = it / 1000L
                // 更新 TextView 显示
                binding.tvDate.text = getYmdForEn(time = it)
            }
        }

        // 选择Task
        binding.etEmail.setSafeOnClickListener {
            runCatching {
                val taskList = viewModel.taskConfigurationList.value?.data?.list?.map { it.taskName }
                xpopup(this@ProModeTaskSetActivity) {
                    isDestroyOnDismiss(false)
                    isDestroyOnDismiss(false)
                    asCustom(
                        BaseStringPickPop(this@ProModeTaskSetActivity,
                            "Task",
                            selectIndex = taskList?.indexOf(if (copyTaskDataForUpload.taskName.isNullOrEmpty()) 0 else copyTaskDataForUpload.taskName) ?: 0,
                            listString = taskList?.toMutableList(),
                            confirmAction = {
                                binding.etEmail.text = taskList?.get(it) ?: ""
                                // 选择好之后，需要保存
                                copyTaskDataForUpload.taskName = taskList?.get(it) ?: ""

                                viewModel.taskConfigurationList.value?.data?.list?.firstOrNull { be -> be.taskName == taskList?.get(it) }?.let { data ->
                                    copyTaskDataForUpload.taskName = data.taskName
                                    copyTaskDataForUpload.taskType = data.taskType
                                    copyTaskDataForUpload.taskTime = data.taskTime
                                    copyTaskDataForUpload.week = data.week
                                    copyTaskDataForUpload.day = data.day
                                    // task时间
                                    binding.etEmails.text = "${getYmdForEn(time = copyTaskDataForUpload.taskTime * 1000L)} (Week ${data.week} Day ${data.day})"
                                }
                            })
                    ).show()
                }
            }
        }

        // 选择Task日期
        binding.etEmails.setSafeOnClickListener {
            showDatePickerDialog(this@ProModeTaskSetActivity, binding.etEmails, startTime * 1000L, endTime * 1000L) {
                val ymd = getYmdForEn(time = it)
                val (weeks, days) = calculateWeeksAndDaysIncludingStartDate(startTime, it / 1000L)
                binding.etEmails.text = "$ymd (Week $weeks Day $days)"
                copyTaskDataForUpload.taskTime = it / 1000L
            }
        }

        binding.svtCancel.setSafeOnClickListener { finish() }
        binding.svtConfirm.setSafeOnClickListener {
            // 调用保存接口
            viewModel.saveTask(SaveTaskReq(step = step, templateId = templateId, taskContent = mutableListOf(copyTaskDataForUpload)))
        }
    }

    private fun showDatePickerDialog(
        context: Context,
        editText: TextView,
        startTime: Long,  // 开始时间，以毫秒为单位
        maxTime: Long,     // 截止时间，以毫秒为单位
        onAction: ((Long) -> Unit)? = null
    ): Long {
        var selectedTimestamp = startTime  // 用于保存用户选择的时间戳

        // 创建日期选择监听器
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            // 使用 Calendar 来设置用户选择的日期
            val calendar = Calendar.getInstance()
            calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0)  // 设置为当天的 00:00:00

            // 获取时间戳（以毫秒为单位）
            val timestampInMillis = calendar.timeInMillis

            // 日志输出和显示选择的日期
            logI("选择的日期是：$year-${monthOfYear + 1}-$dayOfMonth，时间戳：$timestampInMillis")

            // 保存选择的时间戳
            selectedTimestamp = timestampInMillis

            onAction?.invoke(timestampInMillis)
        }

        // 初始化日历，并将 startTime 设置为初始日期
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime

        // 创建 DatePickerDialog，并设置默认选中的日期为 startTime
        val datePickerDialog = DatePickerDialog(
            context,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // 设置开始日期为 startTime
        datePickerDialog.datePicker.minDate = startTime

        // 设置截止日期为 maxTime
        datePickerDialog.datePicker.maxDate = maxTime

        // 显示日期选择器
        datePickerDialog.show()

        return selectedTimestamp  // 返回选择的时间戳
    }

    /**
     * 计算出多少开始和结束时距离多少个星期
     *
     * @param startTimestamp 为结束时间
     * @param endTimestamp 为开始时间
     *
     * 单位为： 秒
     * 写反了。
     *
     * 包含当天。
     */

    private fun calculateWeeksAndDaysIncludingStartDate(startTimestamp: Long, endTimestamp: Long): Pair<Long, Long> {
        val zoneId = ZoneId.systemDefault()
        val startDate = Instant.ofEpochSecond(startTimestamp).atZone(zoneId).toLocalDate()
        val endDate = Instant.ofEpochSecond(endTimestamp).atZone(zoneId).toLocalDate()

        // 计算总天数，包含起始日期
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate)
        val weeks = totalDays / 7
        val days = totalDays % 7

        return Pair(weeks + 1, days)
    }


    /**
     * 获取当前年月日-- 后面跟着英文的th
     * 如 9 12th 2022
     *
     * 传入:毫秒
     */
    private fun getYmdForEn(dateTime: Date? = null, time: Long? = null): String {
        dateTime?.let {
            val mm = CalendarUtil.getFormat("MMM").format(dateTime.time)
            val dd = CalendarUtil.getFormat("dd").format(dateTime.time) + CalendarUtil.getDaySuffix(
                dateTime
            )
            val yyyy = CalendarUtil.getFormat("yyyy").format(dateTime.time)
            return "$mm $dd $yyyy"
        }

        time?.let {
            if (time == 0L) return ""
            val mm = CalendarUtil.getFormat("MMM").format(time)
            val date = Date()
            date.time = time
            val dd = CalendarUtil.getFormat("dd").format(time)
            val yyyy = CalendarUtil.getFormat("yyyy").format(time)
            // return "$mm $dd $yyyy"
            return "$mm,$dd"
        }
        return ""
    }
}