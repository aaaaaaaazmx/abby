package com.cl.modules_home.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cl.common_base.widget.SvTextView
import com.cl.common_base.bean.PlantInfoData
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomePeriodPopBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToLong
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.modules_home.activity.ProModeStartActivity
import com.cl.modules_home.ui.PeriodActivity
import com.google.api.Distribution.BucketOptions.Linear
import com.joketng.timelinestepview.LayoutType
import com.joketng.timelinestepview.OrientationShowType
import com.joketng.timelinestepview.TimeLineState
import com.joketng.timelinestepview.adapter.TimeLineStepAdapter
import com.joketng.timelinestepview.view.TimeLineStepView
import com.lxj.xpopup.core.BottomPopupView
import java.util.Date

/**
 * This is a short description.
 *
 * @author 李志军 2022-08-11 14:46
 */
class HomePeriodPop(
    context: Context,
    private val isManual: Boolean? = null,
    private var templateId: String? = null,
    private var harvestTime: String? = null,
    private var data: MutableList<PlantInfoData.InfoList>? = null,
    val unLockAction: ((guideType: String?, taskId: String?, lastOneType: String?, taskTime: String?) -> Unit)? = null,
    val unLockNow: ((pop: HomePeriodPop, step: String) -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_period_pop
    }

    fun setData(data: MutableList<PlantInfoData.InfoList>, harvestTime: String? = null, templateId: String?) {
        this.data = data
        this.harvestTime = harvestTime
        this.templateId = templateId
        for (i: Int in data.indices) {
            val info = data[i]
            when ("${info.journeyStatus}") {
                // 旅行状态(0-待解锁、1-解锁完成、2-进行中、3-允许解锁、4-种植待完成)
                KEY_ON_GOING -> {
                    // 进行中
                    info.timeLineState = TimeLineState.CURRENT
                }

                KEY_LOCK_COMPLETED -> {
                    // 3
                    info.timeLineState = TimeLineState.INACTIVE
                }

                KEY_ALLOW_UNLOCKING -> {
                    // 4
                    info.timeLineState = TimeLineState.INACTIVE
                }

                KEY_WAIT -> {
                    // 0
                    info.timeLineState = TimeLineState.INACTIVE
                }

                KEY_UNLOCKING_COMPLETED -> {
                    // 1
                    info.timeLineState = TimeLineState.ACTIVE
                }

                else -> {
                    // 0
                    info.timeLineState = TimeLineState.INACTIVE
                }
            }
        }
    }

    var binding: HomePeriodPopBinding? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<HomePeriodPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            showView(LayoutType.RIGHT)
            ViewUtils.setVisible(!harvestTime.isNullOrEmpty(), binding?.llHarvest)
            binding?.tvEta?.text = "${context.getString(com.cl.common_base.R.string.home_eta)} ${getYmdForEn(time = harvestTime.safeToLong() * 1000L)}"


            ivChart.setOnClickListener {
                // 跳转到图表洁面
                context.startActivity(Intent(context, PeriodActivity::class.java))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun beforeShow() {
        super.beforeShow()
        showView(LayoutType.RIGHT)
        ViewUtils.setVisible(!harvestTime.isNullOrEmpty(), binding?.llHarvest)
        // ETA Jun （月份缩写） 20(日期）,2025 (年）
        binding?.tvEta?.text = "${context.getString(com.cl.common_base.R.string.home_eta)} ${getYmdForEn(time = harvestTime.safeToLong() * 1000L)}"
    }

    /**
     * 获取当前年月日-- 后面跟着英文的th
     * 如 9 12th 2022
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
            return "$mm $dd, $yyyy"
        }
        return ""
    }

    private fun showView(type: LayoutType) {
        data?.let {
            binding?.timeView?.initData(
                it,
                OrientationShowType.TIMELINE,
                object : TimeLineStepView.OnInitDataCallBack {
                    // 自定义的View
                    override fun createCustomView(
                        leftLayout: ViewGroup,
                        rightLayout: ViewGroup,
                        holder: TimeLineStepAdapter.CustomViewHolder
                    ) {
                        LayoutInflater.from(context)
                            .inflate(R.layout.home_item_period_view, rightLayout, true)
                    }

                    @SuppressLint("ResourceAsColor")
                    override fun onBindDataViewHolder(
                        holder: TimeLineStepAdapter.CustomViewHolder,
                        position: Int
                    ) {
                        val periodTitle = holder.itemView.findViewById<TextView>(R.id.period_title)
                        val periodTime = holder.itemView.findViewById<TextView>(R.id.period_time)
                        val svtWaitUnlock =
                            holder.itemView.findViewById<SvTextView>(R.id.svt_wait_unlock)
                        val clRoot = holder.itemView.findViewById<ConstraintLayout>(R.id.cl_root)
                        val ivGou = holder.itemView.findViewById<ImageView>(R.id.iv_gou)
                        val svtUnlock = holder.itemView.findViewById<SvTextView>(R.id.svt_unlock)
                        val tvGoing = holder.itemView.findViewById<TextView>(R.id.tv_going)
                        val isLock = holder.itemView.findViewById<FrameLayout>(R.id.fl_root)
                        val svtProEdit = holder.itemView.findViewById<SvTextView>(R.id.svt_pro_edit)

                        tvGoing.visibility = View.GONE
                        svtUnlock.visibility = View.GONE
                        ivGou.visibility = View.GONE
                        svtWaitUnlock.visibility = View.GONE
                        periodTime.visibility = if (data?.get(position)?.etaTime.safeToLong() != 0L) View.VISIBLE else View.GONE
                        svtProEdit.visibility = View.GONE
                        if (data?.isEmpty() == true) return
                        ViewUtils.setInvisible(isLock, data?.get(position)?.unlockNow == false || data?.get(position)?.unlockNow == null)

                        // 更改当前周期时间 proMode下专属
                        svtProEdit.setSafeOnClickListener {
                            // 跳转到周期选择界面
                            // 回来后还得刷新一下PlantInfo更新当前周期时间以及顺序。
                            context.startActivity(Intent(context, ProModeStartActivity::class.java).apply {
                                putExtra(ProModeStartActivity.STEP, this@HomePeriodPop.data?.get(position)?.step)
                                putExtra(ProModeStartActivity.TEMPLATE_ID , templateId)
                                putExtra(ProModeStartActivity.IS_CURRENT_PERIOD, "${this@HomePeriodPop.data?.get(position)?.journeyStatus.toString() == KEY_ON_GOING}")
                            })
                            dismiss()
                        }

                        // 提前解锁
                        isLock.setOnClickListener {
                            unLockNow?.invoke(this@HomePeriodPop,this@HomePeriodPop.data?.get(position)?.step.toString())
                        }

                        // 解锁
                        svtUnlock.setOnClickListener {
                            logI(
                                """
                                guideType: ${data?.get(position)?.guideType.toString()}
                                taskId: ${data?.get(position)?.taskId.toString()}
                            """.trimIndent()
                            )
                            if ((data?.size ?: 0) > 0) {
                                unLockAction?.invoke(
                                    data?.get(position)?.guideType.toString(),
                                    data?.get(position)?.taskId.toString(),
                                    if (position != 0) data?.get(position - 1)?.guideType.toString() else data?.get(
                                        0
                                    )?.guideType.toString(),
                                    data?.get(position)?.taskTime.toString()
                                )
                            }
                            dismiss()
                        }


                        // 赋值
                        periodTitle.text =
                            data?.get(position)?.journeyName
                        // 未解锁时不显示周期
                        val journeyStatus =  "${data?.get(position)?.journeyStatus}"
                        periodTime.text = if (journeyStatus == KEY_ON_GOING || journeyStatus == KEY_LOCK_COMPLETED || journeyStatus == KEY_UNLOCKING_COMPLETED) {
                            "${context.getString(com.cl.common_base.R.string.week)}${data?.get(position)?.week} ${context.getString(com.cl.common_base.R.string.day)}${data?.get(position)?.day}"
                        } else {
                            "${context.getString(com.cl.common_base.R.string.home_eta)} ${getYmdForEn(time = data?.get(position)?.etaTime.safeToLong() * 1000L)}"
                        }

                        kotlin.runCatching {
                            when (journeyStatus) {
                                KEY_WAIT -> {
                                    if (isManual == true) {
                                        svtProEdit.visibility = View.VISIBLE
                                    } else {
                                        svtWaitUnlock.text = context?.getString(com.cl.common_base.R.string.string_284)
                                        svtWaitUnlock.visibility = View.VISIBLE
                                    }

                                    // 待解锁状态下，不显示时间周期

                                    clRoot.background = ContextCompat.getDrawable(
                                        context,
                                        R.mipmap.home_period_green
                                    )

                                    periodTitle.setTextColor(
                                        Color.BLACK
                                    )
                                    periodTime.setTextColor(
                                        Color.BLACK
                                    )
                                }

                                KEY_UNLOCKING_COMPLETED -> {
                                    ivGou.visibility = View.VISIBLE
                                    periodTime.visibility = View.VISIBLE

                                    clRoot.background = ContextCompat.getDrawable(
                                        context,
                                        com.cl.common_base.R.drawable.background_main_color_r6
                                    )

                                    periodTitle.setTextColor(
                                        Color.WHITE
                                    )
                                    periodTime.setTextColor(
                                        Color.WHITE
                                    )
                                }

                                KEY_ON_GOING -> {
                                    tvGoing.visibility = View.VISIBLE
                                    periodTime.visibility = View.VISIBLE
                                    // 进行中
                                    clRoot.background = ContextCompat.getDrawable(
                                        context,
                                        com.cl.common_base.R.drawable.background_cyan_r6
                                    )
                                    periodTitle.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            com.cl.common_base.R.color.mainColor
                                        )
                                    )
                                    periodTime.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            com.cl.common_base.R.color.mainColor
                                        )
                                    )
                                }

                                KEY_ALLOW_UNLOCKING -> {
                                    // 区分专业模式和普通模式下的文案。
                                    if (isManual == true) {
                                        svtProEdit.visibility = View.VISIBLE
                                    } else {
                                        svtUnlock.text = context?.getString(com.cl.common_base.R.string.string_284)
                                        svtUnlock.visibility = View.VISIBLE
                                    }

                                    clRoot.background = ContextCompat.getDrawable(
                                        context,
                                        R.mipmap.home_period_green
                                    )

                                    periodTitle.setTextColor(
                                        Color.BLACK
                                    )
                                    periodTime.setTextColor(
                                        Color.BLACK
                                    )
                                }

                                KEY_LOCK_COMPLETED -> {
                                    if (isManual == true) {
                                        svtProEdit.visibility = View.VISIBLE
                                    } else {
                                        svtUnlock.text = context?.getString(com.cl.common_base.R.string.string_284)
                                        svtUnlock.visibility = View.VISIBLE
                                    }

                                    clRoot.background = ContextCompat.getDrawable(
                                        context,
                                        R.mipmap.home_period_green
                                    )

                                    periodTitle.setTextColor(
                                        Color.BLACK
                                    )
                                    periodTime.setTextColor(
                                        Color.BLACK
                                    )
                                }

                                else -> {
                                    if (isManual == true) {
                                        svtProEdit.visibility = View.VISIBLE
                                    } else {
                                        svtWaitUnlock.text = context?.getString(com.cl.common_base.R.string.string_284)
                                        svtWaitUnlock.visibility = View.VISIBLE
                                    }

                                    // 待解锁状态下，不显示时间周期

                                    clRoot.background = ContextCompat.getDrawable(
                                        context,
                                        R.mipmap.home_period_green
                                    )

                                    periodTitle.setTextColor(
                                        Color.BLACK
                                    )
                                    periodTime.setTextColor(
                                        Color.BLACK
                                    )
                                }
                            }
                        }

                    }
                })?.setLayoutType(type)
                ?.setIsCustom(true)
        }
    }

    companion object {
        /**
         * journeyStatus	旅行状态(0-待解锁、1-解锁完成、2-进行中、3-允许解锁、4-种植待完成)
         */
        const val KEY_WAIT = "0"
        const val KEY_UNLOCKING_COMPLETED = "1"
        const val KEY_ON_GOING = "2"
        const val KEY_ALLOW_UNLOCKING = "3"
        const val KEY_LOCK_COMPLETED = "4"


        const val KEY_SEED = "Seed"
    }
}