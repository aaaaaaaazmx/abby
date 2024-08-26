package com.cl.common_base.pop

import android.content.Context
import android.os.PowerManager
import android.util.Log
import android.view.animation.AlphaAnimation
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.cl.common_base.BaseApplication
import com.cl.common_base.R
import com.cl.common_base.adapter.PumpWaterAdapter
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BasePumpWaterPopBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.widget.toast.ToastUtil
import com.lin.cardlib.CardLayoutManager
import com.lin.cardlib.CardSetting
import com.lin.cardlib.CardTouchHelperCallback
import com.lin.cardlib.OnSwipeCardListener
import com.lin.cardlib.utils.ReItemTouchHelper
import com.lxj.xpopup.core.BottomPopupView
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/**
 * 排水弹窗
 *
 * @author 李志军 2022-08-10 15:06
 */
class BasePumpWaterPop(
    context: Context,
    private val onSuccessAction: ((status: Boolean) -> Unit)? = null,
    private var data: MutableList<AdvertisingData>? = null,
    private var onWaterFinishedAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : BottomPopupView(context) {
    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    private var count = 1

    override fun getImplLayoutId(): Int {
        return R.layout.base_pump_water_pop
    }

    private val adapter by lazy {
        PumpWaterAdapter(mutableListOf())
    }

    fun setData(data: MutableList<AdvertisingData>) {
        this.data = data
    }

    override fun beforeShow() {
        super.beforeShow()
        kotlin.runCatching {
            mWakeLock?.acquire()
            if (data?.size != 0) {
                // 卡片布局需要展示3张，所以需要多添加几张
                if ((data?.size ?: 0) == 1) {
                    data?.get(0)?.let { data?.add(it) }
                }
                if ((data?.size ?: 0) == 2) {
                    data?.get(0)?.let { data?.add(it) }
                    data?.get(1)?.let { data?.add(it) }
                }
                adapter.setList(data)
            }

            // 因为是通过by lazy 添加的， 所以状态需要重置
            isFirst = true
            binding?.btnSuccess?.isChecked = true
            binding?.btnSuccess?.let {
                it.background = context.resources.getDrawable(
                    R.mipmap.base_start_bg,
                    context.theme
                )
            }
            binding?.tvAddClockTime?.text = context.getString(R.string.string_201)
        }
    }

    override fun onDismiss() {
        // 涂鸦指令，添加排水功能
        isOpenOrStop(false)
        job?.cancel()
        animation.cancel()
        count = 0
        timing = 140 * 3
        ViewUtils.setGone(binding?.ivWaterOne, binding?.ivWaterOne, binding?.ivWaterThree)
        mWakeLock?.release()
        GSYVideoManager.releaseAllVideos()
        super.onDismiss()
    }

    private val animation by lazy {
        val animation = AlphaAnimation(
            0f, 1f
        )
        animation.duration = 1000 //执行时间
        animation.repeatCount = -1 //重复执行动画
        animation
    }

    private val mWakeLock by lazy {
        val systemService = BaseApplication.getContext().getSystemService(Context.POWER_SERVICE) as? PowerManager
        val mWakeLock: PowerManager.WakeLock? = systemService?.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag")
        mWakeLock
    }

    // 记时器、标准时间为140秒
    private var timing: Int = 140 * 3
    var binding: BasePumpWaterPopBinding? = null
    var isFirst = true
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<BasePumpWaterPopBinding>(popupImplView)?.apply {
            tvAddClockTime.text = context.getString(R.string.string_201)
            btnSuccess.setOnClickListener {
                onSuccessAction?.invoke(if (isFirst) isFirst else btnSuccess.isChecked)
                isFirst = false
                // true 排水
                // false 停止
                synchronized(this) {
                    it.background = if (btnSuccess.isChecked) {
                        // 如果是第一次排水
                        showAlphaAniamtion()
                        context.resources.getDrawable(
                            R.mipmap.base_start_bg,
                            context.theme
                        )
                    } else {
                        ViewUtils.setGone(binding?.cbBg)
                        animation.cancel()
                        context.resources.getDrawable(R.mipmap.base_suspend_bg, context.theme)
                    }

                    tvAddClockTime.text =
                        if (btnSuccess.isChecked) "The program will pause every 2 minutes to prevent overflow. We recommend using a container of 1.5 gallons+ for water changes."
                        else context.getString(R.string.base_pump_stop_dec)
                }
            }
            ivClose.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
            }


            val setting = CardSetting()
            setting.setSwipeListener(object : OnSwipeCardListener<AdvertisingData?> {
                override fun onSwiping(
                    viewHolder: RecyclerView.ViewHolder,
                    dx: Float,
                    dy: Float,
                    direction: Int
                ) {
                    when (direction) {
                        ReItemTouchHelper.DOWN -> Log.e("aaa", "swiping direction=down")
                        ReItemTouchHelper.UP -> Log.e("aaa", "swiping direction=up")
                        ReItemTouchHelper.LEFT -> Log.e("aaa", "swiping direction=left")
                        ReItemTouchHelper.RIGHT -> Log.e("aaa", "swiping direction=right")
                    }
                }

                override fun onSwipedOut(
                    viewHolder: RecyclerView.ViewHolder,
                    o: AdvertisingData?,
                    direction: Int
                ) {
                    GSYVideoManager.releaseAllVideos()
                   /* when (direction) {
                        ReItemTouchHelper.DOWN -> Toast.makeText(
                            context,
                            "swipe down out",
                            Toast.LENGTH_SHORT
                        ).show()
                        ReItemTouchHelper.UP -> Toast.makeText(
                            context,
                            "swipe up out ",
                            Toast.LENGTH_SHORT
                        ).show()
                        ReItemTouchHelper.LEFT -> Toast.makeText(
                            context,
                            "swipe left out",
                            Toast.LENGTH_SHORT
                        ).show()
                        ReItemTouchHelper.RIGHT -> Toast.makeText(
                            context,
                            "swipe right out",
                            Toast.LENGTH_SHORT
                        ).show()
                    }*/
                }

                override fun onSwipedClear() {
                    //                    Toast.makeText(context, "cards are consumed", Toast.LENGTH_SHORT)
                    //                        .show()
                }
            })

            rvAdd.adapter = adapter
            val helperCallback: CardTouchHelperCallback<AdvertisingData> =
                CardTouchHelperCallback<AdvertisingData>(rvAdd, adapter.data, setting)
            val mReItemTouchHelper = ReItemTouchHelper(helperCallback)
            val layoutManager = CardLayoutManager(mReItemTouchHelper, setting)
            rvAdd.layoutManager = layoutManager

            // 手动滑动第一张
            handler.postDelayed(Runnable {
                mReItemTouchHelper.swipeManually(ReItemTouchHelper.RIGHT)
            }, 3000)
        }

        // 蓝牙状态监听变化
        LiveEventBus.get().with(Constants.Tuya.KEY_THING_DEVICE_TO_APP, String::class.java)
            .observe(this) {
                GSON.parseObjectInBackground(it, Map::class.java) {map ->
                    map?.forEach { (key, value) ->
                        when (key) {
                            TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                                // 涂鸦指令，添加排水功能
                                //                            isOpenOrStop(false)
                                // 排水成功
                                if ((value as? Boolean == false)) return@forEach
                                onWaterFinishedAction?.invoke()
                                dismiss()
                            }
                            //
                            TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_INSTRUCTIONS -> {
                                // 涂鸦指令，添加排水功能
                                //                            isOpenOrStop(value)
                                synchronized(this) {
                                    // 加锁的目的是为了
                                    // 当用户在点击时,又突然接收到设备的指令,导致显示不正确的问题
                                    // false 是暂停排水、 true是开始排水
                                    if (value == false) {
                                        job?.cancel()
                                        count++
                                    } else {
                                        // 计时器减去时间
                                        if (count >= 3) timing = 120
                                        downTime()
                                    }
                                    binding?.btnSuccess?.background =
                                        if ((value as? Boolean != true)) {
                                            ViewUtils.setGone(binding?.cbBg)
                                            animation.cancel()
                                            context.resources.getDrawable(
                                                R.mipmap.base_start_bg,
                                                context.theme
                                            )
                                        } else {
                                            showAlphaAniamtion()
                                            context.resources.getDrawable(
                                                R.mipmap.base_suspend_bg,
                                                context.theme
                                            )
                                        }
                                    if (binding?.btnSuccess?.isChecked == true) {
                                        // 暂停
                                        if (value == false) {
                                            binding?.tvAddClockTime?.text = context.getString(R.string.base_pump_auto_start_desc)
                                        } else {
                                            binding?.tvAddClockTime?.text = "The program will pause every 2 minutes to prevent overflow. We recommend using a container of 1.5 gallons+ for water changes."
                                        }
                                    } else {
                                        // 暂停
                                        if (value == false) {
                                            binding?.tvAddClockTime?.text = context.getString(R.string.base_pump_stop_dec)
                                        } else {
                                            binding?.tvAddClockTime?.text = "The program will pause every 2 minutes to prevent overflow. We recommend using a container of 1.5 gallons+ for water changes."
                                        }
                                    }
                                    binding?.btnSuccess?.isChecked = value as Boolean
                                }

                                if ((value as? Boolean == false)) return@forEach
                                // 查询是否排水结束
                                ThingHomeSdk.newDeviceInstance(userInfo?.deviceId)?.let {
                                    it.getDp(TuYaDeviceConstants.KAY_PUMP_WATER_FINISHED, object :
                                        IResultCallback {
                                        override fun onError(code: String?, error: String?) {
                                            logI(
                                                """
                                            KAY_PUMP_WATER_FINISHED: error
                                            code: $code
                                            error: $error
                                        """.trimIndent()
                                            )
                                            ToastUtil.shortShow(error)
                                            Reporter.reportTuYaError("newDeviceInstance", error, code)
                                        }

                                        override fun onSuccess() {
                                            logI("onSuccess")
                                        }
                                    })
                                }
                            }
                        }
                    }
                }

            }
    }

    private fun showAlphaAniamtion() {
        ViewUtils.setVisible(binding?.cbBg)
        when (count) {
            1 -> {
                animation.cancel()
                ViewUtils.setVisible(binding?.ivWaterOne)
                ViewUtils.setGone(binding?.ivWaterTwo, binding?.ivWaterThree)
                binding?.ivWaterOne?.animation = animation
                animation.start()
            }
            2 -> {
                animation.cancel()
                ViewUtils.setVisible(binding?.ivWaterOne, binding?.ivWaterTwo)
                ViewUtils.setGone(binding?.ivWaterThree)
                binding?.ivWaterOne?.animation = null
                binding?.ivWaterTwo?.animation = animation
                animation.start()
            }
            else -> {
                animation.cancel()
                ViewUtils.setVisible(binding?.ivWaterOne, binding?.ivWaterTwo, binding?.ivWaterThree)
                binding?.ivWaterOne?.animation = null
                binding?.ivWaterTwo?.animation = null
                binding?.ivWaterThree?.animation = animation
                animation.start()
            }
        }
    }

    private fun isOpenOrStop(value: Any?) {
        DeviceControl.get()
            .success {
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
            .pumpWater((value as? Boolean == true))
    }


    private var job: Job? = null
    private fun downTime() {
        // 开启定时器，每次20秒刷新未读气泡消息
        job = countDownCoroutines(
            10 * 6 * 500000,
            lifecycleScope,
            onTick = {
                // logI("$it,,,,$timing")
                timing -= 1
                // 等于0了，表示排水成功
                if (timing == 0) {
                    onWaterFinishedAction?.invoke()
                    job?.cancel()
                    dismiss()
                }
                if (it == 0) {
                    job?.cancel()
                }
            },
            onStart = {},
            onFinish = {
                job?.cancel()
            })
    }

    /**
     * 计时器
     */
    private fun countDownCoroutines(
        total: Int,
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onStart: (() -> Unit)? = null,
        onFinish: (() -> Unit)? = null,
    ): Job {
        return flow {
            for (i in total downTo 0) {
                emit(i)
                delay(1000)
            }
        }.flowOn(Dispatchers.Main)
            .onStart { onStart?.invoke() }
            .onCompletion { onFinish?.invoke() }
            .onEach { onTick.invoke(it) }
            .launchIn(scope)
    }

}