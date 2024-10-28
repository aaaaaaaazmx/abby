package com.cl.modules_home.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.BaseApplication
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.safeToLong
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.ChooseTimePop
import com.cl.common_base.pop.SelectPeriodTimePop
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.cl.modules_home.activity.ProModeStartActivity.Companion.IS_CURRENT_PERIOD
import com.cl.modules_home.adapter.ProModeEnvAdapter
import com.cl.modules_home.databinding.HomeProModeEnvActivityBinding
import com.cl.modules_home.request.EnvDeleteReq
import com.cl.modules_home.request.EnvParamListBeanItem
import com.cl.modules_home.request.EnvParamListReq
import com.cl.modules_home.request.EnvSaveReq
import com.cl.modules_home.viewmodel.ProModeViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.isSensitiveHeader
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class ProModeEnvActivity : BaseActivity<HomeProModeEnvActivityBinding>() {

    @Inject
    lateinit var viewModel: ProModeViewModel

    companion object {
        // step 当前周期标识
        const val STEP = "step"

        // stepNow 多语言翻译的当前周期文案
        const val STEP_NOW = "step_now"

        // m模板id
        const val TEMPLATE_ID = "template_id"

        // taskData
        const val TASK_DATA = "task_data"

        // 结束时间
        const val END_TIME = "end_time"

        // 周期开始时间
        const val START_TIME = "start_time"

        // 当前时间
        const val CURRENT_TIME = "current_time"

        // 循环的结束时间
        const val END_CYCLE_TIME = "end_cycle_time"

        // 是否是当前周期
        const val IS_CURRENT_PERIOD = "is_current_period"

        // 是否是修改灯光
        const val IS_UPDATE_LIGHT = "is_update_light"

        const val KEY_REQUEST_TASK_SET = 101
    }

    // 是否是当前周期,如果不为空，那么就是从首页的周期界面跳转过来的
    // 不为空时，true为当前周期，false为其他周期。
    private val isCurrentPeriod by lazy {
        intent.getStringExtra(IS_CURRENT_PERIOD)
    }

    private val daysList by lazy {
        mutableListOf(
            BaseApplication.getContext().getString(com.cl.common_base.R.string.string_2424),
            BaseApplication.getContext().getString(com.cl.common_base.R.string.string_2425),
            BaseApplication.getContext().getString(com.cl.common_base.R.string.string_2426),
            BaseApplication.getContext().getString(com.cl.common_base.R.string.string_2427),
            BaseApplication.getContext().getString(com.cl.common_base.R.string.string_2428),
            BaseApplication.getContext().getString(com.cl.common_base.R.string.string_2429),
            BaseApplication.getContext().getString(com.cl.common_base.R.string.string_2430)
        )
    }

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

    private val adapter by lazy {
        ProModeEnvAdapter(mutableListOf())
    }

    // 从环境卡片界面跳转过来修改灯光的，只需要更改这一个界面。
    private val isUpdateLight by lazy {
        intent.getBooleanExtra(IS_UPDATE_LIGHT, false)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.tvPeriod.text = "$stepShow"
        if (isUpdateLight) {
            binding.btnSuccess.text = getString(com.cl.common_base.R.string.base_ok)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun observe() {
        viewModel.apply {
            checkEnvParam.observe(this@ProModeEnvActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    when (data?.checkStatus) {
                        0 -> {
                            // 存在断层
                            xpopup(this@ProModeEnvActivity) {
                                dismissOnTouchOutside(false)
                                isDestroyOnDismiss(false)
                                asCustom(
                                    BaseCenterPop(
                                        this@ProModeEnvActivity,
                                        content = getString(com.cl.common_base.R.string.home_your_current_lighting_settings),
                                        titleText = getString(com.cl.common_base.R.string.home_incomplete_lighting_schedule),
                                        cancelText = getString(com.cl.common_base.R.string.home_back_to_editing),
                                        confirmText = getString(com.cl.common_base.R.string.home_proceed),
                                        onConfirmAction = {
                                            saveEnvParam(EnvSaveReq(list = adapter.data, step = step, templateId = templateId, useRecommend = false))
                                        })
                                ).show()
                            }
                        }

                        1 -> {
                            // 正确
                            saveEnvParam(EnvSaveReq(list = adapter.data, step = step, templateId = templateId, useRecommend = false))
                        }

                        2 -> {
                            // 存在时间上的冲突
                            ToastUtil.shortShow(data?.checkMeg)
                            if (data?.errorEnvId.isNullOrEmpty()) return@success
                            binding.recyclerView.smoothScrollToPosition(data?.errorEnvId?.get(0).safeToInt())
                        }
                    }
                }
            })

            saveEnvParam.observe(this@ProModeEnvActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (isUpdateLight) {
                        finish()
                        return@success
                    }
                    // 保存成功
                    // 跳转到测试任务配置界面
                    startActivity(Intent(this@ProModeEnvActivity, ProModeTaskActivity::class.java).apply {
                        putExtra(STEP_NOW, stepShow)
                        putExtra(STEP, step)
                        putExtra(TEMPLATE_ID, templateId)
                        putExtra(END_TIME,  viewModel.cycleEnvList.value?.data?.stepEnd?.safeToLong() ?: 0L)
                        putExtra(START_TIME,  viewModel.cycleEnvList.value?.data?.stepStart?.safeToLong() ?: 0L)
                        putExtra(IS_CURRENT_PERIOD, isCurrentPeriod)
                    })
                }
            })

            cycleEnvList.observe(this@ProModeEnvActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    adapter.setList(data?.list)
                }
            })


            deleteEnvParam.observe(this@ProModeEnvActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 只有最后一个可以删除
                    adapter.removeAt(adapter.data.size - 1)
                    val size = adapter.data.size
                    if (size == 0) return@success
                    adapter.notifyItemChanged(adapter.data.size - 1)
                }
            })
        }
    }

    /**
     * 计算出多少开始和结束时距离多少个星期
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

    private fun extractNumbers(input: String): Pair<Int, Int> {
        // 修改正则表达式，匹配 "Week X Y Day" 其中 X 为 week 数字，Y 为 day 数字，支持任意数量的空格
        val regex = """${getString(com.cl.common_base.R.string.week)}\s*(\d+)\s*(\d*)\s*${getString(com.cl.common_base.R.string.day)}""".toRegex()

        // 在输入字符串中查找第一个匹配项
        val matchResult = regex.find(input.trim())

        // 如果找到匹配项，提取 week 和 day 的数字并返回；否则返回 (0, 0)
        return if (matchResult != null) {
            val (weekNumber, dayNumber) = matchResult.destructured
            val day = if (dayNumber.isNotBlank()) dayNumber.toInt() else 0  // 如果 day 没有提供，则默认为 0
            Pair(weekNumber.toInt(), day)
        } else {
            Pair(0, 0)  // 没有匹配项时返回默认值
        }
    }

    override fun initData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this@ProModeEnvActivity)
        binding.recyclerView.adapter = adapter

        binding.tvRecommend.setSafeOnClickListener {
            xpopup(this@ProModeEnvActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(false)
                asCustom(BaseCenterPop(this@ProModeEnvActivity, content = getString(com.cl.common_base.R.string.home_you_w), onConfirmAction = {
                    viewModel.saveEnvParam(EnvSaveReq(list = adapter.data, step = step, templateId = templateId, useRecommend = true))
                })).show()
            }
        }

        binding.btnSuccess.setSafeOnClickListener {
            // 先检查
            viewModel.checkEnvParam(EnvSaveReq(list = adapter.data, step = step, templateId = templateId, useRecommend = false))
        }

        binding.ivAdd.setSafeOnClickListener {
            // Ensure adapter has data before proceeding
            if (adapter.data.isEmpty()) return@setSafeOnClickListener
            // Retrieve the last item from the list and create a new instance with modifications
            val lastData = adapter.data.lastOrNull() ?: return@setSafeOnClickListener

            val stepEnd = viewModel.cycleEnvList.value?.data?.stepEnd?.safeToLong() ?: 0L
            val stepStart = viewModel.cycleEnvList.value?.data?.stepStart?.safeToLong() ?: 0L
            val (weeks, days) = calculateWeeksAndDaysIncludingStartDate(stepStart, stepEnd)
            // Check if the last data entry has the same week and day as the current entry
            if (lastData.week == weeks.safeToInt() && lastData.day == days.safeToInt()) {
                // Show a message indicating the user needs to edit the end date if both are identical
                ToastUtil.shortShow(getString(com.cl.common_base.R.string.home_please_edit_enddate, "${lastData.envName}"))
                return@setSafeOnClickListener
            }

            // Check if the last data entry is on the same week but occurs on a later day
            if (lastData.week == weeks.safeToInt() && lastData.day > days.safeToInt()) {
                // Show a message indicating the user needs to edit the end date since the day is later
                ToastUtil.shortShow(getString(com.cl.common_base.R.string.home_please_edit_enddate, "${lastData.envName}"))
                return@setSafeOnClickListener
            }

            // Optional: Check if the last data entry's week is greater than the current week (future week)
            if (lastData.week > weeks) {
                // Show a message indicating the user needs to edit the end date since the week is in the future
                ToastUtil.shortShow(getString(com.cl.common_base.R.string.home_please_edit_enddate, "${lastData.envName}"))
                return@setSafeOnClickListener
            }

            val beforeWeek = if (lastData.day == 7) lastData.week + 1 else lastData.week
            val beforeDay = if (lastData.day == 7) 1 else lastData.day + 1

            val newData = lastData.copy(
                runningOn = false,
                envId = null,
                envName = getString(com.cl.common_base.R.string.home_profilessss, "${(adapter.data.size + 1)}"),
                week = weeks.safeToInt(),
                day = days.safeToInt(),
                sweek = beforeWeek,
                sday =  beforeDay
            )

            // Add the new data to the adapter
            adapter.addData(newData)

            // Refresh the second-to-last item to hide the delete button
            val previousIndex = adapter.data.size - 2
            if (previousIndex >= 0) {
                adapter.notifyItemChanged(previousIndex)
            }

            // Smooth scroll to the newly added item
            binding.recyclerView.smoothScrollToPosition(adapter.data.size - 1)
        }

        adapter.addChildClickViewIds(R.id.iv_close, R.id.tv_date_rang, R.id.ft_timer)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val data = (adapter.data[position] as? EnvParamListBeanItem)
            when (view.id) {
                R.id.iv_close -> {
                    if (data?.envId.isNullOrEmpty()) {
                        adapter.removeAt(position)
                        val size = adapter.data.size
                        if (size == 0) return@setOnItemChildClickListener
                        adapter.notifyItemChanged(adapter.data.size - 1)
                    } else {
                        // 调用删除接口。
                        viewModel.deleteEnvParam(EnvDeleteReq(envId = data?.envId.safeToInt(), templateId.toString()))
                    }
                }

                R.id.tv_date_rang -> {
                    // 获取 stepEnd 和 stepStart 的时间戳，计算周和天的差异
                    val stepEnd = viewModel.cycleEnvList.value?.data?.stepEnd?.safeToLong() ?: 0L
                    val stepStart = viewModel.cycleEnvList.value?.data?.stepStart?.safeToLong() ?: 0L
                    val (weeks, days) = calculateWeeksAndDaysIncludingStartDate(stepStart, stepEnd)
                    logI("The difference is $weeks weeks and $days days.")

                    // 获取当前模板的结束时间 (WeekX DayY)
                    val endWeekDay = "${getString(com.cl.common_base.R.string.week)} ${data?.week} ${getString(com.cl.common_base.R.string.day)} ${data?.day}"

                    // 生成从 Week1 到 WeekX 的列表
                    val weekList = (1..weeks.coerceAtLeast(1)).map { "${getString(com.cl.common_base.R.string.week)} $it" }.toMutableList()

                    // 生成从 Day0 到 DaysX 的列表
                    val dayList = (0..days.coerceAtLeast(0)).map { "${getString(com.cl.common_base.R.string.day)} $it" }.toMutableList()

                    // 弹出选择周期时间的窗口
                    xpopup(this@ProModeEnvActivity) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(false)
                        asCustom(SelectPeriodTimePop(context = this@ProModeEnvActivity, timeString = endWeekDay, weekList = weekList, dayList = daysList, selectAction = { week, day ->
                            // 可以在这里处理选择的 week 和 day
                            val extractNumbers = extractNumbers("$week $day")
                            data?.week = extractNumbers.first
                            data?.day = extractNumbers.second
                            adapter.notifyItemChanged(position)
                        })).show()
                    }
                }

                R.id.ft_timer -> {
                    // 获取当前开关灯的时间
                    // 0- 12, 12-24
                    val startTime = when (data?.turnOnLight) {
                        0 -> 12
                        12 -> 24
                        else -> data?.turnOnLight ?: 12
                    }

                    val endTime = when (data?.turnOffLight) {
                        0 -> 12
                        12 -> 24
                        else -> data?.turnOffLight ?: 12
                    }

                    val ftTurnOn = startTime.let {
                        if (it > 12) {
                            "${it - 12}:00 PM"
                        } else if (it < 12) {
                            "${it}:00 AM"
                        } else if (it == 12) {
                            "12:00 AM"
                        } else {
                            "12:00 AM"
                        }
                    }

                    val ftTurnOff = endTime.let {
                        if (it > 12) {
                            "${it - 12}:00 PM"
                        } else if (it < 12) {
                            "${it}:00 AM"
                        } else if (it == 12) {
                            "12:00 AM"
                        } else {
                            "12:00 AM"
                        }
                    }

                    // 发送dp点
                    val lightSchedule = "$ftTurnOn-$ftTurnOff"
                    viewModel.setmuteOff(endTime.toString())
                    viewModel.setmuteOn(startTime.toString())

                    // 选择开关灯时间
                    chooserTime(position)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 获取环境列表
        viewModel.getCycleEnvList(EnvParamListReq(step = step.toString(), templateId = templateId.toString()))
    }

    private val pop by lazy {
        XPopup.Builder(this@ProModeEnvActivity)
    }

    private fun chooserTime(position: Int) {
        pop.asCustom(
            ChooseTimePop(
                this@ProModeEnvActivity,
                turnOnText = getString(com.cl.common_base.R.string.string_1359),
                turnOffText = getString(com.cl.common_base.R.string.string_1360),
                isShowNightMode = false,
                isTheSpacingHours = false,
                turnOnHour = viewModel.muteOn?.safeToInt(),
                turnOffHour = viewModel.muteOff?.safeToInt(),
                isProMode = false,
                proModeAction = { onTime, offMinute, timeOn, timeOff, timeOpenHour, timeCloseHour, lightIntensity ->
                    timeOn?.let {
                        adapter.data[position].turnOnLight = if (it >= 12) it - 12 else it
                    }
                    timeOff?.let {
                        adapter.data[position].turnOffLight = if (it >= 12) it - 12 else it
                    }
                    adapter.notifyItemChanged(position)
                })
        ).show()
    }

}