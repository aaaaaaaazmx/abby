package com.cl.modules_my.ui

import android.Manifest
import android.content.Context
import android.content.res.AssetManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.*
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.calendar.CalendarEventUtil
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.AbTextViewCalendar
import com.cl.common_base.widget.SvTextView
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.MyCalendarAdapter
import com.cl.modules_my.databinding.MyCalendayActivityBinding
import com.cl.modules_my.viewmodel.CalendarViewModel
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.joketng.timelinestepview.LayoutType
import com.joketng.timelinestepview.OrientationShowType
import com.joketng.timelinestepview.adapter.TimeLineStepAdapter
import com.joketng.timelinestepview.view.TimeLineStepView
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.sign


@Route(path = RouterPath.My.PAGE_MY_CALENDAR)
@AndroidEntryPoint
class CalendarActivity : BaseActivity<MyCalendayActivityBinding>() {

    @Inject
    lateinit var mViewMode: CalendarViewModel

    private val adapter by lazy {
        MyCalendarAdapter(mutableListOf())
    }

    private val pop by lazy {
        XPopup.Builder(this@CalendarActivity)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
    }

    override fun initView() {
        // 设置标题颜色以及标题文案
        binding.title.setTitle(getString(com.cl.common_base.R.string.my_calendar))
            .setTitleColor(com.cl.common_base.R.color.mainColor)
            .setQuickClickListener {
                // 会滚到当前日期
                val data = adapter.data
                if (data.isEmpty()) return@setQuickClickListener
                val layoutManager = binding.rvList.layoutManager as GridLayoutManager
                val findFirstVisibleItemPosition =
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                val currentPosition = data.indexOfFirst { it.isCurrentDay }
                if (currentPosition > findFirstVisibleItemPosition) {
                    // 表示在后面
                    binding.rvList.scrollToPosition(currentPosition + 7 * 3)
                } else if (currentPosition < findFirstVisibleItemPosition) {
                    binding.rvList.scrollToPosition(currentPosition - 7)
                }
                // 设置今天的日子
                binding.abMonth.text = CalendarUtil.getMonthFromLocation(Date().time)
                binding.tvTodayDate.text = mViewMode.getYmdForEn(Date())
            }

        // 初始化本地日历数据
        initCalendarData()
        // 适配器
        binding.rvList.layoutManager = GridLayoutManager(this@CalendarActivity, 7)
        binding.rvList.adapter = adapter

        // help
        val snapHelper = GravitySnapHelper(Gravity.TOP)
        snapHelper.attachToRecyclerView(binding.rvList)

        // 点击事件
        adapter.addChildClickViewIds(R.id.tv_content_day, R.id.rl_root)
        // 设置滑动速度
        setMaxFlingVelocity(binding.rvList, 2000)
        // 初始化年月日
        binding.tvTodayDate.text = mViewMode.getYmdForEn(Date())
    }

    private fun initCalendarData() {
        // 添加本地日历的数据
        // todo 需要判断当前的月份，来显示与加载，如果当前是1月，需要加载上面的，如果当前是12月，需要加载下面一年的
        mViewMode.getCalendar(
            CalendarUtil.getYearStartDay(
                CalendarUtil.getFormat("yyyy").format(Date().time).toInt()
            ),
            CalendarUtil.getYearEndDay(CalendarUtil.getFormat("yyyy").format(Date().time).toInt())
        )
        // 添加本地12个月的数据
        mViewMode.getLocalCalendar(
            year = CalendarUtil.getFormat("yyyy").format(Date().time).toInt()
        )
    }

    override fun MyCalendayActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@CalendarActivity
            viewModel = mViewMode
            executePendingBindings()
        }
    }

    override fun observe() {
        mViewMode.apply {
            // 获取换水的图文接口
            advertising.observe(this@CalendarActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    pop
                        .enableDrag(false)
                        .maxHeight(dp2px(600f))
                        .dismissOnTouchOutside(false)
                        .asCustom(
                            BasePumpWaterPop(
                                this@CalendarActivity,
                                { status ->
                                    // 涂鸦指令，添加排水功能
                                    DeviceControl.get()
                                        .success {
                                            // todo 设备下发命令成功
                                        }
                                        .error { code, error ->
                                            ToastUtil.shortShow(
                                                """
                                                pumpWater: 
                                                code-> $code
                                                errorMsg-> $error
                                            """.trimIndent()
                                            )
                                        }
                                        .pumpWater(status)
                                },
                                onWaterFinishedAction = {
                                    // 排水结束，那么直接弹出
                                    pop
                                        .isDestroyOnDismiss(false)
                                        .enableDrag(false)
                                        .maxHeight(dp2px(600f))
                                        .dismissOnTouchOutside(false)
                                        .asCustom(
                                            BasePumpWaterFinishedPop(
                                                this@CalendarActivity,
                                                onSuccessAction = {
                                                    // 排水成功弹窗，点击OK按钮
                                                    // 排水成功、上报结束
                                                    mViewMode.deviceOperateFinish(UnReadConstants.StatusManager.VALUE_STATUS_PUMP_WATER)
                                                })
                                        ).show()
                                },
                                data = this.data,
                            )
                        ).show()
                }
            })

            // 排水结束上报 - deviceOperateFinish
            deviceOperateFinish.observe(this@CalendarActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 判断水箱里面的水位
                    when (mViewMode.getWaterVolume.value) {
                        // 加水弹窗
                        "0L" -> {
                            // 加水弹窗
                            mViewMode.taskId.value?.let {
                                mViewMode.deviceOperateStart(
                                    it,
                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_WATER
                                )
                            }
                            pop
                                .isDestroyOnDismiss(false)
                                .enableDrag(false)
                                .dismissOnTouchOutside(false)
                                .asCustom(
                                    HomePlantFourPop(
                                        context = this@CalendarActivity,
                                        onNextAction = {
                                            // 加水弹窗
                                            pop
                                                .isDestroyOnDismiss(false)
                                                .enableDrag(false)
                                                .dismissOnTouchOutside(false)
                                                .asCustom(
                                                    HomePlantFivePop(
                                                        context = this@CalendarActivity,
                                                        onCancelAction = {},
                                                        onNextAction = {
                                                            // 如果是在换水的三步当中
                                                            mViewMode.taskId.value?.let {
                                                                mViewMode.deviceOperateStart(
                                                                    it,
                                                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_MANURE
                                                                )
                                                            }

                                                            pop
                                                                .isDestroyOnDismiss(false)
                                                                .maxHeight(dp2px(600f))
                                                                .enableDrag(false)
                                                                .dismissOnTouchOutside(false)
                                                                .asCustom(
                                                                    // 加肥弹窗
                                                                    HomePlantSixPop(
                                                                        context = this@CalendarActivity,
                                                                        onNextAction = {
                                                                            // 需要先发送指令喂食
                                                                            DeviceControl.get()
                                                                                .success {
                                                                                    // 如果是在换水的三步当中的最后一步，加肥
                                                                                    // 直接调用完成任务
                                                                                    mViewMode.taskId.value?.let {
                                                                                        mViewMode.finishTask(
                                                                                            it
                                                                                        )
                                                                                    }
                                                                                }
                                                                                .error { code, error ->
                                                                                    ToastUtil.shortShow(
                                                                                        """
                                                                                        feedAbby:
                                                                                        code-> $code
                                                                                        errorMsg-> $error
                                                                                    """.trimIndent()
                                                                                    )
                                                                                }
                                                                                .feedAbby(true)
                                                                        }
                                                                    )
                                                                ).show()

                                                        }
                                                    )
                                                ).show()
                                        }
                                    )
                                ).show()
                        }
                        else -> {
                            // 如果是在换水的三步当中
                            mViewMode.taskId.value?.let {
                                mViewMode.deviceOperateStart(
                                    it,
                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_MANURE
                                )
                            }

                            pop
                                .isDestroyOnDismiss(false)
                                .maxHeight(dp2px(600f))
                                .enableDrag(false)
                                .dismissOnTouchOutside(false)
                                .asCustom(
                                    // 加肥弹窗
                                    HomePlantSixPop(
                                        context = this@CalendarActivity,
                                        onNextAction = {
                                            // 需要先发送指令喂食
                                            DeviceControl.get()
                                                .success {
                                                    // 如果是在换水的三步当中的最后一步，加肥
                                                    // 直接调用完成任务
                                                    mViewMode.taskId.value?.let {
                                                        mViewMode.finishTask(
                                                            it
                                                        )
                                                    }
                                                }
                                                .error { code, error ->
                                                    ToastUtil.shortShow(
                                                        """
                                                              feedAbby:
                                                              code-> $code
                                                              errorMsg-> $error
                                                        """.trimIndent()
                                                    )
                                                }
                                                .feedAbby(true)
                                        }
                                    )
                                ).show()
                        }
                    }
                }
            })

            // guideInfo
            getGuideInfo.observe(this@CalendarActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 给通用弹窗赋值
                    pop
                        .enableDrag(true)
                        .maxHeight(dp2px(700f))
                        .dismissOnTouchOutside(false)
                        .isDestroyOnDismiss(false)
                        .asCustom(
                            BasePlantUsuallyPop(
                                this@CalendarActivity,
                                onNextAction = {
                                    // 判断当前的周期状态
                                    val status = mViewMode.guideInfoStatus.value
                                    if (status.isNullOrEmpty()) return@BasePlantUsuallyPop
                                    when (status) {
                                        CalendarData.TASK_TYPE_CHANGE_WATER -> {
                                        }
                                        CalendarData.TASK_TYPE_CHANGE_CUP_WATER -> {
                                            // todo 任务完成
                                        }
                                        CalendarData.TASK_TYPE_LST -> {
                                            // todo 任务完成
                                        }
                                        CalendarData.TASK_TYPE_TOPPING -> {
                                            // todo 任务完成
                                        }
                                        CalendarData.TASK_TYPE_TRIM -> {
                                            // todo 任务完成
                                        }
                                        CalendarData.TASK_TYPE_CHECK_TRANSPLANT -> {
                                            // todo 这个应该是转周期了，调用图文、然后解锁花期
                                            mViewMode.unlockJourney(CalendarData.TASK_TYPE_CHECK_TRANSPLANT)
                                        }
                                        CalendarData.TASK_TYPE_CHECK_CHECK_FLOWERING -> {
                                            mViewMode.unlockJourney(CalendarData.TASK_TYPE_CHECK_TRANSPLANT)
                                        }
                                        CalendarData.TASK_TYPE_CHECK_CHECK_FLUSHING -> {
                                            mViewMode.unlockJourney(CalendarData.TASK_TYPE_CHECK_TRANSPLANT)
                                        }
                                        CalendarData.TASK_TYPE_CHECK_CHECK_DRYING -> {
                                            mViewMode.unlockJourney(CalendarData.TASK_TYPE_CHECK_TRANSPLANT)
                                        }
                                        CalendarData.TASK_TYPE_CHECK_CHECK_CURING -> {
                                            mViewMode.unlockJourney(CalendarData.TASK_TYPE_CHECK_TRANSPLANT)
                                        }
                                        CalendarData.TASK_TYPE_CHECK_CHECK_FINISH -> {
                                            mViewMode.unlockJourney(CalendarData.TASK_TYPE_CHECK_TRANSPLANT)
                                        }

                                    }
                                },
                            ).setData(data)
                        ).show()
                }
            })

            // 本地数据
            localCalendar.observe(this@CalendarActivity) {
                if (it.isNullOrEmpty()) return@observe
                when (mViewMode.scrollMonth.value) {
                    -1 -> {
                        // 优先加载本地数据、等网络数据返回了之后添加这玩意
                        adapter.setList(it)
                        // 滚到到当前日期到上一行
                        // todo 但是后续添加不需要滚到到现在这一行
                        binding.rvList.scrollToPosition(it.indexOf(mViewMode.mCurrentDate) - 7)
                        // todo  初始化当月,, 固定写法只加7，会出现时间差错问题
                        binding.abMonth.text =
                            CalendarUtil.getMonthFromLocation(adapter.data[it.indexOf(mViewMode.mCurrentDate) + 7].timeInMillis)
                    }
                    1, 2, 3, 4 -> {
                        adapter.addData(0, it)
                    }
                    5, 6, 7 -> {}
                    8, 9, 10, 11, 12 -> {
                        //
                        adapter.addData(it)
                    }
                }
            }

            // 获取日历任务
            getCalendar.observe(this@CalendarActivity, resourceObserver {
                loading { }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    hideProgressLoading()
                    // 添加数据。``
                    // todo 是个数组
                    if (data.isNullOrEmpty()) return@success
                    if (mViewMode.localCalendar.value.isNullOrEmpty()) return@success
                    lifecycleScope.launch(Dispatchers.IO) {
                        // 合并数据
                        val local = mViewMode.localCalendar.value
                        local?.let { localData ->

                            // 加载当前月份的上下2个月，一共5个月
                            val isCurrentDay = localData.first { it.isCurrentDay }
                            val currentYear = isCurrentDay.year
                            val currentMonth = isCurrentDay.month
                            // 在这个区间之内，可以优先加载上下2个月
                            if (currentMonth in 2..11) {
                                val startDay =
                                    CalendarUtil.getYearMonthStartDay(currentYear, currentMonth - 1)
                                val endDay =
                                    CalendarUtil.getYearMonthEndDay(currentYear, currentMonth + 1)
                                val locationStartDayIndex = localData.indexOfFirst {
                                    it.ymd == startDay
                                }
                                val locationEndDayIndex = localData.indexOfFirst {
                                    it.ymd == endDay
                                }

                                for (i in locationStartDayIndex until locationEndDayIndex) {
                                    data?.firstOrNull { it.date == localData[i].ymd }.apply {
                                        localData[i].calendarData = this
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    showTaskList(mViewMode.mCurrentDate)
                                    // 设置数据
                                    adapter.setList(local)
                                }
                            } else if (currentMonth < 2) {
                                // 那么需要加载上一年的2个月数据
                                val startDay =
                                    CalendarUtil.getYearMonthStartDay(
                                        currentYear - 1,
                                        12 - abs((currentMonth - 1))
                                    )
                                val endDay =
                                    CalendarUtil.getYearMonthEndDay(currentYear, currentMonth + 1)
                                val locationStartDayIndex = localData.indexOfFirst {
                                    it.ymd == startDay
                                }
                                val locationEndDayIndex = localData.indexOfFirst {
                                    it.ymd == endDay
                                }

                                for (i in locationStartDayIndex until locationEndDayIndex) {
                                    data?.firstOrNull { it.date == localData[i].ymd }.apply {
                                        localData[i].calendarData = this
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    showTaskList(mViewMode.mCurrentDate)
                                    // 设置数据
                                    adapter.setList(local)
                                }
                            } else if (currentMonth > 11) {
                                val startDay =
                                    CalendarUtil.getYearMonthStartDay(
                                        currentYear - 1,
                                        currentMonth - 1
                                    )
                                val endDay =
                                    CalendarUtil.getYearMonthEndDay(
                                        currentYear,
                                        abs((currentMonth + 1) - 12)
                                    )
                                val locationStartDayIndex = localData.indexOfFirst {
                                    it.ymd == startDay
                                }
                                val locationEndDayIndex = localData.indexOfFirst {
                                    it.ymd == endDay
                                }

                                for (i in locationStartDayIndex until locationEndDayIndex) {
                                    data?.firstOrNull { it.date == localData[i].ymd }.apply {
                                        localData[i].calendarData = this
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    showTaskList(mViewMode.mCurrentDate)
                                    // 设置数据
                                    adapter.setList(local)
                                }
                            }

                            // todo 这个快速点击就崩溃
                            // 加载完整的数据，会有卡顿，需要放到后面去添加
                            localData.forEachIndexed { _, calendar ->
                                data?.firstOrNull { it.date == calendar.ymd }.apply {
                                    calendar.calendarData = this
                                }
                            }

                            withContext(Dispatchers.Main) {
                                showTaskList(mViewMode.mCurrentDate, true)
                                // 设置数据
                                adapter.setList(local)
                            }
                        }

                    }
                }
            })

            // 更新日历任务
            updateTask.observe(this@CalendarActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    errorMsg?.let {
                        hideProgressLoading()
                        ToastUtil.shortShow(it)
                    }
                    success {
                        hideProgressLoading()
                    }
                }
            })

        }
    }

    override fun initData() {
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val list = adapter.data as? MutableList<com.cl.common_base.util.calendar.Calendar>
            val data = adapter.data[position] as? com.cl.common_base.util.calendar.Calendar
            when (view.id) {
                R.id.tv_content_day -> {
                    if (list.isNullOrEmpty()) return@setOnItemChildClickListener
                    view.background = ContextCompat.getDrawable(
                        this@CalendarActivity,
                        com.cl.common_base.R.drawable.base_dot_main_color
                    )

                    // 设置为true
                    // 判断点击的是否是今日
                    list.indexOfFirst { it.isChooser }.let {
                        if (it != -1) {
                            list[it].isChooser = false
                            adapter.notifyItemChanged(it)
                        }
                    }

                    data?.isChooser = true
                    adapter.notifyItemChanged(position)

                    // 设置选中动效
                    //缩小
                    val animation = ScaleAnimation(
                        1.0f, 0.5f, 1.0f, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                    )
                    animation.duration = 600 //执行时间
                    animation.repeatCount = 0 //重复执行动画
                    view.startAnimation(animation) //使用View启动动画

                    // todo 时间转换，并且需要请求接口
                    data?.timeInMillis?.let {
                        binding.tvTodayDate.text = mViewMode.getYmdForEn(time = it)
                    }

                    // 显示下面的taskList
                    showTaskList(data, true)
                }


            }
        }


        // 滑动状态监听
        binding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /**
             *当 RecyclerView的滑动状态改变时回调方法被调用。
             *
             * @param recyclerView
             * @param newState     滚动状态。以下其中一个：
             * 						RecyclerView.SCROLL_STATE_IDLE
             *                      RecyclerView.SCROLL_STATE_DRAGGING
             *                      RecyclerView.SCROLL_STATE_SETTLING
             *
             *                      findFirstVisibleItemPositions(int[]) ：返回第一个可见span的items的位置
            findLastVisibleItemPositions(int[]) ：返回最后一个可见span的items的位置
            findFirstCompletelyVisibleItemPositions(int[]) ：返回第一个完全可见span的items的位置
            findLastCompletelyVisibleItemPositions(int[]) ：返回最后一个完全可见span的items的位置

             */
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                /**
                 * 只用当他闲置的时候，去加载数据
                 */
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        layoutManager.findFirstVisibleItemPosition()
                        layoutManager.findFirstCompletelyVisibleItemPosition()
                        layoutManager.findLastVisibleItemPosition()
                        layoutManager.findLastCompletelyVisibleItemPosition()

                        //                        logI(
                        //                            """
                        //                               ${layoutManager.findFirstVisibleItemPosition()}
                        //                               ${layoutManager.findFirstCompletelyVisibleItemPosition()}
                        //                               ${layoutManager.findLastVisibleItemPosition()}
                        //                               ${layoutManager.findLastCompletelyVisibleItemPosition()}
                        //                            """.trimIndent()
                        //                        )

                        // 查看当前第三行的中间。
                        scrollByDate()
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        //                        logI(
                        //                            """
                        //                            SCROLL_STATE_DRAGGING:
                        //                            ${layoutManager.findFirstVisibleItemPosition()}
                        //                               ${layoutManager.findFirstCompletelyVisibleItemPosition()}
                        //                               ${layoutManager.findLastVisibleItemPosition()}
                        //                               ${layoutManager.findLastCompletelyVisibleItemPosition()}
                        //                        """.trimIndent()
                        //                        )

                        // 手指按下去的操作、然后抬起的瞬间
                        if (adapter.data.isEmpty()) return
                        val firstPosition = layoutManager.findFirstVisibleItemPosition()
                        val data = adapter.data[firstPosition]

                        // 如果是小于等于6，那么上一年
                        if (data.month <= 4) {
                            // 如果上一年的数据已经添加了。那么就不需要添加了。
                            // 如果已经加载了上一年了，就不需要重复加载
                            if (adapter.data.firstOrNull { it.year == data.year - 1 && it.month == 5 && it.day == 20 } != null) return
                            mViewMode.setScrollMonth(data.month)
                            mViewMode.getLocalCalendar(1, 12, data.year - 1)
                            mViewMode.getCalendar(
                                CalendarUtil.getYearStartDay(data.year - 1),
                                CalendarUtil.getYearEndDay(data.year - 1)
                            )
                        }
                        if (data.month >= 8) {
                            // 如果已经加载了下一年了，就不需要重复加载
                            // 加载下一年
                            // 如果已经加载了下一年的数据了，那么直接返回，
                            if (adapter.data.firstOrNull { it.year == data.year + 1 && it.month == 5 && it.day == 20 } != null) return
                            mViewMode.setScrollMonth(data.month)
                            mViewMode.getLocalCalendar(1, 12, data.year + 1)
                            mViewMode.getCalendar(
                                CalendarUtil.getYearStartDay(data.year + 1),
                                CalendarUtil.getYearEndDay(data.year + 1)
                            )
                        }
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        //                        logI(
                        //                            """
                        //                            SCROLL_STATE_SETTLING:
                        //                            ${layoutManager.findFirstVisibleItemPosition()}
                        //                               ${layoutManager.findFirstCompletelyVisibleItemPosition()}
                        //                               ${layoutManager.findLastVisibleItemPosition()}
                        //                               ${layoutManager.findLastCompletelyVisibleItemPosition()}
                        //                        """.trimIndent()
                        //                        )


                    }
                }

            }

            /**
             * 当 RecyclerView 滚动时，回调方法被调用。这个方法会在滚动完成后被调用。
             * 如果布局计算后可见项发生范围变化（item range changes），也将调用此回调。
             * 这种情况下， dx 和 dy 会为 0.
             *
             * @param recyclerView
             * @param dx 水平（horizontal scroll）滚量（距离）
             * @param dy 竖直（vertical scroll）滚动量
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // todo 这样加载太暴力了。
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                //                logI(
                //                    """
                //                            onScrolled:
                //                            ${layoutManager.findFirstVisibleItemPosition()}
                //                               ${layoutManager.findFirstCompletelyVisibleItemPosition()}
                //                               ${layoutManager.findLastVisibleItemPosition()}
                //                               ${layoutManager.findLastCompletelyVisibleItemPosition()}
                //                        """.trimIndent()
                //                )
            }
        })


        // 点击事件
        clickEvent()
    }


    /**
     * 获取当前页面的第三行的第5个日期，用来判断当前的年月日
     */
    private fun scrollByDate() {
        val layoutManager = binding.rvList.layoutManager as? GridLayoutManager
        val thirdLineFirst =
            layoutManager?.findFirstCompletelyVisibleItemPosition()?.plus(17)
        if (adapter.data.isEmpty()) return
        // 格式化时间戳
        thirdLineFirst?.let {
            binding.abMonth.text =
                CalendarUtil.getMonthFromLocation(adapter.data[it].timeInMillis)
        }
    }

    private fun showTaskList(
        data: com.cl.common_base.util.calendar.Calendar?,
        isExecutionAlphaAni: Boolean? = false
    ) {
        val getCalendarDate = mViewMode.getCalendar.value?.data
        if (getCalendarDate?.isEmpty() == true) return
        val calendarData = getCalendarDate?.firstOrNull { it.date == data?.ymd }
        // 刷新上面的背景框
        val startTime = calendarData?.epochStartTime ?: ""
        val endTime = calendarData?.epochEndTime ?: ""
        // 计算两个时间相差多少天
        val diffDay = CalendarUtil.getDatePoor(
            DateHelper.formatToLong(endTime, "yyyy-MM-dd"),
            DateHelper.formatToLong(startTime, "yyyy-MM-dd")
        )
        val currentPosition = adapter.data.indexOfFirst { it.ymd == startTime }
        adapter.data.filter { it.isShowBg }.forEach {
            val i = adapter.data.indexOf(it)
            adapter.data[i].isShowBg = false
            adapter.notifyItemChanged(i)
        }
        logI(
            """
            diffDay：
            $diffDay
            $currentPosition
        """.trimIndent()
        )

        // 绘制点击之后的
        // 相差多天，然后统一通知，刷新背景
        if (currentPosition != -1) {
            for (i in currentPosition..(currentPosition + diffDay)) {
                adapter.data[i].isShowBg = true
                adapter.notifyItemChanged(i)
            }
        }

        if (isExecutionAlphaAni == true) {
            // 产品需要一个从0-1的alpha动画
            // Alpha动画 安排！
            val animation = AlphaAnimation(
                0f, 1f
            )
            animation.duration = 1000 //执行时间
            animation.repeatCount = 0 //重复执行动画
            binding.llRoot.startAnimation(animation) //使用View启动动画
        }

        // 如果日历的数据为空，那么直接隐藏时间轴、显示其他的背景
        ViewUtils.setVisible(
            null == calendarData,
            binding.svtDayBg,
            binding.svtPeriodBg,
            binding.svtTaskListBg
        )
        // 如果日历的数据为空，那么直接隐藏时间轴、显示其他的背景
        ViewUtils.setGone(binding.timeLine, null == calendarData)

        // 如果日历数据不为空，那么开始加载数据
        calendarData?.let {
            // 设置下面卡片的数据
            // 周期
            binding.tvCycle.text = it.epoch
            // 天数
            binding.tvDay.text = "Day${it.day}"
            // 判断当前周期有无任务, 显示空布局 or 展示时间轴
            ViewUtils.setVisible(it.taskList.isNullOrEmpty(), binding.rlEmpty)
            ViewUtils.setVisible(!it.taskList.isNullOrEmpty(), binding.timeLine)
            initTime(it.taskList ?: mutableListOf())
        }
    }

    private fun clickEvent() {
        binding.rlCycle.setOnClickListener {

        }
    }

    // 反射进行这是最大速度的设定
    // 设定RecyclerView最大滑动速度
    private fun setMaxFlingVelocity(rv: RecyclerView, velocity: Int) {
        try {
            val field: Field = rv.javaClass.getDeclaredField("mMaxFlingVelocity")
            field.isAccessible = true
            field.set(rv, velocity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化时间轴
     */
    private fun initTime(data: MutableList<CalendarData.TaskList>) {
        val listContent = mutableListOf<CalendarData.TaskList>()
        listContent.addAll(data)
        listContent.add(0, CalendarData.TaskList())
        binding.timeLine.initData(
            listContent,
            OrientationShowType.CENTER_VERTICAL,
            object : TimeLineStepView.OnInitDataCallBack {
                override fun onBindDataViewHolder(
                    holder: TimeLineStepAdapter.CustomViewHolder,
                    position: Int
                ) {
                    if (position == 0) {
                        holder.rightLayout.visibility = View.GONE
                        holder.leftLayout.visibility = View.GONE
                        val layoutParams = holder.imgMark.layoutParams as LinearLayout.LayoutParams
                        layoutParams.width = dp2px(0f)
                        layoutParams.height = dp2px(0f)
                        holder.llLine.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                        if (position == 0) holder.imgMark.setImageDrawable(null) else holder.imgMark.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@CalendarActivity,
                                R.mipmap.my_iv_red_circle
                            )
                        )
                        return
                    }
                    val layoutParams = holder.imgMark.layoutParams as LinearLayout.LayoutParams
                    holder.llLine.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                    holder.imgLineEnd.layoutParams.width = dp2px(0.8f)
                    holder.imgLineStart.layoutParams.width = dp2px(0.8f)
                    holder.imgMark.scaleType = ImageView.ScaleType.CENTER_CROP

                    // 判断周期
                    logI(
                        """
                        initTime ->  time:
                        ${Date().time}
                        ${listContent[position].taskTime?.toLong() ?: 0L}
                        ${
                            DateHelper.after(
                                Date(),
                                Date(listContent[position].taskTime?.toLong() ?: 0L)
                            )
                        }
                        ${
                            CalendarUtil.getFormat("yyyy-MM-dd")
                                .format(listContent[position].taskTime?.toLong() ?: 0L)
                        }
                        ${CalendarUtil.getFormat("yyyy-MM-dd").format(Date().time)}
                    """.trimIndent()
                    )
                    if (CalendarUtil.getFormat("yyyy-MM-dd").format(
                            listContent[position].taskTime?.toLong() ?: 0L
                        ) == CalendarUtil.getFormat("yyyy-MM-dd").format(Date().time)
                    ) {
                        // 当前时间等于taskTime
                        // 当前时间小于taskTime(任务时间)
                        when (listContent[position].taskCategory) {
                            CalendarData.TYPE_CHANGE_WATER -> {
                                holder.imgMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@CalendarActivity,
                                        com.cl.common_base.R.mipmap.my_calendar_circle_bg
                                    )
                                )
                            }
                            CalendarData.TYPE_PERIOD_CHECK -> {
                                holder.imgMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@CalendarActivity,
                                        com.cl.common_base.R.mipmap.my_calendar_circle_three_bg
                                    )
                                )
                            }
                            CalendarData.TYPE_TRAIN -> {
                                holder.imgMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@CalendarActivity,
                                        com.cl.common_base.R.mipmap.my_calendar_circle_two_bg
                                    )
                                )
                            }
                        }
                    } else if (DateHelper.after(
                            Date(),
                            Date(listContent[position].taskTime?.toLong() ?: 0L)
                        )
                    ) {
                        // 当前时间大于taskTime(任务时间)
                        holder.imgMark.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@CalendarActivity,
                                com.cl.common_base.R.drawable.base_dot_gray
                            )
                        )
                    } else if (DateHelper.after(
                            Date(
                                listContent[position].taskTime?.toLong() ?: 0L
                            ), Date()
                        )
                    ) {
                        // 当前时间小于taskTime(任务时间)
                        when (listContent[position].taskCategory) {
                            CalendarData.TYPE_CHANGE_WATER -> {
                                holder.imgMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@CalendarActivity,
                                        com.cl.common_base.R.mipmap.my_calendar_circle_bg
                                    )
                                )
                            }
                            CalendarData.TYPE_PERIOD_CHECK -> {
                                holder.imgMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@CalendarActivity,
                                        com.cl.common_base.R.mipmap.my_calendar_circle_three_bg
                                    )
                                )
                            }
                            CalendarData.TYPE_TRAIN -> {
                                holder.imgMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@CalendarActivity,
                                        com.cl.common_base.R.mipmap.my_calendar_circle_two_bg
                                    )
                                )
                            }
                        }

                    }



                    logI(
                        """
                        task:
                        ${listContent[position].taskTime}
                        ${listContent[position].taskName}
                    """.trimIndent()
                    )
                    val tvTaskTime = holder.leftLayout.findViewById<TextView>(R.id.tv_task_time)
                    tvTaskTime.text = listContent[position].taskTime?.toLong()
                        ?.let { DateHelper.formatTime(it, "hh:mm a", Locale.getDefault()) }

                    val tvTaskName = holder.rightLayout.findViewById<TextView>(R.id.tv_task_name)
                    // 按钮
                    val svtWaitUnlock =
                        holder.rightLayout.findViewById<AbTextViewCalendar>(R.id.svt_wait_unlock)
                    val svtUnlock =
                        holder.rightLayout.findViewById<SvTextView>(R.id.svt_unlock)
                    val svtGrayUnlock =
                        holder.rightLayout.findViewById<SvTextView>(R.id.svt_gray_unlock)
                    when (listContent[position].taskStatus) {
                        // (1-已完成、0-未完成可操作、2-未完成不可操作)
                        "1" -> {
                            ViewUtils.setGone(svtUnlock)
                            ViewUtils.setGone(svtGrayUnlock)
                            ViewUtils.setVisible(svtWaitUnlock)
                            svtWaitUnlock.text = "Done"
                        }
                        "0" -> {
                            ViewUtils.setVisible(svtUnlock)
                            ViewUtils.setGone(svtWaitUnlock)
                            ViewUtils.setGone(svtGrayUnlock)
                            svtUnlock.text = "GO"
                        }
                        "2" -> {
                            ViewUtils.setGone(svtWaitUnlock)
                            ViewUtils.setVisible(svtGrayUnlock)
                            ViewUtils.setGone(svtUnlock)
                            svtGrayUnlock.text = "Go"
                        }
                    }
                    tvTaskName.text = listContent[position].taskName

                    svtUnlock.setOnClickListener {
                        when (listContent[position].taskStatus) {
                            "0" -> {
                                //  三行弹窗
                                pop.asCustom(
                                    BaseThreeTextPop(
                                        this@CalendarActivity,
                                        content = getString(
                                            com.cl.common_base.R.string.my_to_do,
                                            listContent[position].taskName
                                        ),
                                        oneLineText = getString(com.cl.common_base.R.string.my_go),
                                        twoLineText = getString(com.cl.common_base.R.string.my_remind_me),
                                        threeLineText = getString(com.cl.common_base.R.string.my_cancel),
                                        oneLineCLickEventAction = {
                                            // 记录taskId
                                            listContent[position].taskId?.let { taskId ->
                                                mViewMode.setTaskId(
                                                    taskId
                                                )
                                            }
                                            // todo 解锁周期图文引导弹窗
                                            when (listContent[position].taskType) {
                                                CalendarData.TASK_TYPE_CHANGE_WATER -> {
                                                    // todo 三合一流程、加水换水加肥
                                                    // 换水、加水、加肥。三步
                                                    changWaterAddWaterAddpump()
                                                }
                                                CalendarData.TASK_TYPE_CHANGE_CUP_WATER -> {
                                                    // todo 图文
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_CHANGE_CUP_WATER)
                                                }
                                                CalendarData.TASK_TYPE_LST -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_LST)
                                                }
                                                CalendarData.TASK_TYPE_TOPPING -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_TOPPING)
                                                }
                                                CalendarData.TASK_TYPE_TRIM -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_TRIM)
                                                }
                                                CalendarData.TASK_TYPE_CHECK_TRANSPLANT -> {
                                                    // 发芽转 veg
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_CHECK_TRANSPLANT)
                                                }
                                                CalendarData.TASK_TYPE_CHECK_CHECK_FLOWERING -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_CHECK_CHECK_FLOWERING)
                                                }
                                                CalendarData.TASK_TYPE_CHECK_CHECK_FLUSHING -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_CHECK_CHECK_FLUSHING)
                                                }
                                                CalendarData.TASK_TYPE_CHECK_CHECK_DRYING -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_CHECK_CHECK_DRYING)
                                                }
                                                CalendarData.TASK_TYPE_CHECK_CHECK_CURING -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_CHECK_CHECK_CURING)
                                                }
                                                CalendarData.TASK_TYPE_CHECK_CHECK_FINISH -> {
                                                    mViewMode.getGuideInfo(CalendarData.TASK_TYPE_CHECK_CHECK_FINISH)
                                                }
                                            }
                                        },
                                        twoLineCLickEventAction = {
                                            // remind me
                                            // 需要授权日历权限弹窗
                                            pop.asCustom(BaseCenterPop(
                                                this@CalendarActivity,
                                                content = getString(com.cl.common_base.R.string.my_calendar_permisson),
                                                confirmText = getString(com.cl.common_base.R.string.my_confirm),
                                                onConfirmAction = {
                                                    // 授权日历弹窗
                                                    PermissionHelp().applyPermissionHelp(
                                                        this@CalendarActivity,
                                                        getString(com.cl.common_base.R.string.my_calendar_permisson),
                                                        object :
                                                            PermissionHelp.OnCheckResultListener {
                                                            override fun onResult(result: Boolean) {
                                                                if (!result) return
                                                                // 跳转选择事件弹窗
                                                                pop.asCustom(
                                                                    BaseTimeChoosePop(
                                                                        this@CalendarActivity,
                                                                        currentTime = listContent[position].taskTime?.toLong(),
                                                                        onConfirmAction = { time, timeMis ->
                                                                            // 时间
                                                                            // 传给后台 & 上报给手机本地日历
                                                                            // todo 传给后台
                                                                            // todo 上报给手机本地日历
                                                                            CalendarEventUtil.addCalendarEvent(
                                                                                this@CalendarActivity,
                                                                                listContent[position].taskName,
                                                                                listContent[position].taskName,
                                                                                timeMis, 2
                                                                            )
                                                                        })
                                                                ).show()
                                                            }
                                                        },
                                                        Manifest.permission.READ_CALENDAR,
                                                        Manifest.permission.WRITE_CALENDAR,
                                                    )
                                                }
                                            )).show()

                                        },
                                        threeLineCLickEventAction = {
                                            // 暂时没啥用
                                        },
                                    )
                                ).show()
                            }
                        }
                    }

                }

                override fun createCustomView(
                    leftLayout: ViewGroup,
                    rightLayout: ViewGroup,
                    holder: TimeLineStepAdapter.CustomViewHolder
                ) {
                    LayoutInflater.from(this@CalendarActivity)
                        .inflate(R.layout.my_item_custom, rightLayout, true)

                    LayoutInflater.from(this@CalendarActivity)
                        .inflate(R.layout.my_item_lef_custom, leftLayout, true)
                }

            }).setLayoutType(LayoutType.ALL)
            .setIsCustom(true)
    }

    // 三合一流程
    private fun changWaterAddWaterAddpump() {
        // 记录taskId
        // 首先是换水
        pop
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .asCustom(
                HomePlantDrainPop(
                    context = this@CalendarActivity,
                    onNextAction = {
                        // 请求接口
                        mViewMode.advertising()
                    },
                    onCancelAction = {

                    },
                    onTvSkipAddWaterAction = {
                        // todo 跳过换水上报接口有问题、后台暂时未给
                        pop
                            .isDestroyOnDismiss(false)
                            .enableDrag(false)
                            .maxHeight(dp2px(600f))
                            .dismissOnTouchOutside(false)
                            .asCustom(
                                HomeSkipWaterPop(this@CalendarActivity, onConfirmAction = {
                                    mViewMode.taskId.value?.let {
                                        // 跳过换水
                                        mViewMode.deviceOperateStart(
                                            it,
                                            UnReadConstants.StatusManager.VALUE_STATUS_SKIP_CHANGING_WATERE
                                        )
                                    }
                                })
                            ).show()
                    }
                ).setData(true)
            ).show()
    }

    /**
     * 设备下发指令
     */
    override fun onTuYaToAppDataChange(status: String) {
        val map = GSON.parseObject(status, Map::class.java)
        map?.forEach { (key, value) ->
            when (key) {
                // 当用户加了水，是需要动态显示当前水的状态的
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_STATUS_INSTRUCTIONS -> {
                    logI("KEY_DEVICE_WATER_STATUS： $value")
                    mViewMode.setWaterVolume(value.toString())
                }

                // 排水结束
                TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                }

                // SN修复的通知
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_SN_INSTRUCTION -> {
                    logI("KEY_DEVICE_REPAIR_SN： $value")
                }
            }
        }
    }
}


