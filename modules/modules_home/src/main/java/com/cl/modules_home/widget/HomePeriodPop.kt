package com.cl.modules_home.widget

import android.content.Context
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
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePeriodPopBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.util.ViewUtils
import com.google.api.Distribution.BucketOptions.Linear
import com.joketng.timelinestepview.LayoutType
import com.joketng.timelinestepview.OrientationShowType
import com.joketng.timelinestepview.TimeLineState
import com.joketng.timelinestepview.adapter.TimeLineStepAdapter
import com.joketng.timelinestepview.view.TimeLineStepView
import com.lxj.xpopup.core.BottomPopupView

/**
 * This is a short description.
 *
 * @author 李志军 2022-08-11 14:46
 */
class HomePeriodPop(
    context: Context,
    private var data: MutableList<PlantInfoData.InfoList>? = null,
    val unLockAction: ((guideType: String?, taskId: String?, lastOneType: String?, taskTime: String?) -> Unit)? = null,
    val unLockNow: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_period_pop
    }

    fun setData(data: MutableList<PlantInfoData.InfoList>) {
        this.data = data
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
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<HomePeriodPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            showView(LayoutType.RIGHT)
        }
    }

    override fun beforeShow() {
        super.beforeShow()
        showView(LayoutType.RIGHT)
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

                        tvGoing.visibility = View.GONE
                        svtUnlock.visibility = View.GONE
                        ivGou.visibility = View.GONE
                        svtWaitUnlock.visibility = View.GONE
                        periodTime.visibility = View.GONE
                        if (data?.isEmpty() == true) return
                        ViewUtils.setInvisible(isLock, data?.get(position)?.unlockNow == false)

                        // 提前解锁
                        isLock.setOnClickListener {
                            unLockNow?.invoke()
                            dismiss()
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
                        periodTime.text =
                            "Week${data?.get(position)?.week} Day${data?.get(position)?.day}"

                        kotlin.runCatching {
                            when ("${data?.get(position)?.journeyStatus}") {
                                KEY_WAIT -> {
                                    svtWaitUnlock.text = "Unlock"
                                    svtWaitUnlock.visibility = View.VISIBLE
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
                                    tvGoing.visibility =
                                        View.VISIBLE
                                }

                                KEY_ALLOW_UNLOCKING -> {
                                    svtUnlock.text = "Unlock"
                                    svtUnlock.visibility = View.VISIBLE

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
                                    svtUnlock.text = "Unlock"
                                    svtUnlock.visibility = View.VISIBLE

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
                                    svtWaitUnlock.text = "Unlock"
                                    svtWaitUnlock.visibility = View.VISIBLE
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