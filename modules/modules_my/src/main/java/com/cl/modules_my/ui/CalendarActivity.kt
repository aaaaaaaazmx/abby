package com.cl.modules_my.ui

import android.Manifest
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.height
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.width
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.BaseThreeTextPop
import com.cl.common_base.pop.BaseTimeChoosePop
import com.cl.common_base.util.calendar.CalendarEventUtil
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.MyCalendarAdapter
import com.cl.modules_my.databinding.MyCalendayActivityBinding
import com.cl.modules_my.repository.AllProgressBean
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.joketng.timelinestepview.LayoutType
import com.joketng.timelinestepview.OrientationShowType
import com.joketng.timelinestepview.TimeLineState
import com.joketng.timelinestepview.adapter.TimeLineStepAdapter
import com.joketng.timelinestepview.view.TimeLineStepView
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Field
import java.util.*


@Route(path = RouterPath.My.PAGE_MY_CALENDAR)
@AndroidEntryPoint
class CalendarActivity : BaseActivity<MyCalendayActivityBinding>() {
    private val adapter by lazy {
        MyCalendarAdapter(mutableListOf())
    }

    private val pop by lazy {
        XPopup.Builder(this@CalendarActivity)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
    }

    override fun initView() {
        binding.rvList.layoutManager = GridLayoutManager(this@CalendarActivity, 7)
        binding.rvList.adapter = adapter
        val mCurrentDate = com.cl.common_base.util.calendar.Calendar()
        val d = Date()
        mCurrentDate.year = CalendarUtil.getDate("yyyy", d)
        mCurrentDate.month = CalendarUtil.getDate("MM", d)
        mCurrentDate.day = CalendarUtil.getDate("dd", d)
        mCurrentDate.isCurrentDay = true
        val list: MutableList<com.cl.common_base.util.calendar.Calendar> = mutableListOf()
        for (i in 1..12) {
            list += CalendarUtil.initCalendarForMonthView(
                2022,
                i,
                mCurrentDate,
                Calendar.SUNDAY
            )
        }

        // 添加数据
        adapter.setList(
            list
        )
        // 滚到到当前日期到上一行
        binding.rvList.scrollToPosition(list.indexOf(mCurrentDate) - 7)
        adapter.addChildClickViewIds(R.id.tv_content_day)
        // help
        val snapHelper = GravitySnapHelper(Gravity.TOP)
        snapHelper.attachToRecyclerView(binding.rvList)

        // 设置标题颜色以及标题文案
        binding.title.setTitle(getString(com.cl.common_base.R.string.my_calendar))
            .setTitleColor(com.cl.common_base.R.color.mainColor)
            .setQuickClickListener {
                // 会滚到当前日期
                val data = adapter.data
                if (data.isEmpty()) return@setQuickClickListener
                binding.rvList.scrollToPosition(data.indexOfFirst { it.isCurrentDay })
            }
        // 初始化当月
        binding.abMonth.text = CalendarUtil.getMonthFromLocation(Date().time)
        binding.tvTodayDate.text = CalendarUtil.getYMDFromLocation(Date().time)

        // 设置滑动速度
        setMaxFlingVelocity(binding.rvList, 5000)

        initTime()
    }

    override fun observe() {
    }

    override fun initData() {
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val list = adapter.data as? MutableList<com.cl.common_base.util.calendar.Calendar>
            val data = adapter.data[position] as? com.cl.common_base.util.calendar.Calendar
            when (view.id) {
                R.id.tv_content_day -> {
                    if (list.isNullOrEmpty()) return@setOnItemChildClickListener
                    // 找到之前的，清除之前的isChoose、然后改为true

                    // 设置为true
                    // 判断点击的是否是今日
                    if (data?.isCurrentDay == true) {
                    } else {
                        list.indexOfFirst { it.isChooser }.let {
                            if (it != -1) {
                                list[it].isChooser = false
                                adapter.notifyItemChanged(it)
                            }
                        }
                        data?.isChooser = true
                        view.background = ContextCompat.getDrawable(
                            this@CalendarActivity,
                            com.cl.common_base.R.drawable.base_dot_main_color
                        )
                        // 设置选中文字效果
                        view.findViewById<TextView>(R.id.text_date_day).setTextColor(Color.WHITE)
                        // 设置选中动效
                        //缩小
                        val animation = ScaleAnimation(
                            1.0f, 0.5f, 1.0f, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                        )
                        animation.duration = 200 //执行时间
                        animation.repeatCount = 1 //重复执行动画
                        animation.repeatMode = Animation.REVERSE //重复 缩小和放大效果
                        view.startAnimation(animation) //使用View启动动画

                        // todo 时间转换，并且需要请求接口
                        data?.timeInMillis?.let {
                            binding.tvTodayDate.text = CalendarUtil.getYMDFromLocation(it)
                        }
                    }
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
                        val thirdLineFirst =
                            layoutManager.findFirstCompletelyVisibleItemPosition() + 17
                        if (adapter.data.isEmpty()) return
                        // 格式化时间戳
                        binding.abMonth.text =
                            CalendarUtil.getMonthFromLocation(adapter.data[thirdLineFirst].timeInMillis)
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

                        // 手指按下去的操作
                        if (adapter.data.isEmpty()) return
                        val firstPosition = layoutManager.findFirstVisibleItemPosition()
                        val data = adapter.data[firstPosition]
                        val d = Date()
                        val mCurrentDate = com.cl.common_base.util.calendar.Calendar()
                        mCurrentDate.year = CalendarUtil.getDate("yyyy", d)
                        mCurrentDate.month = CalendarUtil.getDate("MM", d)
                        mCurrentDate.day = CalendarUtil.getDate("dd", d)
                        mCurrentDate.isCurrentDay = true
                        val list: MutableList<com.cl.common_base.util.calendar.Calendar> =
                            mutableListOf()
                        // 如果是小于等于6，那么上一年
                        if (data.month <= 4) {
                            for (i in 1..12) {
                                list += CalendarUtil.initCalendarForMonthView(
                                    data.year - 1,
                                    i,
                                    mCurrentDate,
                                    Calendar.SUNDAY
                                )
                            }
                            adapter.addData(0, list)
                            list.clear()
                        }
                        if (data.month >= 8) {
                            // 加载下一年
                            for (i in 1..12) {
                                list += CalendarUtil.initCalendarForMonthView(
                                    data.year + 1,
                                    i,
                                    mCurrentDate,
                                    Calendar.SUNDAY
                                )
                            }
                            adapter.addData(list)
                            list.clear()
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
            }
        })


        // 点击事件
        clickEvent()
    }

    private fun clickEvent() {
        binding.rlCycle.setOnClickListener {
            //  三行弹窗
            pop.asCustom(
                BaseThreeTextPop(
                    this@CalendarActivity,
                    content = getString(com.cl.common_base.R.string.my_to_do),
                    oneLineText = getString(com.cl.common_base.R.string.my_go),
                    twoLineText = getString(com.cl.common_base.R.string.my_remind_me),
                    threeLineText = getString(com.cl.common_base.R.string.my_cancel),
                    oneLineCLickEventAction = {
                        // todo 解锁周期图文引导弹窗
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
                                    object : PermissionHelp.OnCheckResultListener {
                                        override fun onResult(result: Boolean) {
                                            if (!result) return
                                            // 跳转选择事件弹窗
                                            pop.asCustom(
                                                BaseTimeChoosePop(
                                                    this@CalendarActivity,
                                                    onConfirmAction = {time, timeMis ->
                                                        // 时间
                                                        // 传给后台 & 上报给手机本地日历
                                                        // todo 传给后台
                                                        // todo 上报给手机本地日历
                                                        CalendarEventUtil.addCalendarEvent(this@CalendarActivity,
                                                            "吃饭睡觉，打豆豆",
                                                            "打豆豆",
                                                            timeMis,2
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
    private val listContent = mutableListOf<AllProgressBean>()
    fun initTime() {
        repeat(7) {
            val bean = AllProgressBean("item$it")

            if (it == 3) {
//                bean.rightTitle = "说的就是看到就隆盛科技发我我积分为弗兰克健康检查绿茶女名称v女村民们处女吗没VM从VM你十多分十分时分我去问而我认为"
                bean.timeLineState = TimeLineState.CURRENT
            }
            if (it > 3) {
//                bean.rightTitle = "所得到的多多多多多item$it"
                bean.timeLineState = TimeLineState.INACTIVE
            }
            listContent.add(bean)
        }
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
                        //                layoutParams.setMargins(dp2px(5f),dp2px(5f),dp2px(5f),dp2px(5f))
                        //                val drawable = RoundedBitmapDrawableFactory.create(resources,
                        //                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                        //                drawable.isCircular = true
                        if (position == 0) holder.imgMark.setImageDrawable(null) else holder.imgMark.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@CalendarActivity,
                                R.mipmap.my_iv_red_circle
                            )
                        )
                        //                        holder.itemView.width(LinearLayout.LayoutParams.WRAP_CONTENT)
                        //                        holder.itemView.height(LinearLayout.LayoutParams.WRAP_CONTENT)
                        //                        holder.llLine.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                        return
                    }
                    val layoutParams = holder.imgMark.layoutParams as LinearLayout.LayoutParams
                    //                    layoutParams.width = dp2px(10f)
                    //                    layoutParams.height = dp2px(10f)
                    holder.llLine.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                    holder.imgLineEnd.layoutParams.width = dp2px(0.8f)
                    holder.imgLineStart.layoutParams.width = dp2px(0.8f)
                    //                layoutParams.setMargins(dp2px(5f),dp2px(5f),dp2px(5f),dp2px(5f))
                    //                val drawable = RoundedBitmapDrawableFactory.create(resources,
                    //                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    //                drawable.isCircular = true
                    if (position == 0) holder.imgMark.setImageDrawable(null) else holder.imgMark.setImageDrawable(
                        ContextCompat.getDrawable(this@CalendarActivity, R.drawable.my_head_bg)
                    )
                    //                holder.imgMark.setPadding(dp2px(5f),dp2px(5f),dp2px(5f),dp2px(5f))
                    holder.imgMark.scaleType = ImageView.ScaleType.CENTER_CROP
                    //                holder.imgMark.background = ContextCompat.getDrawable(this@CalendarActivity, R.mipmap.my_close)


                    // 加载右边布局
                    //                val drawable2 = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.mipmap.my_about_us))
                    //                drawable2.cornerRadius = dp2px(6f).toFloat()
                    //                holder.itemView.img_one.setImageDrawable(drawable2)
                    //                holder.itemView.img_two.setImageDrawable(drawable2)
                    //                holder.itemView.img_three.setImageDrawable(drawable2)
                    //                if(position % 3 == 0){
                    //                    holder.itemView.img_two.visibility = View.VISIBLE
                    //                } else {
                    //                    holder.itemView.img_two.visibility = View.GONE
                    //                }
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
}