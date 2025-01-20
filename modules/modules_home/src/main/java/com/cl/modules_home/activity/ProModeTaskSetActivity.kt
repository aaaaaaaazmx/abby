package com.cl.modules_home.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingConversion
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToLong
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.setVisible
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseStringPickPop
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.common_base.widget.decoraion.FullyGridLayoutManager
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.STEP
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.STEP_NOW
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.TEMPLATE_ID
import com.cl.modules_home.adapter.PlantListAdapter
import com.cl.modules_home.databinding.HomeTaskSetActivityBinding
import com.cl.modules_home.request.EnvSaveReq
import com.cl.modules_home.request.SaveTaskReq
import com.cl.modules_home.request.Task
import com.cl.modules_home.viewmodel.ProModeViewModel
import com.luck.picture.lib.utils.DensityUtil
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@Route(path = RouterPath.Home.PAGE_HOME_TASK_SET)
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

    // taskId
    private val taskId by lazy {
        intent.getStringExtra(ProModeEnvActivity.TASK_ID)
    }

    // 周期开始时间
    private val startTime by lazy {
        intent.getLongExtra(ProModeEnvActivity.START_TIME, 0L)
    }

    private val taskData by lazy {
        intent.getSerializableExtra(ProModeEnvActivity.TASK_DATA) as? Task
    }

    // 添加任务，从日历界面带过的当前时间
    private val addCurrentTime by lazy {
        intent.getLongExtra(ProModeEnvActivity.CURRENT_TIME, 0L)
    }

    // 添加任务 从日历界面带过来的。
    private val isCalendarPage by lazy {
        intent.getBooleanExtra("${ProModeEnvActivity.KEY_REQUEST_TASK_SET}", false)
    }

    private val daysList by lazy {
        mutableListOf("1", "2", "3", "4", "5", "6", "7")
    }

    // 延迟初始化 copyTaskDataForUpload，在需要时才进行赋值
    private lateinit var copyTaskDataForUpload: Task

    private val adapter by lazy {
        PlantListAdapter(mutableListOf())
    }


    @SuppressLint("SetTextI18n")
    override fun initView() {
        // 初始化 copyTaskDataForUpload，根据 taskData 是否为空提供合理的默认值
        copyTaskDataForUpload = taskData ?: Task(
            recurringTask = false,
            recurringDay = "1",
            endTime = endTime,
            taskTime = if (addCurrentTime != 0L) addCurrentTime else startTime
        )

        // 是否循环任务
        binding.recurringTaskSwitch.isItemChecked = copyTaskDataForUpload.recurringTask
        logI("1231231: ${copyTaskDataForUpload.recurringTask}")
        // 顺序不能乱
        ViewUtils.setVisible(!isCalendarPage, binding.recurringTaskTextView, binding.recurringTaskSwitch, binding.clRoot)
        binding.clRoot.setVisible(copyTaskDataForUpload.recurringTask)

        // 循环天数
        if (copyTaskDataForUpload.recurringDay.isNullOrBlank()) {
            binding.tvDay.text = "1"
            copyTaskDataForUpload.recurringDay = "1"
        } else {
            binding.tvDay.text = "${copyTaskDataForUpload.recurringDay}"
        }
        // 循环结束时间
        binding.tvDate.text = getYmdForEn(time = copyTaskDataForUpload.endTime * 1000L)
        // 周期任务
        binding.etEmail.text = copyTaskDataForUpload.taskName
        // 周期任务描述
        binding.taskTextViews.text = copyTaskDataForUpload.taskdescription
        ViewUtils.setVisible(!copyTaskDataForUpload.taskdescription.isNullOrEmpty(), binding.taskTextViews)

        // 任务时间
        if (!copyTaskDataForUpload.week.isNullOrEmpty() && !copyTaskDataForUpload.day.isNullOrEmpty()) {
            binding.etEmails.text = "${getYmdForEn(time = copyTaskDataForUpload.taskTime * 1000L)} (${getString(com.cl.common_base.R.string.week)} ${copyTaskDataForUpload.week} ${getString(com.cl.common_base.R.string.day)} ${copyTaskDataForUpload.day})"
        }

        binding.recyclerView.apply {
            layoutManager = FullyGridLayoutManager(
                this@ProModeTaskSetActivity,
                4, GridLayoutManager.VERTICAL, false
            )
            addItemDecoration(
                GridSpaceItemDecoration(
                    4,
                    DensityUtil.dip2px(this@ProModeTaskSetActivity, 1f), DensityUtil.dip2px(this@ProModeTaskSetActivity, 1f)
                )
            )
            adapter = this@ProModeTaskSetActivity.adapter
        }
    }

    @SuppressLint("SetTextI18n")
    override fun observe() {
        viewModel.apply {
            taskList.observe(this@ProModeTaskSetActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 显示植物。
                    ViewUtils.setVisible(!data?.get(0)?.multiplants.isNullOrEmpty(), binding.recyclerView, binding.appliesToTextView)
                    if (data.isNullOrEmpty()) return@success
                    val task = data?.get(0) ?: return@success
                    // 说明是空的，是新增的
                    if (copyTaskDataForUpload.taskName.isNullOrEmpty() && copyTaskDataForUpload.taskType.isNullOrEmpty() && copyTaskDataForUpload.taskId.isNullOrEmpty()) {
                        copyTaskDataForUpload = task.copy(
                            endTime = copyTaskDataForUpload.endTime,
                            recurringTask = copyTaskDataForUpload.recurringTask,
                            recurringDay = copyTaskDataForUpload.recurringDay,
                            taskTime = if (addCurrentTime != 0L) addCurrentTime else task.taskTime,
                            week = calculateWeeksAndDaysIncludingStartDate(startTime, if (addCurrentTime != 0L) addCurrentTime else task.taskTime).first.toString(),
                            day = calculateWeeksAndDaysIncludingStartDate(startTime, if (addCurrentTime != 0L) addCurrentTime else task.taskTime).second.toString()
                        )

                        // 周期任务描述
                        binding.taskTextViews.text = copyTaskDataForUpload.taskdescription
                        ViewUtils.setVisible(!copyTaskDataForUpload.taskdescription.isNullOrEmpty(), binding.taskTextViews)
                        // 周期任务
                        binding.etEmail.text = copyTaskDataForUpload.taskName
                        // task时间
                        binding.etEmails.text = "${getYmdForEn(time = copyTaskDataForUpload.taskTime * 1000L)} (${getString(com.cl.common_base.R.string.week)} ${copyTaskDataForUpload.week} ${getString(com.cl.common_base.R.string.day)} ${copyTaskDataForUpload.day})"
                    }
                    adapter.setList(data?.get(0)?.multiplants)
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
                    if (isCalendarPage) {
                        setResult(RESULT_OK)
                    }
                    finish()
                }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        // 请求周期任务列表
        viewModel.getTaskList(EnvSaveReq(step = step, templateId = templateId))

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
                            getString(com.cl.common_base.R.string.day),
                            selectIndex = daysList.indexOf(copyTaskDataForUpload.recurringDay?.ifEmpty { "1" }),
                            listString = daysList.toMutableList(),
                            confirmAction = {
                                binding.tvDay.text = "${it + 1}"
                                copyTaskDataForUpload.recurringDay = "${it + 1}"
                            })
                    ).show()
                }
            }
        }
        // 修改循环周期截止日期
        binding.tvDate.setSafeOnClickListener {
            showDatePickerDialog(this@ProModeTaskSetActivity, binding.tvDate, startTime * 1000L, endTime * 1000L) {
                copyTaskDataForUpload.endTime = it / 1000L
                // 更新 TextView 显示
                binding.tvDate.text = getYmdForEn(time = it)
            }
        }

        // 选择Task
        binding.etEmail.setSafeOnClickListener {
            runCatching {
                val taskList = viewModel.taskList.value?.data?.map { it.taskName }
                xpopup(this@ProModeTaskSetActivity) {
                    isDestroyOnDismiss(false)
                    isDestroyOnDismiss(false)
                    asCustom(
                        BaseStringPickPop(this@ProModeTaskSetActivity,
                            getString(com.cl.common_base.R.string.home_tasksss),
                            selectIndex = taskList?.indexOf(if (copyTaskDataForUpload.taskName.isNullOrEmpty()) 0 else copyTaskDataForUpload.taskName) ?: 0,
                            listString = taskList?.toMutableList(),
                            confirmAction = {
                                // 选择好之后，需要保存
                                viewModel.taskList.value?.data?.firstOrNull { be -> be.taskName == taskList?.get(it) }?.let { data ->
                                    copyTaskDataForUpload = data.copy(
                                        endTime = copyTaskDataForUpload.endTime,
                                        recurringTask = copyTaskDataForUpload.recurringTask,
                                        recurringDay = copyTaskDataForUpload.recurringDay,
                                        taskTime = if (addCurrentTime != 0L) addCurrentTime else data.taskTime,
                                        week = calculateWeeksAndDaysIncludingStartDate(startTime, if (addCurrentTime != 0L) addCurrentTime else data.taskTime).first.toString(),
                                        day = calculateWeeksAndDaysIncludingStartDate(startTime, if (addCurrentTime != 0L) addCurrentTime else data.taskTime).second.toString()
                                    )

                                    // 周期名字
                                    binding.etEmail.text = copyTaskDataForUpload.taskName
                                    // task时间
                                    binding.etEmails.text = "${getYmdForEn(time = copyTaskDataForUpload.taskTime * 1000L)} (${getString(com.cl.common_base.R.string.week)} ${copyTaskDataForUpload.week} ${getString(com.cl.common_base.R.string.day)} ${copyTaskDataForUpload.day})"
                                    // 周期任务描述
                                    binding.taskTextViews.text = copyTaskDataForUpload.taskdescription
                                    ViewUtils.setVisible(!copyTaskDataForUpload.taskdescription.isNullOrEmpty(), binding.taskTextViews)
                                }
                            })
                    ).show()
                }
            }
        }

        // 选择Task日期
        binding.etEmails.setSafeOnClickListener {
            if(addCurrentTime != 0L) {
                return@setSafeOnClickListener
            }
            showDatePickerDialog(this@ProModeTaskSetActivity, binding.etEmails, startTime * 1000L, endTime * 1000L) {
                val ymd = getYmdForEn(time = it)
                val (weeks, days) = calculateWeeksAndDaysIncludingStartDate(startTime, it / 1000L)
                binding.etEmails.text = "$ymd (${getString(com.cl.common_base.R.string.week)} $weeks ${getString(com.cl.common_base.R.string.day)} $days)"
                copyTaskDataForUpload.taskTime = it / 1000L
                copyTaskDataForUpload.week = weeks.toString()
                copyTaskDataForUpload.day = days.toString()
            }
        }

        binding.svtCancel.setSafeOnClickListener { finish() }
        binding.svtConfirm.setSafeOnClickListener {
            viewModel.saveTask(SaveTaskReq(step = step, templateId = templateId, taskContent = mutableListOf(copyTaskDataForUpload), multiplants =  adapter.data.filter { it.isSelect == true }.toMutableList()))
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

    /**
     * 计算包含起始日期在内的周数和天数。
     *
     * @param startTimestamp 起始时间戳（秒）。
     * @param endTimestamp 结束时间戳（秒）。
     * @param zoneId 时间区域，默认使用系统默认时区。
     * @return WeeksAndDays 表示周数和天数，天数范围为 1 到 7。
     * @throws IllegalArgumentException 如果结束时间早于起始时间。
     */
    private fun calculateWeeksAndDaysIncludingStartDate(
        startTimestamp: Long,
        endTimestamp: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<Int, Int> {
        // 输入验证
        val startDate = Instant.ofEpochSecond(startTimestamp).atZone(zoneId).toLocalDate()
        val endDate = Instant.ofEpochSecond(endTimestamp).atZone(zoneId).toLocalDate()

        // 计算总天数，包含起始日期
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1

        // 计算完整的周数
        val weeks = ((totalDays - 1) / 7).toInt()

        // 计算剩余的天数，范围为 1 到 7
        val days = ((totalDays - 1) % 7 + 1).toInt()

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