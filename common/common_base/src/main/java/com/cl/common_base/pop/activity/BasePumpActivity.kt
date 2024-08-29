package com.cl.common_base.pop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.BaseApplication
import com.cl.common_base.R
import com.cl.common_base.adapter.PumpWaterAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.bean.LikeReq
import com.cl.common_base.bean.RewardReq
import com.cl.common_base.bean.UnreadMessageData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.databinding.BasePopPumpActivityBinding
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.BasePumpWaterFinishedPop
import com.cl.common_base.pop.BaseThreeTextPop
import com.cl.common_base.pop.RewardPop
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.widget.toast.ToastUtil
import com.lin.cardlib.CardSetting
import com.lin.cardlib.OnSwipeCardListener
import com.lin.cardlib.utils.ReItemTouchHelper
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.widget.SmartDragLayout
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.Serializable
import javax.inject.Inject


/**
 * 排水弹窗
 */
@AndroidEntryPoint
class BasePumpActivity : BaseActivity<BasePopPumpActivityBinding>() {
    @Inject
    lateinit var mViewMode: BasePumpViewModel

    /**
     * 图文数据
     */
    /*private val data by lazy {
        intent?.extras?.getSerializable(KEY_DATA) as? MutableList<AdvertisingData>
    }*/

    /**
     * 未读消息列表数据
     */
    private val unreadMessageData by lazy {
        intent.getSerializableExtra(KEY_UNREAD_MESSAGE_DATA) as? MutableList<UnreadMessageData>
    }

    /**
     * 是否是英制
     */
    private val isFractional by lazy {
        val iss = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        iss
    }

    /**
     * 一系列的TaskId数组
     *
     * 如果携带这个数组跳转到这个界面的，目前必定是从日历界面跳转过来的
     */
    private val taskIdList by lazy { (intent.getSerializableExtra(BasePopActivity.KEY_TASK_ID_LIST) as? MutableList<CalendarData.TaskList.SubTaskList>) ?: mutableListOf() }

    /**
     * 用于固定解锁的或者跳转的id
     */
    private val fixedId by lazy { intent.getStringExtra(BasePopActivity.KEY_FIXED_TASK_ID) }

    /**
     * 用于完成任务包的packNo
     */
    private val packNo by lazy { intent.getStringExtra(BasePopActivity.KEY_PACK_NO) }

    /**
     * 用于推迟任务包的taskNo
     */
    private val taskNo by lazy { intent.getStringExtra(BasePopActivity.KEY_TASK_NO) }

    /**
     * 传入过来的用于FinishTask的ViewDatas
     */
    private val viewDatas by lazy {
        val inputData = intent.getSerializableExtra(BasePopActivity.KEY_INPUT_BOX) as? MutableList<FinishTaskReq.ViewData>
        inputData ?: mutableListOf()
    }

    /**
     * 传递过来的taskId
     */
    private val taskId by lazy { intent.getStringExtra(BasePopActivity.KEY_TASK_ID) }


    /**
     * 排水界面视图适配器
     */
    private val adapter by lazy {
        PumpWaterAdapter(mutableListOf())
    }

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun initView() {
        // 刷新
        binding.waterPop.refreshLayout.apply {
            ClassicsFooter.REFRESH_FOOTER_LOADING = getString(R.string.string_255) //"正在刷新...";
            ClassicsFooter.REFRESH_FOOTER_REFRESHING = getString(R.string.string_255) //"正在加载...";
            ClassicsFooter.REFRESH_FOOTER_NOTHING = getString(R.string.string_256)
            ClassicsFooter.REFRESH_FOOTER_FINISH = getString(R.string.string_257)
            ClassicsFooter.REFRESH_FOOTER_FAILED = getString(R.string.string_258)

            // 加载更多是显示是否显示新内容
            setEnableScrollContentWhenLoaded(false)

            // 刷新监听
            setOnRefreshListener {
                // 重新加载数据
                logI("setOnRefreshListener: refresh")
                mViewMode.updateCurrent(1)
                mViewMode.advertising()
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (mViewMode.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                mViewMode.updateCurrent(current)
                mViewMode.advertising()
            }
            // 刷新头部局
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
            // 刷新高度
            setHeaderHeight(60f)
            // 自动刷新
            // autoRefresh()
        }

        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.smart) { v, insets ->
            binding.waterPop.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
        binding.smart.setDuration(600)
        binding.smart.enableDrag(false) // 禁止拖拽
        binding.smart.dismissOnTouchOutside(false)
        binding.smart.isThreeDrag(false)
        binding.smart.open()
        binding.smart.setOnCloseListener(object : SmartDragLayout.OnCloseListener {
            override fun onClose() {
                finish()
            }

            override fun onDrag(y: Int, percent: Float, isScrollUp: Boolean) {
                // binding.smart.alpha = percent
            }

            override fun onOpen() {
            }
        })
        mViewMode.advertising()
        // 最长持有10分钟
        mWakeLock?.acquire(10*60*1000L)
        initClick()
    }

    /**
     * 排水界面的点击i时间
     */
    private var isFirst = true

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initClick() {
        binding.waterPop.apply {

            // 点击排水跳过
            tvSkip.setOnClickListener {
                XPopup.Builder(this@BasePumpActivity)
                    .asCustom(BaseThreeTextPop(this@BasePumpActivity, content = getString(R.string.string_260),
                        oneLineText = getString(R.string.string_261), twoLineText = getString(R.string.string_262),
                        oneLineCLickEventAction = {
                            isOpenOrStop(true)
                        },
                        twoLineCLickEventAction = {
                        isHaveTaskExcu()
                    })).show()
            }

            ivClose.setOnClickListener {
                directShutdown()
            }
            btnSuccess.setOnClickListener {
                isOpenOrStop(if (isFirst) isFirst else btnSuccess.isChecked)
                isFirst = false
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
                        // ViewUtils.setGone(cbBg)
                        animation.cancel()
                        resources.getDrawable(R.mipmap.base_suspend_bg, theme)
                    }


                    tvAddClockTime.text =
                        if (btnSuccess.isChecked) getString(R.string.the_program, if (!isFractional) "1.5 gallons+" else "6L")
                        else getString(R.string.base_pump_stop_dec)

                    // The program will pause every 2 minutes to prevent overflow. We recommend using a container of 1.5 gallons+ for water changes.
                    tvAddClockTime.text =
                        if (btnSuccess.isChecked) getString(R.string.the_program, if (!isFractional) "1.5 gallons+" else "6L")
                        else getString(R.string.base_pump_stop_dec)
                }
            }
        }
    }


    /**
     * 是否有携带过来的任务需要处理
     */
    private fun isHaveTaskExcu() {
        // 暂停排水
        kotlin.runCatching { // 需要判断当前是否携带过来的任务是为空
            if (taskIdList.isEmpty()) { //  如果是空的，那么因该是没有这个选项的。
                // 开启排水
                isOpenOrStop(false)
                // 弹出完成排水界面
                XPopup.Builder(this@BasePumpActivity).isDestroyOnDismiss(false).enableDrag(false)
                    .maxHeight(dp2px(600f)).dismissOnTouchOutside(false).asCustom(
                        BasePumpWaterFinishedPop(this@BasePumpActivity)
                    ).show()
                return@runCatching
            } else { //  如果不是空的,判断是否是最后一个任务，
                if (taskIdList.size == 1) { // 如果是最后一个任务，那么直接完成当前任务，并且返回
                    mViewMode.finishTask(FinishTaskReq(taskId = fixedId.toString(), packetNo = packNo.toString(), viewDatas = if (viewDatas.isEmpty()) null else viewDatas))
                } else { // 如果不是最后一个任务，那么需要先移除第1个，然后在进行第二个。
                    if (taskIdList.size > 0) {
                        taskIdList.removeAt(0)
                        // 如果不是最后一个任务，那么根据taskType进行跳转
                        // 换水任务
                        if (taskIdList[0].jumpType == CalendarData.KEY_JUMP_TYPE_TO_WATER) { // 换水加载图文数据
                            mViewMode.advertising()
                            return
                        }

                        // 跳转到富文本
                        val intent = Intent(this@BasePumpActivity, BasePopActivity::class.java)
                        intent.putExtra(BasePopActivity.KEY_TASK_ID, taskId)
                        intent.putExtra(BasePopActivity.KEY_TASK_ID_LIST, taskIdList as? Serializable)
                        intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, fixedId)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, taskIdList[0].textId)
                        intent.putExtra(BasePopActivity.KEY_INPUT_BOX, viewDatas as? Serializable)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_TASK_PACKAGE_ID, true)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, getString(com.cl.common_base.R.string.string_263))
                        intent.putExtra(BasePopActivity.KEY_PACK_NO, packNo)
                        intent.putExtra(BasePopActivity.KEY_TASK_NO, taskIdList[0].taskNo)
                        startActivity(intent)
                        return
                    }
                }
            }
        }
    }

    /**
     * 初始化排水UI、以及设置
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initPumpWater(data: MutableList<AdvertisingData> ? = null) {
        if (data?.size != 0) {
            // 卡片布局需要展示3张，所以需要多添加几张
            if ((data?.size ?: 0) == 1) {
                data?.get(0)?.let { data.add(it) }
            }
            if ((data?.size ?: 0) == 2) {
                data?.get(0)?.let { data.add(it) }
                data?.get(1)?.let { data.add(it) }
            }
            adapter.setList(data)
        }

        binding.waterPop.btnSuccess.let {
            it.background = resources.getDrawable(
                R.mipmap.base_start_bg,
                theme
            )
        }

        binding.waterPop.tvAddClockTime.text = getString(R.string.the_program, if (!isFractional) "1.5 gallons+" else "6L")


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
        /*val helperCallback: CardTouchHelperCallback<AdvertisingData> =
            CardTouchHelperCallback<AdvertisingData>(binding.waterPop.rvAdd, adapter.data, setting)
        val mReItemTouchHelper = ReItemTouchHelper(helperCallback)
        val layoutManager = CardLayoutManager(mReItemTouchHelper, setting)*/

        binding.waterPop.rvAdd.layoutManager = LinearLayoutManager(this)
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.waterPop.rvAdd)

        // todo 手动滑动第一张
        /* Handler().postDelayed(kotlinx.coroutines.Runnable {
             mReItemTouchHelper.swipeManually(ReItemTouchHelper.RIGHT)
         }, 3000)*/


        // 蓝牙状态监听变化
        LiveEventBus.get().with(Constants.Tuya.KEY_THING_DEVICE_TO_APP, String::class.java)
            .observe(this) {
                val map = GSON.parseObjectInBackground(it, Map::class.java) {map ->
                    map?.forEach { (key, value) ->
                        when (key) {
                            // 排水成功，取消监听。
                            /*TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                                // 涂鸦指令，添加排水功能
                                //                            isOpenOrStop(false)
                                // 排水成功
                                if ((value as? Boolean == false)) return@observe
                                job?.cancel()
                                // 判断当前播放器是否全屏
                                if (GSYVideoManager.isFullState(this@BasePumpActivity)) {
                                    finishVideoAndPump = true
                                    ToastUtil.shortShow(getString(R.string.draining_complete))
                                    return@observe
                                }
                                setResult(Activity.RESULT_OK)
                                finish()
                            }*/
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
                                            //  ViewUtils.setGone(binding.waterPop.cbBg)
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
                                            binding.waterPop.tvAddClockTime.text = getString(R.string.the_program, if (!isFractional) "1.5 gallons+" else "6L")
                                        }
                                    } else {
                                        // 暂停
                                        if (value == false) {
                                            binding.waterPop.tvAddClockTime.text = getString(R.string.base_pump_stop_dec)
                                        } else {
                                            binding.waterPop.tvAddClockTime.text = getString(R.string.the_program, if (!isFractional) "1.5 gallons+" else "6L")
                                        }
                                    }
                                    binding.waterPop.btnSuccess.isChecked = value as Boolean
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


    /**
     * 排水的状态执行
     *
     * false 暂停
     * true 开始
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

    // 是否排水完毕、以及视频退出全屏
    private var finishVideoAndPump = false
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
                    // 判断是否需要处理任务
                    isHaveTaskExcu()

                    //  isHaveTaskExcu() 方法最后结尾有返回，如果没进下面的方法，就不会执行下面的代码
                    // 判断当前播放器是否全屏
                    if (GSYVideoManager.isFullState(this@BasePumpActivity)) {
                        finishVideoAndPump = true
                        ToastUtil.shortShow(getString(R.string.draining_complete))
                        return@countDownCoroutines
                    }
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
    @RequiresApi(Build.VERSION_CODES.FROYO)
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
                    ivWaterTwo.animation = animation
                    animation.start()
                }

                3 -> {
                    animation.cancel()
                    ViewUtils.setVisible(ivWaterThree)
                    ivWaterOne.animation = null
                    ivWaterTwo.animation = null
                    ivWaterThree.animation = animation
                    animation.start()
                }

                else -> {
                    animation.cancel()
                    ViewUtils.setGone(ivWaterThree)
                    ViewUtils.setVisible(ivWaterOne, ivWaterTwo, ivWaterThree)
                    ivWaterThree.animation = null
                    ivWaterThree.animation = animation
                    animation.start()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
        // 也就是从全屏退出、然后排水也结束了。
        if (!GSYVideoManager.isFullState(this@BasePumpActivity) && finishVideoAndPump) {
            GSYVideoManager.releaseAllVideos()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }


    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun observe() {
        mViewMode.apply {
            // finishTask
            finishTask.observe(this@BasePumpActivity, resourceObserver {
                success {
                    job?.cancel()
                    // 判断当前播放器是否全屏
                    if (GSYVideoManager.isFullState(this@BasePumpActivity)) {
                        finishVideoAndPump = true
                        ToastUtil.shortShow(getString(R.string.draining_complete))
                        return@success
                    }
                    // 不管是从富文本界面进来的，还是从任务列表进来的，都需要判断是否有任务需要执行setResult，然后会通过onActivityResult返回给出相对应的逻辑
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            })

            // 排水界面富文本
            advertising.observe(this@BasePumpActivity, resourceObserver {
                error { errorMsg, code ->
                    if (binding.waterPop.refreshLayout.isRefreshing) {
                        binding.waterPop.refreshLayout.finishRefresh()
                    }
                    if (binding.waterPop.refreshLayout.isLoading) {
                        binding.waterPop.refreshLayout.finishLoadMore()
                    }
                }

                success {
                    // 跳转到换水页面
                   /* android.os.Handler().postDelayed({
                        // 传递的数据为空
                        val intent = Intent(this@BasePumpActivity, BasePumpActivity::class.java)
                        intent.putExtra(BasePopActivity.KEY_TASK_ID_LIST, taskIdList as? Serializable)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, fixedId)
                        intent.putExtra(BasePumpActivity.KEY_DATA, data as? Serializable)
                        intent.putExtra(BasePopActivity.KEY_PACK_NO, packNo)
                        startActivity(intent)
                    }, 50)*/

                    // 刷新相关
                    if (binding.waterPop.refreshLayout.isRefreshing) {
                        binding.waterPop.refreshLayout.finishRefresh()
                    }
                    if (binding.waterPop.refreshLayout.isLoading) {

                        // 没有加载了、或者加载完毕
                        if ((data?.size ?: 0) <= 0) {
                            binding.waterPop.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            binding.waterPop.refreshLayout.finishLoadMore()
                        }
                    }
                    if (null == this.data) return@success


                    // 数据相关
                    data.let {
                        val current = mViewMode.updateCurrent.value
                        if (current == 1) {
                            initPumpWater(it)
                            // 刷新数据
                            // adapter.setList(it)
                        } else {
                            // 追加数据
                            it.let { it1 -> adapter.addData(adapter.data.size, it1) }
                        }
                    }
                }
            })

            // 点赞
            likeData.observe(this@BasePumpActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success

                    val item = adapter.data[position] as? AdvertisingData
                    item?.let {
                        it.isPraise = 1
                        it.praise = it.praise?.plus(1)
                        adapter.notifyItemChanged(position)
                    }
                }
            })
            // 取消点赞
            unlikeData.observe(this@BasePumpActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success

                    val item = adapter.data[position] as? AdvertisingData
                    item?.let {
                        it.isPraise = 0
                        it.praise = it.praise?.minus(1)
                        adapter.notifyItemChanged(position)
                    }
                }
            })

            // 打赏
            rewardData.observe(this@BasePumpActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    val oxygenNum = mViewMode.rewardOxygen.value
                    val position = mViewMode.currentPosition.value ?: -1
                    if (oxygenNum == 0) return@success
                    if (position == -1) return@success

                    val data = adapter.data[position]
                    data.reward = oxygenNum?.let { data.reward?.plus(it) }
                    data.isReward = 1

                    // 刷新当前
                    adapter.notifyItemChanged(position)
                }
            })

        }
    }

    override fun initData() {
        //                         mViewMode.like(LikeReq(learnMoreId = item.learnMoreId, likeId = item.id.toString(), type = "moments"))
        adapter.addChildClickViewIds(R.id.cl_love, R.id.cl_gift)
        adapter.setOnItemChildClickListener { _, view, position ->
            val data = adapter.data[position]
            mViewMode.updateCurrentPosition(position)
            when (view.id) {
                R.id.cl_love -> {
                    if (data.isPraise == 0) {
                        mViewMode.like(LikeReq(likeId = adapter.data[position].id.toString(), type = "drain_water"))
                    } else {
                        mViewMode.unlike(LikeReq(likeId = adapter.data[position].id.toString(), type = "drain_water"))
                    }
                    // 震动
                    SoundPoolUtil.instance.startVibrator(context = this@BasePumpActivity)
                }

                R.id.cl_gift -> {
                    /* startActivity(Intent(this, GiftActivity::class.java).apply {
                         putExtra(GiftActivity.KEY_DATA, adapter.data[position])
                     })*/
                    XPopup.Builder(this@BasePumpActivity)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .asCustom(
                            RewardPop(this@BasePumpActivity, onRewardListener = { oxygenNum ->
                                mViewMode.updateRewardOxygen(oxygenNum.toInt())
                                mViewMode.reward(
                                    RewardReq(
                                        momentsId = data.id.toString(),
                                        oxygenNum = oxygenNum,
                                        type = "drain_water",
                                    )
                                )
                            })
                        ).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        directShutdown()
    }


    // 直接关闭
    private fun directShutdown() {
        if (taskIdList.isNotEmpty()) {
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).navigation(this@BasePumpActivity)
            return
        }
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    override fun onDestroy() {
        super.onDestroy()
        isOpenOrStop(false)
        job?.cancel()
        animation.cancel()
        mWakeLock?.release()
        GSYVideoManager.releaseAllVideos()
    }

    companion object {
        const val KEY_DATA = "key_data"
        const val KEY_UNREAD_MESSAGE_DATA = "KEY_UNREAD_MESSAGE_DATA"

        const val KEY_PUMP_FINISH = 10
    }
}