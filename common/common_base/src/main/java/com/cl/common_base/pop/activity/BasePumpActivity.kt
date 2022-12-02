package com.cl.common_base.pop.activity

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.cl.common_base.BaseApplication
import com.cl.common_base.R
import com.cl.common_base.adapter.PumpWaterAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.bean.UnreadMessageData
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.databinding.BasePopPumpActivityBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.widget.toast.ToastUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lin.cardlib.CardLayoutManager
import com.lin.cardlib.CardSetting
import com.lin.cardlib.CardTouchHelperCallback
import com.lin.cardlib.OnSwipeCardListener
import com.lin.cardlib.utils.ReItemTouchHelper
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 排水弹窗
 */
@AndroidEntryPoint
class BasePumpActivity : BaseActivity<BasePopPumpActivityBinding>() {
    @Inject
    lateinit var mViewMode: BasePumpViewModel


    private val callback by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.vvDivider.alpha = slideOffset
            }
        }
    }

    /**
     * 图文数据
     */
    private val data by lazy {
        intent?.extras?.getSerializable(KEY_DATA) as? MutableList<AdvertisingData>
    }

    /**
     * 未读消息列表数据
     */
    private val unreadMessageData by lazy {
        intent.getSerializableExtra(KEY_UNREAD_MESSAGE_DATA) as? MutableList<UnreadMessageData>
    }

    /**
     * 获取当前设备信息
     */
    private val tuYaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }


    /**
     * 排水界面视图适配器
     */
    private val adapter by lazy {
        PumpWaterAdapter(mutableListOf())
    }

    /**
     * 常亮
     */
    private val mWakeLock by lazy {
        val systemService = BaseApplication.getContext().getSystemService(POWER_SERVICE) as? PowerManager
        val mWakeLock: PowerManager.WakeLock? = systemService?.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag")
        mWakeLock
    }

    // 排水的次数
    private var count = 1

    // 记时器、标准时间为140秒
    private var timing: Int = 140 * 3

    /**
     * 排水动画
     */
    private val animation by lazy {
        val animation = AlphaAnimation(
            0f, 1f
        )
        animation.duration = 1000 //执行时间
        animation.repeatCount = -1 //重复执行动画
        animation
    }

    override fun initView() {
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.cl) { v, insets ->
            binding.ll.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop

                val from = BottomSheetBehavior.from(binding.ll)
                from.skipCollapsed = true
                from.state = BottomSheetBehavior.STATE_EXPANDED
                from.addBottomSheetCallback(callback)
            }
            return@setOnApplyWindowInsetsListener insets
        }
        val asa = intent?.extras?.getSerializable(KEY_DATA)
        val ss = intent?.getSerializableExtra(KEY_DATA)
        logI("${(ss as? MutableList<AdvertisingData>)?.get(0)?.id}")
        initPumpWater()
        initClick()
    }

    /**
     * 排水界面的点击i时间
     */
    private fun initClick() {
        binding.waterPop.apply {
            ivClose.setOnClickListener {
                finish()
            }

            btnSuccess.setOnClickListener {
                isOpenOrStop(btnSuccess.isChecked)
                // isFirst = false
                // true 排水
                // false 停止
                synchronized(this) {
                    it.background = if (btnSuccess.isChecked) {
                        // 如果是第一次排水
                        showAlphaAnimation()
                        resources.getDrawable(
                            R.mipmap.base_start_bg,
                            theme
                        )
                    } else {
                        ViewUtils.setGone(cbBg)
                        animation.cancel()
                        resources.getDrawable(R.mipmap.base_suspend_bg, theme)
                    }

                    tvAddClockTime.text =
                        if (btnSuccess.isChecked) getString(R.string.base_pump_start_desc)
                        else getString(R.string.base_pump_stop_dec)
                }
            }
        }
    }

    /**
     * 初始化排水UI、以及设置
     */
    private fun initPumpWater() {
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
            ToastUtil.shortShow("${data?.size}")
            adapter.setList(data)
        }

        // 因为是通过by lazy 添加的， 所以状态需要重置
        // isFirst = true
        binding.waterPop.btnSuccess.isChecked = true
        binding.waterPop.btnSuccess.let {
            it.background = resources.getDrawable(
                R.mipmap.base_start_bg,
                theme
            )
        }
        binding.waterPop.tvAddClockTime.text = "Click to start draining"


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

        binding.waterPop.rvAdd.adapter = adapter
        val helperCallback: CardTouchHelperCallback<AdvertisingData> =
            CardTouchHelperCallback<AdvertisingData>(binding.waterPop.rvAdd, adapter.data, setting)
        val mReItemTouchHelper = ReItemTouchHelper(helperCallback)
        val layoutManager = CardLayoutManager(mReItemTouchHelper, setting)
        binding.waterPop.rvAdd.layoutManager = layoutManager

        // 手动滑动第一张
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            mReItemTouchHelper.swipeManually(ReItemTouchHelper.RIGHT)
        }, 3000)


        // 蓝牙状态监听变化
        LiveEventBus.get().with(Constants.Tuya.KEY_TUYA_DEVICE_TO_APP, String::class.java)
            .observe(this) {
                val map = GSON.parseObject(it, Map::class.java)
                map?.forEach { (key, value) ->
                    when (key) {
                        TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                            // 涂鸦指令，添加排水功能
                            //                            isOpenOrStop(false)
                            // 排水成功
                            if ((value as? Boolean == false)) return@observe
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        //
                        TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_INSTRUCTIONS -> {
                            // 涂鸦指令，添加排水功能
                            // isOpenOrStop(value)
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
                                binding.waterPop.btnSuccess.background =
                                    if ((value as? Boolean != true)) {
                                        ViewUtils.setGone(binding.waterPop.cbBg)
                                        animation.cancel()
                                        resources.getDrawable(
                                            R.mipmap.base_start_bg,
                                            theme
                                        )
                                    } else {
                                        showAlphaAnimation()
                                        resources.getDrawable(
                                            R.mipmap.base_suspend_bg,
                                            theme
                                        )
                                    }
                                if (binding.waterPop.btnSuccess.isChecked) {
                                    // 暂停
                                    if (value == false) {
                                        binding.waterPop.tvAddClockTime.text = getString(R.string.base_pump_auto_start_desc)
                                    } else {
                                        binding.waterPop.tvAddClockTime.text = getString(R.string.base_pump_start_desc)
                                    }
                                } else {
                                    // 暂停
                                    if (value == false) {
                                        binding.waterPop.tvAddClockTime.text = getString(R.string.base_pump_stop_dec)
                                    } else {
                                        binding.waterPop.tvAddClockTime.text = getString(R.string.base_pump_start_desc)
                                    }
                                }
                                binding.waterPop.btnSuccess.isChecked = value as Boolean
                            }

                            if ((value as? Boolean == false)) return@observe
                            // 查询是否排水结束
                            TuyaHomeSdk.newDeviceInstance(tuYaDeviceBean?.devId)?.let {
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


    /**
     * 排水的状态执行
     */
    private fun isOpenOrStop(value: Any?) {
        DeviceControl.get()
            .success {
                if (unreadMessageData.isNullOrEmpty()) return@success
                if (unreadMessageData?.firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER) {
                    mViewMode.deviceOperateStart(
                        business = "${unreadMessageData?.firstOrNull()?.messageId}",
                        type = UnReadConstants.StatusManager.VALUE_STATUS_PUMP_WATER
                    )
                    return@success
                }
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
                    job?.cancel()
                    setResult(Activity.RESULT_OK)
                    finish()
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

    /**
     * 动画开始
     */
    private fun showAlphaAnimation() {
        binding.waterPop.apply {
            ViewUtils.setVisible(cbBg)
            when (count) {
                1 -> {
                    animation.cancel()
                    ViewUtils.setVisible(ivWaterOne)
                    ViewUtils.setGone(ivWaterTwo, ivWaterThree)
                    ivWaterOne.animation = animation
                    animation.start()
                }
                2 -> {
                    animation.cancel()
                    ViewUtils.setVisible(ivWaterOne, ivWaterTwo)
                    ViewUtils.setGone(ivWaterThree)
                    ivWaterOne.animation = null
                    ivWaterTwo.animation = animation
                    animation.start()
                }
                else -> {
                    animation.cancel()
                    ViewUtils.setVisible(ivWaterOne, ivWaterTwo, ivWaterThree)
                    ivWaterOne.animation = null
                    ivWaterTwo.animation = null
                    ivWaterThree.animation = animation
                    animation.start()
                }
            }
        }
    }


    override fun observe() {
    }

    override fun initData() {
    }

    override fun onDestroy() {
        super.onDestroy()
        isOpenOrStop(false)
        job?.cancel()
        animation.cancel()
        BottomSheetBehavior.from(binding.ll).removeBottomSheetCallback(callback)
        mWakeLock?.release()
        GSYVideoManager.releaseAllVideos()
    }

    companion object {
        const val KEY_DATA = "key_data"
        const val KEY_UNREAD_MESSAGE_DATA = "KEY_UNREAD_MESSAGE_DATA"

        const val KEY_PUMP_FINISH = 10
    }
}