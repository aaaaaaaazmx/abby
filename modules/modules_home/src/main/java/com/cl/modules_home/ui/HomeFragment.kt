package com.cl.modules_home.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bbgo.module_home.R
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.request.AutomaticLoginReq
import com.cl.modules_home.viewmodel.HomeViewModel
import com.cl.modules_home.widget.*
import com.bbgo.module_home.databinding.HomeBinding
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.bean.FinishPageData
import com.cl.common_base.bean.JpushMessageData
import com.cl.common_base.bean.UnreadMessageData
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.pop.*
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.util.span.appendClickable
import com.cl.modules_home.adapter.HomeFinishItemAdapter
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.random.Random

/**
 * 种植引导Fragment
 * 种植继承
 */
@Route(path = RouterPath.Home.PAGE_HOME)
@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeBinding>() {

    @Inject
    lateinit var mViewMode: HomeViewModel

    // 倒计时携程任务
    private var job: Job? = null

    // 引导状态，也就是到第几步
    // 默认为null，也就是默认为0，也就是从plant1开始走
    private val plantGuideFlag by lazy {
        val flag = arguments?.getString(Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG)
        flag ?: "0"
    }

    // 植物存在状态（0-未种植、1-已种植、2-未种植，且存在旧种植记录、3-种植完成过）
    private val plantFlag by lazy {
        val flag = arguments?.getString(Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE)
        flag ?: "0"
    }

    // 传过来的设备状态
    // 默认为不在线
    private val deviceOffLineStatus by lazy {
        val flag = arguments?.getString(Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE)
        flag ?: "0"
    }

    override fun initView(view: View) {
        ARouter.getInstance().inject(this)
        binding.plantOffLine.title.setLeftVisible(false)
        // 刷新数据以及token
        // 一并检查下当前的状态
        mViewMode.refreshToken(
            AutomaticLoginReq(
                userName = mViewMode.account,
                password = mViewMode.psd,
                token = Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN)
            )
        )

        // getAppVersion 检查版本更新
        mViewMode.getAppVersion()

        // 极光应用内部消息
        // 主要是用来显示气泡
        LiveEventBus.get().with(Constants.Jpush.KEY_IN_APP_MESSAGE, String::class.java)
            .observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) return@observe
                kotlin.runCatching {
                    // 极光消息需要插入最前面，并且去重
                    val unReadList = mViewMode.unreadMessageList.value ?: mutableListOf()
                    // 接续数据
                    val extras = GSON.parseObject(it.toString(), JpushMessageData::class.java)
                    val unreadMessage =
                        GSON.parseObject(extras?.extras.toString(), UnreadMessageData::class.java)
                    // 删除和极光消息一样的ID
                    unReadList.removeIf { data -> data.messageId == unreadMessage?.messageId }
                    // 把极光消息添加到第一个，并且展示
                    unreadMessage?.let { unRead -> unReadList.add(unRead) }
                    // 添加到ViewModel
                    mViewMode.setUnreadMessageList(unReadList)
                    // 更改气泡内容
                    changUnReadMessageUI()
                    // 刷新信息
                    mViewMode.plantInfo()
                }
            }

        // 开启定时器，每次20秒刷新未读气泡消息
        job = mViewMode.countDownCoroutines(
            10 * 6 * 500000,
            lifecycleScope,
            onTick = {
                if (it % 20 == 0) {
                    // 表示过了20秒
                    mViewMode.getUnread()
                }
                if (it == 0) {
                    job?.cancel()
                }
            },
            onStart = {},
            onFinish = {
                // todo 这个finish也指的是当前页面被关闭, 定时任务不能放在这个地方.
                job?.cancel()
            })
    }

    /**
     * 水箱水的状态
     */
    private fun setWaterStatus(value: Any?) {
        binding.pplantNinth.ivWaterStatus.background = when (value) {
            "0L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc,
                        R.mipmap.home_low_water
                    )
                }
            }
            "1L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc,
                        R.mipmap.home_ok_water
                    )
                }
            }
            "2L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc,
                        R.mipmap.home_ok_water
                    )
                }
            }
            "3L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc,
                        R.mipmap.home_max_water
                    )
                }
            }
            else -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc,
                        R.mipmap.home_low_water
                    )
                }
            }
        }
    }

    /**
     * 进入这个页面时，所展示的逻辑
     * 主要是用来切换当前植物的状态UI方法
     */
    private fun showView(viewPlantFlag: String, viewPlantGuideFlag: String) {
        // 根据传过来的flag获取图文引导
        logI(
            """ 
            plantFlag： $viewPlantFlag
            plantGuideFlag: $viewPlantGuideFlag
        """.trimIndent()
        )
        // 判断当前植物存在状态
        when (viewPlantFlag) {
            // 从来没有种植过
            KEY_NEW_PLANT -> {
                // 未种植
                // 直接跳转到到第几步骤
                /**
                 * 默认进Plant 1， 2
                Plant2后记“1”，Plant4,5打断回3
                plant5后记“2”，plan6打断回3的continue
                plant6后记“3”，89打断后回7
                plant9后记“4”，没有start running则停abby#1
                 */
                when (viewPlantGuideFlag) {
                    "0" -> {
                        // 这是默认进入的，也就是Flag = null 的情况下
                        ViewUtils.setVisible(binding.plantFirst.root)
                    }
                    "1" -> {
                        ViewUtils.setVisible(binding.plantAddWater.root)
                    }
                    "2" -> {
                        ViewUtils.setVisible(binding.plantAddWater.root)
                        ViewUtils.setVisible(binding.plantAddWater.clContinue)
                        ViewUtils.setGone(binding.plantAddWater.ivAddWater)
                    }
                    "3" -> {
                        ViewUtils.setVisible(binding.plantClone.root)
                    }
                    "4" -> {
                        ViewUtils.setVisible(binding.pplantNinth.root)
                    }
                }
            }
            // 已经种植过了
            KEY_PLANTED -> {
                // 显示布局
                ViewUtils.setVisible(binding.pplantNinth.root)
                //  todo 这个显示有问题，会重复隐藏
                ViewUtils.setGone(binding.pplantNinth.clContinue)
                mViewMode.plantInfo()
            }
            // 继承
            KEY_EXTEND_PLANT -> {
                // 弹出继承或者重新种植的的窗口
                //                ViewUtils.setVisible(binding.plantExtendBg.root)
                // todo 由于涂鸦下发的onLine会重新刷新这个，所以需要判断一下
                //                if (plantExtendPop.isShow) return
                //                plantExtendPop.show()
                // todo 现在不走这个继承弹窗了，直接走页面了
                // 继承弹窗需要直接删除了
                ViewUtils.setGone(binding.plantExtendBg.root)
                ViewUtils.setVisible(binding.plantFirst.root)
            }
            // 种植完成过
            KEY_PLANTING_COMPLETED -> {
                // 种植完成过
                mViewMode.getFinishPage()
                ViewUtils.setVisible(binding.plantComplete.root)
            }
        }
    }

    /**
     * 各种View 点击方法
     */
    override fun lazyLoad() {
        // 跳转到种植引导界面
        binding.plantFirst.apply {
            // 跳跳转plant2
            ivStart.setOnClickListener {
                mViewMode.start()
            }

        }

        // 添加水的步骤
        binding.plantAddWater.apply {
            ivAddWater.setOnClickListener {
                plantFour.show()
            }
            svContinue.setOnClickListener {
                plantSix.show()
            }
        }


        // clone步骤
        binding.plantClone.apply {
            start.setOnClickListener {
                plantEight.show()
            }
        }

        // 开始种植
        binding.pplantNinth.apply {
            // 选中日历
            ivCalendar.setOnClickListener {
                // 如果是订阅用户
                if (mViewMode.refreshToken.value?.data?.isVip == 1) {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).navigation()
                } else {
                    // 不是订阅用户，直接弹出图文
                    mViewMode.getGuideInfo("unearned_subscription_explain")
                }
            }

            // 客服支持
            ivSupport.setOnClickListener {
                // todo 客服支持
                // 暂时跳转邮箱
                sendEmail()
            }

            tvFeef.setOnClickListener {
                plantFeed.show()
            }

            tvDrain.setOnClickListener {
                plantDrain.show()
            }

            // 点击弹出周期弹窗
            clPeroid.setOnClickListener {
                // 调用接口一次
                mViewMode.plantInfo()
                mViewMode.periodData.value?.let { data -> periodPop?.setData(data) }
                periodPopDelegate.show()
            }

            // 点击环境弹窗
            clEnvir.setOnClickListener {
                // 刷新植物信息以及环境信息
                mViewMode.plantInfo()
                // 每次都获取到了信息
                mViewMode.tuyaDeviceBean?.devId?.let { devId -> mViewMode.environmentInfo(devId) }
            }

            // 点击弹窗上面的关闭
            ivClose.setOnClickListener {
                // 标记已读
                if (mViewMode.unreadMessageList.value.isNullOrEmpty()) return@setOnClickListener
                mViewMode.unreadMessageList.value?.let { message ->
                    if (message.size == 0) return@setOnClickListener
                    // 标记已读信息
                    mViewMode.getRead("${message.first().messageId}")
                }
            }
        }

        // 设备不在线
        binding.plantOffLine.apply {

        }

        // 解锁完成界面点击事件相关
        binding.plantComplete.apply {
            // 重新种植
            completeStart.setOnClickListener {
                // 已读全部消息
                mViewMode.unReadAll()
                // 种植完成
                mViewMode.plantFinish("")
            }
            // 分享
            completeIvShare.setOnClickListener {
                // todo
            }
        }
    }

    /**
     *  初始化弹窗
     */
    private val plantOnePop by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomePlantTwoPop(
                    context = it,
                    onNextAction = {
                        ViewUtils.setVisible(binding.plantAddWater.root)
                        ViewUtils.setGone(binding.plantFirst.root)
                    }
                )
            })
    }

    /**
     * 添加化肥弹窗
     */
    private val plantFeed by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomePlantFeedPop(
                    context = it,
                    onNextAction = {
                        // 涂鸦指令，添加化肥
                        DeviceControl.get()
                            .success {
                                // 加肥气泡
                                if (mViewMode.unreadMessageList.value?.first()?.type == UnReadConstants.Device.KEY_ADD_MANURE) {
                                    mViewMode.getRead("${mViewMode.unreadMessageList.value?.first()?.messageId}")
                                    return@success
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
            })
    }

    /**
     * 添加排水弹窗
     */
    private val plantDrain by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false)
            .asCustom(drainagePop)
    }

    /**
     * 排水完成弹窗
     */
    private val plantDrainFinished by lazy {
        context?.let {
            XPopup.Builder(it)
                .isDestroyOnDismiss(false)
                .enableDrag(false)
                .maxHeight(dp2px(600f))
                .dismissOnTouchOutside(false)
                .asCustom(context?.let {
                    BasePumpWaterFinishedPop(it, onSuccessAction = {
                        // 排水成功弹窗，点击OK按钮
                        mViewMode.unreadMessageList.value?.first()?.let { bean ->
                            // 如果正好是第一步排水
                            if (bean.extension.isNullOrEmpty() || bean.extension == UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_ONE) {
                                mViewMode.deviceOperateFinish(UnReadConstants.StatusManager.VALUE_STATUS_PUMP_WATER)
                            }
                            return@BasePumpWaterFinishedPop
                        }
                    })
                })
        }
    }

    /**
     *  继承弹窗
     *  暂时不需要
     */
    private val plantExtendPop by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomePlantExtendPop(
                    context = it,
                    onNextAction = { status ->
                        when (status) {
                            HomePlantExtendPop.KEY_NEW_PLANT -> {
                                ViewUtils.setGone(binding.plantExtendBg.root)
                                ViewUtils.setVisible(binding.plantFirst.root)
                            }
                            HomePlantExtendPop.KEY_EXTEND -> {
                                // 直接跳转到种植界面
                                ViewUtils.setGone(binding.plantExtendBg.root)
                                ViewUtils.setGone(binding.plantFirst.root)
                                ViewUtils.setVisible(binding.pplantNinth.root)
                                ViewUtils.setGone(binding.pplantNinth.clContinue)
                                // mViewMode.startRunning(null, true)
                            }
                        }
                    }
                )
            })
    }


    // 升级弹窗
    private val updatePop by lazy {
        context?.let {
            FirmwareUpdatePop(it, onConfirmAction = { isBoolean ->
                // 跳转到固件升级界面
                ARouter.getInstance()
                    .build(RouterPath.My.PAGE_MY_FIRMWARE_UPDATE)
                    .withBoolean(Constants.Global.KEY_GLOBAL_MANDATORY_UPGRADE, isBoolean)
                    .navigation()
            })
        }
    }


    /**
     * plant4 弹窗
     */
    private val plantFour by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomePlantFourPop(
                    context = it,
                    onNextAction = {
                        plantFive.show()
                    }
                )
            })
    }

    /**
     * plant5 弹窗 加水
     */
    private val plantFive by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomePlantFivePop(
                    context = it,
                    onCancelAction = {
                        // 在执行未读消息时，不需要showView
                        if (mViewMode.unreadMessageList.value?.first()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                            || mViewMode.unreadMessageList.value?.first()?.type == UnReadConstants.Device.KEY_ADD_WATER
                        ) {
                            return@HomePlantFivePop
                        }

                        // 种植引导时，点击取消弹窗时，处理得事。
                        // 状态改为2，然后
                        // 当作 plantGuideFlag = 2 来处理
                        //                        showView(plantFlag, "2")
                    },
                    onNextAction = {
                        // 如果是在换水的三步当中
                        if (mViewMode.unreadMessageList.value?.first()?.type == UnReadConstants.Device.KEY_CHANGING_WATER) {
                            // 弹出加肥
                            mViewMode.unreadMessageList.value?.first()?.let { bean ->
                                mViewMode.userMessageFlag(
                                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_THREE,
                                    "${bean.messageId}"
                                )
                                mViewMode.deviceOperateStart(
                                    "${bean.messageId}",
                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_MANURE
                                )
                            }
                            plantSix.show()
                            return@HomePlantFivePop
                        }

                        // 加水
                        if (mViewMode.unreadMessageList.value?.first()?.type == UnReadConstants.Device.KEY_ADD_WATER) {
                            mViewMode.getRead("${mViewMode.unreadMessageList.value?.first()?.messageId}")
                            return@HomePlantFivePop
                        }

                        // plant5后记“2”
                        mViewMode.setCurrentReqStatus(2)
                        mViewMode.saveOrUpdate("2")
                    }
                )
            })
    }

    private val drainagePop by lazy {
        context?.let {
            HomePlantDrainPop(
                context = it,
                onNextAction = {
                    // 请求接口
                    mViewMode.advertising()
                },
                onCancelAction = {

                },
                onTvSkipAddWaterAction = {
                    XPopup.Builder(it)
                        .isDestroyOnDismiss(false)
                        .enableDrag(false)
                        .maxHeight(dp2px(600f))
                        .dismissOnTouchOutside(false)
                        .asCustom(skipWaterConfirmPop)
                        .show()
                }
            )
        }
    }

    /**
     * 确认跳过换水啥的弹窗
     */
    private val skipWaterConfirmPop by lazy {
        context?.let {
            HomeSkipWaterPop(it, onConfirmAction = {
                // 跳过换水、加水、加肥
                mViewMode.unreadMessageList.value?.first()?.let { bean ->
                    val messagaeId = bean.messageId
                    mViewMode.deviceOperateStart(
                        "$messagaeId",
                        UnReadConstants.StatusManager.VALUE_STATUS_SKIP_CHANGING_WATERE
                    )
                    mViewMode.getRead("$messagaeId")
                }
            })
        }
    }

    /**
     * plant6 弹窗 加肥
     */
    private val plantSix by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .maxHeight(dp2px(600f))
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomePlantSixPop(
                    context = it,
                    onNextAction = {
                        // 需要先发送指令喂食
                        DeviceControl.get()
                            .success {
                                // 如果是在换水的三步当中的最后一步，加肥
                                if (mViewMode.unreadMessageList.value?.first()?.type == UnReadConstants.Device.KEY_CHANGING_WATER) {
                                    // 点击按钮就表示已读，已读会自动查看有没有下一条
                                    mViewMode.getRead("${mViewMode.unreadMessageList.value?.first()?.messageId}")
                                    return@success
                                }

                                // 第六个弹窗
                                // plant6后记“3”
                                mViewMode.setCurrentReqStatus(3)
                                mViewMode.saveOrUpdate("3")
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
            })
    }

    /**
     * 通用图文 弹窗
     */
    private val plantUsually by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .maxHeight(dp2px(600f))
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(custom)
    }

    /**
     * 风扇故障弹窗
     */
    private val fanFailPop by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(customFanFailPop)
    }

    private val customFanFailPop by lazy {
        context?.let {
            FanFailPop(it, {
                // todo 风扇故障，跳转环信客服
                // 并且表示已读
                mViewMode.getRead("${mViewMode.unreadMessageList.value?.first()?.messageId}")
                // todo 暂时不跳环信，跳转邮件转发
                sendEmail()
            }, {
                // 并且表示已读
                mViewMode.getRead("${mViewMode.unreadMessageList.value?.first()?.messageId}")
            })
        }
    }


    /**
     * 通用弹窗
     * 网络请求
     */
    private val custom by lazy {
        context?.let {
            BasePlantUsuallyPop(
                context = it,
                onNextAction = { weight ->
                    val unReadList = mViewMode.unreadMessageList.value
                    // 应该是判断当前的种植周期。
                    // 这个是引导阶段
                    if (unReadList.isNullOrEmpty() && mViewMode.popPeriodStatus.value.isNullOrEmpty()) {
                        /**
                         * 这个状态是自己自定义的状态，主要用于上报到第几步
                         * 上报步骤
                         * 引导阶段
                         */
                        /**
                         * 这个状态是自己自定义的状态，主要用于上报到第几步
                         * 上报步骤
                         * 引导阶段
                         */
                        when (mViewMode.typeStatus.value) {
                            0 -> {
                                // 当前表示开始种植
                                // 上报当前的步骤 1
                                mViewMode.setCurrentReqStatus(1)
                                mViewMode.saveOrUpdate("1")
                            }
                            1 -> {
                                // 这是第9个弹窗，开始种植，需要传入步骤为 4
                                mViewMode.setCurrentReqStatus(4)
                                mViewMode.saveOrUpdate("4")
                            }
                            2 -> {}
                            3 -> {}
                            4 -> {}
                            5 -> {}
                            6 -> {}
                        }
                        return@BasePlantUsuallyPop
                    }

                    // 表示是从极光或者未读消息列表中跳转过来的。
                    //                    if (UnReadConstants.plantStatus.contains(unReadList?.first()?.type)) {
                    //                        // 调取解锁接口，
                    //                        // Vegetation	1
                    //                        //  Flowering	2
                    //                        //  Flushing	3
                    //                        //  Drying	5
                    //                        //  Harvest	6
                    //                        //  Curing	7（请求图文时id转换为int）
                    //                        when (unReadList?.first()?.type) {
                    //                            UnReadConstants.Plant.KEY_VEGETATION -> mViewMode.unlockJourney("Vegetation")
                    //                            UnReadConstants.Plant.KEY_FLOWERING -> mViewMode.unlockJourney("Flowering")
                    //                            UnReadConstants.Plant.KEY_FLUSHING -> mViewMode.unlockJourney("Flushing")
                    //                            UnReadConstants.Plant.KEY_DRYING -> mViewMode.unlockJourney(
                    //                                "Drying",
                    //                                weight
                    //                            )
                    //                            UnReadConstants.Plant.KEY_HARVEST -> mViewMode.unlockJourney("Harvest")
                    //                            UnReadConstants.Plant.KEY_CURING -> {
                    //                                mViewMode.unlockJourney(
                    //                                    "Curing",
                    //                                    weight
                    //                                )
                    //                            }
                    //                        }
                    //                        return@HomePlantUsuallyPop
                    //                    }

                    // 其实上面和下面，可以优化为同一种类型，但是需要判断对极光消息的处理，还是分开的好，但是处理逻辑可以写在一起

                    // 如果是从解锁周期弹窗过来的，
                    // 这个后期可以优化，就不和上面的状态同一判断了。避免后期优化时忘记
                    if (!mViewMode.popPeriodStatus.value.isNullOrEmpty()) {
                        // 解锁接口
                        mViewMode.popPeriodStatus.value?.let { status ->
                            mViewMode.unlockJourney(status, weight)
                        }
                    }
                }
            )
        }
    }

    /**
     * plant8 弹窗
     */
    private val plantEight by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomePlantEightPop(
                    context = it,
                    onNextAction = {
                        // 跳转到第9个弹窗
                        // 开始种植 传入1
                        mViewMode.getGuideInfo("1")
                    }
                )
            })
    }

    /**
     * 这是画的周期弹窗
     */
    private val periodPopDelegate by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(true)
            .dismissOnTouchOutside(true)
            .asCustom(periodPop)

    }

    // 这是周期弹窗解锁周期的
    private val periodPop by lazy {
        context?.let {
            HomePeriodPop(
                it,
                unLockAction = { guideId ->
                    // todo 此处是用于周期弹窗解锁的
                    guideId?.let { it1 -> mViewMode.setPopPeriodStatus(it1) }
                }
            )
        }
    }

    private val pop by lazy {
        XPopup.Builder(context)
    }

    /**
     * 解锁Curing周期的弹窗
     */
    private val unlockCuringPop by lazy {
        context?.let {
            HomeUnlockCuringPop(it) {
                // 直接解锁
                mViewMode.unlockJourney("Curing")
            }
        }
    }

    /**
     * 环境弹窗
     */
    private val envirPopDelete by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(true)
            .dismissOnTouchOutside(true)
            .asCustom(envirPop)
    }
    private val envirPop by lazy {
        context?.let {
            HomeEnvlrPop(
                it
            )
        }
    }

    /**
     * APP检测升级弹窗
     */
    private val versionUpdatePop by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(versionPop)
    }
    private val versionPop by lazy {
        context?.let {
            VersionUpdatePop(it, onConfirmAction = {
                //                checkOtaUpdateInfo()
            }, onCancelAction = {
                //                checkOtaUpdateInfo()
            })
        }
    }

    /**
     * 修复SN订阅弹窗
     */
    private val rePairSnPop by lazy {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(context?.let {
                HomeRepairSnPop(it) {
                    // 跳转到找到SN码的地方
                    ARouter.getInstance()
                        .build(RouterPath.PairConnect.PAGE_SCAN_CODE)
                        .navigation()
                }
            })
    }

    /**
     * 种植完成，条目适配器
     */
    private val plantCompleteItemAdapter by lazy {
        HomeFinishItemAdapter(mutableListOf())
    }

    /**
     * 种植完成图文弹窗
     */
    private val plantFinishUsuallyPop by lazy {
        context?.let { LearnIdGuidePop(it) }
    }

    override fun observe() {
        mViewMode.apply {
            // 检查是否种植过
            // 检查植物
            checkPlant.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                success {
                    // 删除未读消息
                    mViewMode.removeFirstUnreadMessage()
                    // 清空气泡状态
                    mViewMode.setPopPeriodStatus(null)

                    // 是否种植过
                    data?.let { PlantCheckHelp().plantStatusCheck(it, true) }
                }
            })
            // 获取通用图文信息接口
            getDetailByLearnMoreId.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, _ ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    hideProgressLoading()
                    // 如果是种植完成调用了这个接口
                    // 那么getFinishPager这个接口返回的数据就不会为空
                    if (mViewMode.getFinishPage.value?.data?.list?.isNotEmpty() == true) {
                        // 跳转种植完成图文弹窗
                        plantFinishUsuallyPop?.setData(data)
                        pop
                            .isDestroyOnDismiss(false)
                            .enableDrag(true)
                            .maxHeight(dp2px(700f))
                            .dismissOnTouchOutside(false)
                            .asCustom(plantFinishUsuallyPop).show()
                        return@success
                    }
                }
            })

            // 气泡消息获取通用图文接口
            getMessageDetail.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, _ ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    hideProgressLoading()
                    // 跳转种植完成图文弹窗
                    plantFinishUsuallyPop?.setData(data)
                    pop
                        .isDestroyOnDismiss(false)
                        .enableDrag(true)
                        .maxHeight(dp2px(700f))
                        .dismissOnTouchOutside(false)
                        .asCustom(plantFinishUsuallyPop).show()
                }
            })

            // 种植完成参数获取
            getFinishPage.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    hideProgressLoading()
                    data?.let {
                        binding.plantComplete.tvTitle.text = it.title
                        val requestOptions = RequestOptions()
                        requestOptions.placeholder(com.cl.common_base.R.mipmap.placeholder)
                        requestOptions.error(com.cl.common_base.R.mipmap.errorholder)
                        //                        Glide.with(this@HomeFragment)
                        //                            .load(it.imageUrl)
                        //                            .apply(requestOptions)
                        //                            .into(binding.plantComplete.ivComplete)
                        binding.plantComplete.tvCompleteDesc.text =
                            getString(com.cl.common_base.R.string.complete_desc, it.harvestComplete)

                        val listBean = it.list
                        if (listBean.isEmpty()) return@success
                        binding.plantComplete.rvFinishGuide.layoutManager =
                            LinearLayoutManager(context)
                        binding.plantComplete.rvFinishGuide.adapter = plantCompleteItemAdapter
                        plantCompleteItemAdapter.setList(listBean)
                        // 设置点击事件
                        plantCompleteItemAdapter.addChildClickViewIds(R.id.ll_title)
                        plantCompleteItemAdapter.setOnItemChildClickListener { adapter, view, position ->
                            val data = adapter.data[position] as? FinishPageData.ListBean
                            when (view.id) {
                                R.id.ll_title -> {
                                    data?.learnMoreId?.let { it1 ->
                                        mViewMode.getDetailByLearnMoreId(
                                            it1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })
            // 气泡按钮点击事件
            bubbleOnClickEvent.observe(viewLifecycleOwner) { clickEvent ->
                if (clickEvent == false) return@observe
                // 自定义开始种植弹窗
                // 判断点击时长
                if (mViewMode.unreadMessageList.value.isNullOrEmpty()) {
                    // 解锁第一个周期
                    // 显示startRunning气泡的时候，必定是发芽了的
                    mViewMode.unlockJourney("Vegetation")
                    return@observe
                }

                // 未读消息弹窗，获取极光消息弹窗
                // 极光消息来了，只需要把这个消息添加到当前list就好了。
                mViewMode.unreadMessageList.value?.let {
                    // 调用图文信息
                    if (it.size == 0) return@let
                    mViewMode.unreadMessageList.value?.first()?.type?.let { type ->
                        // 目前只处理了种植状态
                        if (UnReadConstants.plantStatus.contains(type)) {
                            // 调用图文接口，获取图文并且弹窗
                            // 种植状态的是调用解锁，并不是调用已读
                            mViewMode.setPopPeriodStatus(type)
                        } else {
                            // todo  如果不是种植状态，那么就需要弹出自定义的窗口，各种设备状态图文未处理
                            // todo 设备故障跳转环信
                            when (type) {
                                // 换水、加水、加肥。三步
                                // 这玩意有三步！！！
                                UnReadConstants.Device.KEY_CHANGING_WATER -> {
                                    specificStep()
                                }
                                // 加水
                                UnReadConstants.Device.KEY_ADD_WATER -> {
                                    plantFour.show()
                                }
                                // 加肥
                                UnReadConstants.Device.KEY_ADD_MANURE -> {
                                    plantFeed.show()
                                }
                                else -> {}
                            }
                        }
                    }
                    // How to do LTS
                    mViewMode.unreadMessageList.value?.first()?.jumpType?.let { jumpType ->
                        if (jumpType == UnReadConstants.JumpType.KEY_LEARN_MORE) {
                            // 单独处理， 弹窗
                            mViewMode.getMessageDetail("${mViewMode.unreadMessageList.value?.first()?.messageId}")
                            mViewMode.getRead("${mViewMode.unreadMessageList.value?.first()?.messageId}")
                        }
                    }
                }
            }
            // 植物周期数据监听,植物周期弹窗数据
            periodData.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) return@observe
                // 加这位玩意的监听，就是为了加个小红点
                // 找到onging的状态，然后判断下面一个状态是不是解锁, 不是解锁就不需要显示小红点
                val index =
                    it.indexOfLast { bean -> "${bean.journeyStatus}" == HomePeriodPop.KEY_ON_GOING }
                if (index == -1) return@observe

                kotlin.runCatching {
                    ViewUtils.setVisible(
                        "${it[index + 1].journeyStatus}" == HomePeriodPop.KEY_LOCK_COMPLETED || "${it[index + 1].journeyStatus}" == HomePeriodPop.KEY_ALLOW_UNLOCKING,
                        binding.pplantNinth.ivNewRed
                    )
                }
            }

            // 周期内解锁，这个状态其实很垃圾，无奈选择为了赶进度
            popPeriodStatus.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) return@observe
                // 获取图文引导，然后解锁。
                mViewMode.getGuideInfo(it)
            }

            // 未读消息
            unreadMessageList.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) return@observe
                changUnReadMessageUI()
            }
            // 是否修复SN问题
            repairSN.observe(viewLifecycleOwner) {
                if (it == "NG") {
                    // 表示需要修复
                    rePairSnPop.show()
                } else {
                    if (rePairSnPop.isShow) rePairSnPop.dismiss()
                }
            }
            // 水的容积
            getWaterVolume.observe(viewLifecycleOwner) {
                setWaterStatus(it)
            }

            // 检查app版本更新
            getAppVersion.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    data?.let { versionData ->
                        // 强制升级才弹窗
                        if (versionData.forcedUpdate == "1") {
                            // versionUpdatePop
                            val split = versionData.version?.split(".")
                            var netWorkVersion = ""
                            split?.forEach { version ->
                                netWorkVersion += version
                            }
                            val localVersionSplit = AppUtil.appVersionName.split(".")
                            var localVersion = ""
                            localVersionSplit.forEach { version ->
                                localVersion += version
                            }
                            // 判断当前的版本号是否需要升级
                            kotlin.runCatching {
                                if (netWorkVersion.toInt() > localVersion.toInt()) {
                                    versionPop?.setData(versionData)
                                    versionUpdatePop.show()
                                }
                            }
                        }
                    }
                }
            })

            // 更新信息,刷新token
            refreshToken.observe(viewLifecycleOwner, resourceObserver {
                success {
                    hideProgressLoading()
                    // 保存当前的信息.
                    GSON.toJson(data)
                        ?.let {
                            logI("refreshToken: $it")
                            Prefs.putStringAsync(Constants.Login.KEY_LOGIN_DATA, it)
                        }
                    // 保存Token
                    data?.token?.let { it ->
                        Prefs.putStringAsync(
                            Constants.Login.KEY_LOGIN_DATA_TOKEN,
                            it
                        )
                    }
                    // JPush,添加别名
                    data?.abbyId?.let {
                        JPushInterface.setAlias(context, Random.nextInt(100), it)
                    }
                    /**
                     * 当有设备的时候，判断当前设备是否在线
                     *
                     * 1- 绑定状态
                     * 2- 解绑状态
                     */
                    if (data?.deviceStatus == "1") {
                        data?.deviceOnlineStatus?.let {
                            when (it) {
                                // 	设备在线状态(0-不在线，1-在线)
                                "0" -> {
                                    ViewUtils.setVisible(binding.plantOffLine.root)
                                    offLineTextSpan()
                                }
                                "1" -> {
                                    showView(plantFlag, plantGuideFlag)
                                    // 请求未读消息数据，只有在种植之后才会开始有数据返回
                                    mViewMode.getUnread()
                                    checkOtaUpdateInfo()
                                }
                                else -> {}
                            }
                        }
                    } else {
                        // 跳转到绑定设备界面，
                        // 跳转绑定界面
                        ARouter.getInstance()
                            .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            .navigation()
                    }
                }
                error { msg, code ->
                    hideProgressLoading()
                    msg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
                loading {
                    showProgressLoading()
                }
            })

            // 获取图文引导
            getGuideInfo.observe(viewLifecycleOwner, resourceObserver {
                success {
                    // 需要判断当前是什么状态,从而显示是否展示图文通用弹窗
                    // 需要判断当前是否需要称重 判断当前是周期 CURING 7，然后需要判断 flushingWeight == null 或者直接跳转
                    if (mViewMode.popPeriodStatus.value == UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_CURING) {
                        // 如果在解锁During周期的时候。填入了weight，那么在解锁curing时，这个字段不会为null
                        if (mViewMode.plantInfo.value?.data?.flushingWeight == null) {
                            // 弹出解锁弹窗，然后直接跳转种植完成界面
                            pop.asCustom(unlockCuringPop).show()
                            return@success
                        }
                    }

                    hideProgressLoading()
                    // 给弹窗赋值
                    custom?.setData(data)
                    data?.type?.let { mViewMode.setTypeStatus(it) }
                    plantUsually.show()
                }
                error { errorMsg, _ ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                loading { hideProgressLoading() }
            })

            // 上报引导图文引导
            saveOrUpdate.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                loading { showProgressLoading() }

                success {
                    // 上报完成之后的事情
                    // 引导类型:0-种植、1-开始种植、2-开始花期、3-开始清洗期、5-开始烘干期、6-完成种植
                    when (mViewMode.currentReqStatus.value) {
                        1 -> {
                            // 同意plant2之后的弹窗
                            // 上报开始种植之后，UI的变化
                            ViewUtils.setGone(binding.plantFirst.root)
                            ViewUtils.setVisible(binding.plantAddWater.root)
                        }
                        2 -> {
                            // 同意plant5之后的弹窗
                            // plant5后记“2”
                            ViewUtils.setVisible(binding.plantAddWater.clContinue)
                            ViewUtils.setGone(binding.plantAddWater.ivAddWater)
                            plantSix.show()
                        }
                        3 -> {
                            // plant6后记“3”
                            ViewUtils.setGone(binding.plantAddWater.root)
                            ViewUtils.setVisible(binding.plantClone.root)
                        }
                        4 -> {
                            // plant9之后记4
                            ViewUtils.setGone(binding.plantClone.root)
                            ViewUtils.setVisible(binding.pplantNinth.root)
                        }
                    }
                }
            })

            // 开始种植
            startRunning.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    if (mViewMode.unreadMessageList.value.isNullOrEmpty()) {
                        ViewUtils.setGone(binding.pplantNinth.clContinue)
                    }
                    // 获取植物基本信息
                    mViewMode.plantInfo()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

            // 开始种植
            start.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    errorMsg?.let { ToastUtil.show(it) }
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 优先跳转选择种子还是继承界面
                    // seed or clone
                    ARouter.getInstance()
                        .build(RouterPath.My.PAGE_MT_CLONE_SEED)
                        .withString(Constants.Global.KEY_PLANT_ID, data)
                        .navigation(activity, KEY_FOR_CLONE_RESULT)
                }
            })

            // 获取植物基本信息
            plantInfo.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()

                    // 属性名优先
                    if (data?.attribute.isNullOrEmpty()) {
                        ARouter.getInstance()
                            .build(RouterPath.My.PAGE_MT_CLONE_SEED)
                            .withString(Constants.Global.KEY_PLANT_ID, data?.id.toString())
                            .withBoolean(Constants.Global.KEY_USER_NO_ATTRIBUTE, true)
                            .navigation(activity, KEY_FOR_USER_NAME)
                        return@success
                    }

                    // 用来判断当前用户是否拥有名字 or 属性名，如果没有拥有名字，那么直接需要选择
                    if (data?.strainName.isNullOrEmpty()) {
                        pop.isDestroyOnDismiss(false)
                            .isDestroyOnDismiss(false)
                            .asCustom(context?.let {
                                // 显示居中弹窗文案
                                BaseCenterPop(
                                    it,
                                    onConfirmAction = {
                                        ARouter.getInstance()
                                            .build(RouterPath.My.PAGE_MT_CLONE_SEED)
                                            .withString(
                                                Constants.Global.KEY_PLANT_ID,
                                                data?.id.toString()
                                            )
                                            .withBoolean(
                                                Constants.Global.KEY_USER_NO_STRAIN_NAME,
                                                true
                                            )
                                            .navigation(activity, KEY_FOR_USER_NAME)
                                    },
                                    content = getString(com.cl.common_base.R.string.base_no_starin_name_desc),
                                    isShowCancelButton = false
                                )
                            }).show()
                        return@success
                    }

                    //  todo 需要判断当前是seed阶段还是其他阶段，用来显示杯子，还是植物
                    data?.list?.firstOrNull { "${it.journeyStatus}" == HomePeriodPop.KEY_ON_GOING }
                        ?.let { info ->
                            ViewUtils.setVisible(info.journeyName != HomePeriodPop.KEY_SEED, binding.pplantNinth.ivWaterStatus)
                            if (info.journeyName == HomePeriodPop.KEY_SEED) {
                                // 显示种子背景图
                                // 根据总天数判断
                                binding.pplantNinth.ivBowl.background = when (data?.totalDay) {
                                    0, 1 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_one
                                            )
                                        }
                                    }
                                    2 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_two
                                            )
                                        }
                                    }
                                    3 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_three
                                            )
                                        }
                                    }
                                    4, 5 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_four
                                            )
                                        }
                                    }
                                    6, 7 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_five
                                            )
                                        }
                                    }
                                    8, 9 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_six
                                            )
                                        }
                                    }
                                    10, 11 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_seven
                                            )
                                        }
                                    }
                                    12 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_eight
                                            )
                                        }
                                    }
                                    else -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_seed_bg_one
                                            )
                                        }
                                    }
                                }
                            } else {
                                // 树苗的状态
                                // 也是需要根据植物的信息来,需要找到当前的周期
                                var number = 0
                                // 这样看内定不行
                                val week = info.week?.toInt() ?: 0
                                when (info.journeyName) {
                                    "Vegetation" -> {
                                        (if (week > 4) 4 else week
                                                ).also { period -> number = period }
                                    }
                                    "Flowering" -> {
                                        (if (week > 6) 10 else 4 + week).also { period ->
                                            number = period
                                        }
                                    }
                                    "Flushing" -> {
                                        number = 11
                                    }
                                    else -> {
                                        number = 12
                                    }
                                }
                                binding.pplantNinth.ivBowl.background = when (number) {
                                    1 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_one
                                            )
                                        }
                                    }
                                    2 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_two
                                            )
                                        }
                                    }
                                    3 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_three
                                            )
                                        }
                                    }
                                    4 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_four
                                            )
                                        }
                                    }
                                    5 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_five
                                            )
                                        }
                                    }
                                    6 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_six
                                            )
                                        }
                                    }
                                    7 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_seven
                                            )
                                        }
                                    }
                                    8 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_eight
                                            )
                                        }
                                    }
                                    9 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_nine
                                            )
                                        }
                                    }
                                    10 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_ten
                                            )
                                        }
                                    }
                                    11 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_eleven
                                            )
                                        }
                                    }
                                    12 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_twelve
                                            )
                                        }
                                    }
                                    else -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it,
                                                R.mipmap.home_week_one
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    // 植物信息数据显示
                    binding.pplantNinth.tvWeekDay.text = """
                        Week ${data?.week ?: "-"}
                        Day ${data?.day ?: "-"}
                    """.trimIndent()

                    // 植物的氧气
                    binding.pplantNinth.tvOxy.text = "${data?.oxygen ?: "---"}"

                    // 植物的干燥程度
                    binding.pplantNinth.tvHealthStatus.text = data?.healthStatus ?: "----"

                    // 植物的period 周期
                    data?.list?.let {
                        mViewMode.setPeriodList(it)
                    }

                    // 刷新数据
                    if (periodPopDelegate.isShow) {
                        mViewMode.periodData.value?.let { data -> periodPop?.setData(data) }
                    }
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

            // 获取排水的图文广告
            advertising.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                success {
                    context?.let {
                        XPopup.Builder(context)
                            .enableDrag(false)
                            .maxHeight(dp2px(600f))
                            .dismissOnTouchOutside(false)
                            .asCustom(
                                BasePumpWaterPop(
                                    it,
                                    { status ->
                                        // 涂鸦指令，添加排水功能
                                        DeviceControl.get()
                                            .success {
                                                // 气泡任务和右边手动点击通用一个XPopup
                                                // 气泡任务为：
                                                // 主要是针对任务
                                                if (mViewMode.unreadMessageList.value?.first()?.type == UnReadConstants.Device.KEY_CHANGING_WATER) {
                                                    mViewMode.deviceOperateStart(
                                                        business = "${mViewMode.unreadMessageList.value?.first()?.messageId}",
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
                                            .pumpWater(status)
                                    },
                                    onWaterFinishedAction = {
                                        // 排水结束，那么直接弹出
                                        if (plantDrainFinished?.isShow == false) {
                                            plantDrainFinished?.show()
                                        }
                                    },
                                    data = this.data,
                                )
                            ).show()
                    }
                }
            })

            // 获取环境信息
            environmentInfo.observe(viewLifecycleOwner, resourceObserver {
                success {
                    // 弹出环境框
                    data?.let { envirPop?.setData(it) }
                    envirPopDelete.show()
                }
                error { errorMsg, code ->
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

            // 获取未读消息
            getUnread.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    if (!data.isNullOrEmpty()) {
                        // 显示气泡
                        // 这个气泡只有在开始种植之后才会弹出
                        logI(data.toString())
                        logI("size: ${data?.size}")
                        data?.let {
                            mViewMode.setUnreadMessageList(it)
                        }
                    }
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let {
                        ToastUtil.shortShow(it)
                    }
                }
            })

            // 标记当前消息为已读消息
            getRead.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    // 检查下面是否还有气泡弹出
                    checkBubble()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let {
                        ToastUtil.shortShow(it)
                    }
                }
            })

            // 解锁花期回调
            unlockJourney.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    if (mViewMode.popPeriodStatus.value == UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_CURING) {
                        // 跳转解锁完成界面
                        ViewUtils.setGone(binding.pplantNinth.root)
                        ViewUtils.setVisible(binding.plantComplete.root)
                        // 解锁周期之后，清空保存的周期状态
                        mViewMode.setPopPeriodStatus(null)
                        // 种植完成获取参数
                        mViewMode.getFinishPage()
                        return@success
                    }
                    // 解锁周期之后，清空保存的周期状态
                    mViewMode.setPopPeriodStatus(null)

                    hideProgressLoading()
                    // 又重新请求一次咯.主要是为了消失周期上面的红点,其他没啥作用
                    mViewMode.plantInfo()
                    // 看看下面还有没有弹窗，有的话，就继续弹出来
                    checkBubble()
                }
            })

            // 状态上报结束
            // 这个接口目前只有排水结束会调用
            deviceOperateFinish.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    // 排水结束
                    // 开始加水
                    // 标记为第二步骤

                    // 看水位，低于1L就加水
                    // 低于1L就进加水，否则直接进加肥
                    when (mViewMode.getWaterVolume.value) {
                        "0L" -> {
                            // 加水弹窗
                            mViewMode.unreadMessageList.value?.first()?.let {
                                mViewMode.userMessageFlag(
                                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_TWO,
                                    "${it.messageId}"
                                )
                                mViewMode.deviceOperateStart(
                                    "${it.messageId}",
                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_WATER
                                )
                                // 手动修改状态
                                it.extension =
                                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_TWO
                                it.type = UnReadConstants.StatusManager.VALUE_STATUS_ADD_WATER
                            }
                            plantFour.show()
                        }
                        else -> {
                            mViewMode.unreadMessageList.value?.first()?.let {
                                mViewMode.userMessageFlag(
                                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_THREE,
                                    "${it.messageId}"
                                )
                                mViewMode.deviceOperateStart(
                                    "${it.messageId}",
                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_MANURE
                                )
                                mViewMode.deviceOperateStart(
                                    "${it.messageId}",
                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_MANURE
                                )

                                // 手动修改状态
                                it.extension =
                                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_THREE
                                it.type = UnReadConstants.StatusManager.VALUE_STATUS_ADD_MANURE
                            }
                            // 加肥的弹窗
                            plantSix.show()
                        }
                    }

                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

        }
    }

    private fun offLineTextSpan() {
        // 设置当前span文字
        binding.plantOffLine.tvSpan.movementMethod =
            LinkMovementMethod.getInstance() // 设置了才能点击
        binding.plantOffLine.tvSpan.highlightColor =
            ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
                resources,
                com.cl.common_base.R.color.transparent,
                context?.theme
            )
        binding.plantOffLine.tvSpan.text = buildSpannedString {
            appendLine("1.Check if abby is plugged in and turned on")
            appendLine("2.Check your Wi-Fi network connection")
            appendLine("3.Try to power off and restart your abby")
            append("4.If the problem persists, try to ")
            context?.let { context ->
                ContextCompat.getColor(
                    context,
                    com.cl.common_base.R.color.mainColor
                )
            }
                ?.let { color ->
                    color(
                        color
                    ) {
                        appendClickable("Reconnect abby") {
                            // 跳转到ReconnectActivity
                            ARouter.getInstance()
                                .build(RouterPath.PairConnect.KEY_PAIR_RECONNECTING)
                                .navigation()
                        }
                    }
                }
        }
    }

    override fun HomeBinding.initBinding() {
        binding.lifecycleOwner = this@HomeFragment
        binding.viewModel = mViewMode
        binding.executePendingBindings()
    }

    override fun onResume() {
        super.onResume()
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { v, insets ->
            binding.clRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    /**
     * 种植引导
     *
     * plant2
     */
    private fun startGuidePage() {
        // 获取开始种植的图文引导
        mViewMode.getGuideInfo("0")
    }

    /**
     * 未读消息&极光推送
     * 弹窗未读消息UI改变
     */
    private fun changUnReadMessageUI() {
        // 如果是空的，那么不需要改变
        val unReadList = mViewMode.unreadMessageList.value
        if (unReadList.isNullOrEmpty()) {
            ViewUtils.setGone(binding.pplantNinth.clContinue)
            return
        }
        logI("changUnReadMessageUI Coming")

        // 获取第一个
        val unRead = unReadList.first()
        //  判断气泡时间是否过期，如果过期那么就直接不用了。
        //  LocalReceiveTime == null, 表示长期
        if (unRead.LocalReceiveTime?.isNotEmpty() == true) {
            // 发送时间
            val receiveTime = (unRead.LocalReceiveTime?.toLong())?.div(1000L)
            // 当前时间
            val currentTime = System.currentTimeMillis()

            if (currentTime > (receiveTime ?: 0L)) {
                // 如果当前时间大于接受时间，那么表示当前消息需要隐藏，不需要展示
                mViewMode.removeFirstUnreadMessage()
                // 检查下是否还有弹窗需要继续弹。
                checkBubble()
                return
            }
        }

        // 不限时气泡，显示弹窗
        // 故障 显示弹窗
        if (UnReadConstants.malfunction.contains(unRead.type)) {
            customFanFailPop?.setData(unRead.title)
            fanFailPop.show()
            ViewUtils.setGone(binding.pplantNinth.clContinue)
            return
        }


        // 显示气泡
        ViewUtils.setVisible(binding.pplantNinth.clContinue)

        // 按钮
        binding.pplantNinth.tvBtnDesc.text =
            if (UnReadConstants.plantStatus.contains(unRead.type)) {
                "Unlock"
            } else if (unRead.jumpType == UnReadConstants.JumpType.KEY_TREND) {
                "View"
            } else if (UnReadConstants.noCancel.contains(unRead.type) && unRead.extension.isNullOrEmpty()) {
                "Start"
            } else if (unRead.jumpType == UnReadConstants.JumpType.KEY_LEARN_MORE) {
                "Learn More"
            } else if (unRead.extension?.startsWith(UnReadConstants.Extension.KEY_EXTENSION_CONTINUE) == true) {
                "Continue"
            } else {
                ""
            }.toString()

        // 新的气泡如果需要解锁周期那么就需要显示红点
        ViewUtils.setVisible(
            UnReadConstants.plantStatus.contains(unRead.type),
            binding.pplantNinth.ivNewRed
        )

        // 如果jumpType == none 就不显示按钮
        ViewUtils.setVisible(
            unRead.jumpType != UnReadConstants.JumpType.KEY_NONE,
            binding.pplantNinth.tvBtnDesc
        )

        // 内容
        binding.pplantNinth.tvPopTitle.text =
            if (unRead.extension?.contains(UnReadConstants.Extension.KEY_EXTENSION_CONTINUE) == true) {
                "You have Job to do."
            } else {
                unRead.title
            }

        // 是否显示取消按钮
        binding.pplantNinth.ivClose.visibility =
            if (UnReadConstants.noCancel.contains(unRead.type)) View.GONE else View.VISIBLE
    }

    /**
     * 检查下面是否还有弹窗
     * 是不是需要继续弹出
     */
    private fun checkBubble() {
        // list里面如果没有了，那就不需要更改UI了。
        // 如果list还有值，那么继续弹出下一个pop
        val unReadList = mViewMode.unreadMessageList.value
        if (unReadList.isNullOrEmpty()) {
            ViewUtils.setGone(binding.pplantNinth.clContinue)
            return
        }
        // 更改UI
        changUnReadMessageUI()
    }

    // 检查固件是否需要升级
    private fun checkOtaUpdateInfo() {
        mViewMode.checkFirmwareUpdateInfo { upgradeInfoBeans, isShow ->
            if (isShow) {
                upgradeInfoBeans?.firstOrNull { it.type == 9 }?.let {
                    // 只有提醒升级、强制升级时才会弹窗
                    if (it.upgradeType == 2 || it.upgradeType == 0) {
                        updatePop?.setData(it)
                        XPopup.Builder(context)
                            .isDestroyOnDismiss(false)
                            .enableDrag(false)
                            .dismissOnTouchOutside(false)
                            .asCustom(updatePop)
                            .show()
                    }
                }
            }
        }
    }

    /**
     * 设备状态监听变化
     */
    override fun onDeviceChange(status: String) {
        super.onDeviceChange(status)
        when (status) {
            Constants.Device.KEY_DEVICE_OFFLINE -> {
                logI(
                    """
                    deviceStatus: Constants.Device.KEY_DEVICE_OFFLINE
                """.trimIndent()
                )
                if (binding.plantOffLine.root.visibility == View.GONE) {
                    ViewUtils.setVisible(binding.plantOffLine.root)
                }
                offLineTextSpan()
            }
            Constants.Device.KEY_DEVICE_ONLINE -> {
                logI(
                    """
                    deviceStatus: Constants.Device.KEY_DEVICE_ONLINE
                """.trimIndent()
                )
                ViewUtils.setGone(binding.plantOffLine.root)
                // 刷新数据
                mViewMode.refreshToken.value?.data?.let { data ->
                    // 如果时绑定状态，并且是离线，表示是第一次进来，设备就是离线的。
                    // 当他在线时，就需要刷新状态
                    if (data.deviceStatus == "1") {
                        if (data.deviceOnlineStatus == "0") {
                            mViewMode.refreshToken(
                                AutomaticLoginReq(
                                    userName = mViewMode.account,
                                    password = mViewMode.psd,
                                    token = Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN)
                                )
                            )
                        }
                    }
                }

                // todo 其实这个时需要添加的。
                // 刷新数据以及token
                // 一并检查下当前的状态
            }
            Constants.Device.KEY_DEVICE_REMOVE -> {
                logI(
                    """
                    KEY_DEVICE_REMOVE: 
                    device is removed
                """.trimIndent()
                )
            }
        }
    }

    /**
     * 换水、加水、加肥 具体到哪一步
     * 三步
     */
    private fun specificStep() {
        mViewMode.unreadMessageList.value?.first()?.let {
            val extension = it.extension
            if (extension.isNullOrEmpty() || extension == UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_ONE) {
                // 换水弹窗
                // 添加当前排水的步骤 1
                mViewMode.userMessageFlag(
                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_ONE,
                    "${it.messageId}"
                )
                // 表示可以跳过此次三步
                drainagePop?.setData(true)
                plantDrain.show()
            } else if (extension == UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_TWO) {
                // 看水位，低于1L就加水
                // 低于1L就进加水，否则直接进加肥
                when (mViewMode.getWaterVolume.value) {
                    "0L" -> {
                        // 加水弹窗
                        plantFour.show()
                    }
                    else -> {
                        // 加肥的弹窗
                        plantSix.show()
                    }
                }
            } else if (extension == UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_THREE) {
                //  加肥的弹窗
                plantSix.show()
            } else {

            }
        }
    }

    /**
     * 设备指令监听
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

    override fun onDestroy() {
        super.onDestroy()
        // 销毁job倒计时任务
        job?.cancel()
    }

    /**
     * 发送支持邮件
     */
    private fun sendEmail() {
        val uriText = "mailto:growsupport@heyabby.com" + "?subject=" + Uri.encode("Support")
        val uri = Uri.parse(uriText)
        val sendIntent = Intent(Intent.ACTION_SENDTO)
        sendIntent.data = uri
        val pm = context?.packageManager
        // 根据意图查找包
        val activityList = pm?.queryIntentActivities(sendIntent, 0)
        if (activityList?.size == 0) {
            // 弹出框框
            val clipboard =
                context?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
            val clipData = ClipData.newPlainText(null, "growsupport@heyabby.com")
            // 把数据集设置（复制）到剪贴板
            clipboard.setPrimaryClip(clipData)
            XPopup.Builder(context)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(SendEmailTipsPop(context!!) { null }).show()
            return
        }
        try {
            startActivity(Intent.createChooser(sendIntent, "Send email"))
        } catch (ex: ActivityNotFoundException) {
            XPopup.Builder(context)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(context?.let { SendEmailTipsPop(it) { null } }).show()
        }
    }


    /**
     * 跳转选择种子还是继承界面回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                KEY_FOR_CLONE_RESULT -> {
                    // 是否选择了继承
                    val isChooserClone =
                        data?.getBooleanExtra(Constants.Global.KEY_IS_CHOOSE_CLONE, false)
                    val isChooserSeed =
                        data?.getBooleanExtra(Constants.Global.KEY_IS_CHOOSE_SEED, false)
                    // 表示啥也没选
                    if (isChooserClone == false && isChooserSeed == false) {
                        return
                    }
                    if (isChooserClone == true) {
                        // 如果是
                        // 弹出引导图
                        startGuidePage()
                        return
                    }

                    if (isChooserSeed == true) {
                        // 跳转到向导界面、并且展示
                        ARouter.getInstance().build(RouterPath.My.PAGE_MY_GUIDE_SEED)
                            .navigation()
                        // 解锁Seed周期
                        mViewMode.unlockJourney("Seed")
                        // 显示植物种植布局
                        ViewUtils.setVisible(binding.pplantNinth.root)
                        //  todo 这个显示有问题，会重复隐藏
                        ViewUtils.setGone(binding.pplantNinth.clContinue)
                        return
                    }

                }

                KEY_FOR_USER_NAME -> {
                    // 刷新接口
                    val isRefresh =
                        data?.getBooleanExtra(Constants.Global.KEY_REFRESH_PLANT_INFO, false)
                    if (isRefresh == true) {
                        mViewMode.plantInfo()
                    }
                }

            }
        }

    }

    companion object {
        const val KEY_NEW_PLANT = "0"
        const val KEY_PLANTED = "1"
        const val KEY_EXTEND_PLANT = "2"
        const val KEY_PLANTING_COMPLETED = "3"

        // 跳转继承界面的回调
        const val KEY_FOR_CLONE_RESULT = 66

        // 跳转继承界面为了老用户输入属性或者名字
        const val KEY_FOR_USER_NAME = 77
    }
}