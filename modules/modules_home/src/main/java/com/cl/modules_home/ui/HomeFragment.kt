package com.cl.modules_home.ui

import android.app.Activity
import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
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
import com.cl.common_base.bean.AutomaticLoginReq
import com.cl.modules_home.viewmodel.HomeViewModel
import com.cl.modules_home.widget.*
import com.bbgo.module_home.databinding.HomeBinding
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.bean.*
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.easeui.EaseUiHelper
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.help.SeedGuideHelp
import com.cl.common_base.pop.*
import com.cl.common_base.pop.activity.BasePumpActivity
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.util.span.appendClickable
import com.cl.modules_home.adapter.HomeFinishItemAdapter
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import java.io.Serializable
import javax.inject.Inject
import kotlin.random.Random

/**
 * 种植引导Fragment
 * 种植继承
 */
@Suppress("LABEL_NAME_CLASH")
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

    // 传过来的设备状态
    // 默认为false
    private val firstLoginAndNoDevice by lazy {
        arguments?.getBoolean(Constants.Global.KEY_GLOBAL_PLANT_FIRST_LOGIN_AND_NO_DEVICE, false)
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
        mViewMode.userDetail()

        // getAppVersion 检查版本更新
        mViewMode.getAppVersion()

        liveDataObser()

        // 开启定时器，每次20秒刷新未读气泡消息
        job = mViewMode.countDownCoroutines(10 * 6 * 500000, lifecycleScope, onTick = {
            if (it % 30 == 0) {
                // 表示过了30秒
                mViewMode.getUnread()
                // 查询植物信息Look
                mViewMode.plantInfoLoop()
            }
            if (it == 0) {
                job?.cancel()
            }
        }, onStart = {}, onFinish = {
            // todo 这个finish也指的是当前页面被关闭, 定时任务不能放在这个地方.
            job?.cancel()
        })
    }

    /**
     * eventBus消息接收
     */
    private fun liveDataObser() {
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

        /**
         * 环信消息
         */
        LiveEventBus.get().with(Constants.Global.KEY_MAIN_SHOW_BUBBLE, Boolean::class.java)
            .observe(viewLifecycleOwner) {
                if (it) {
                    mViewMode.getEaseUINumber()
                }
            }
    }

    /**
     * 水箱水的状态
     */
    private fun setWaterStatus(value: Any?) {
        binding.pplantNinth.ivWaterStatus.background = when (value) {
            "0L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc, R.mipmap.home_low_water
                    )
                }
            }
            "1L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc, R.mipmap.home_ok_water
                    )
                }
            }
            "2L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc, R.mipmap.home_ok_water
                    )
                }
            }
            "3L" -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc, R.mipmap.home_max_water
                    )
                }
            }
            else -> {
                context?.let { cc ->
                    ContextCompat.getDrawable(
                        cc, R.mipmap.home_low_water
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
            firstLoginAndNoDevice: $firstLoginAndNoDevice
        """.trimIndent()
        )

        if (firstLoginAndNoDevice == true) {
            // 显示出绑定设备界面
            ViewUtils.setVisible(binding.bindDevice.root)
            ViewUtils.setGone(binding.plantOffLine.root)
            ViewUtils.setGone(binding.clRoot)

            // 如果是第一次、也从未绑定过设备、显示出气泡
            ViewUtils.setVisible(firstLoginAndNoDevice == true, binding.bindDevice.clContinue)
            ViewUtils.setVisible(firstLoginAndNoDevice == true, binding.bindDevice.connectDevice)
            ViewUtils.setGone(binding.bindDevice.tvScan, firstLoginAndNoDevice == true)
            return
        }

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
                mViewMode.getEnvData()
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
                mViewMode.checkFirstSubscriber()
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
                if (mViewMode.userDetail.value?.data?.isVip == 1) {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                        .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                } else {
                    // 不是订阅用户，直接弹出图文
                    mViewMode.getGuideInfo("unearned_subscription_explain")
                }
            }

            // 选中学院
            ivAcademy.setOnClickListener {
                // 如果是订阅用户
                if (mViewMode.userDetail.value?.data?.isVip == 1) {
                    context?.startActivity(Intent(context, AcademyActivity::class.java))
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
                // 环境信息弹窗
                envirPopDelete.show()
            }

            // 点击弹窗上面的关闭
            ivClose.setOnClickListener {
                // 标记已读
                if (mViewMode.unreadMessageList.value.isNullOrEmpty()) return@setOnClickListener
                mViewMode.unreadMessageList.value?.let { message ->
                    if (message.size == 0) return@setOnClickListener
                    // 标记已读信息
                    mViewMode.getRead("${message.firstOrNull()?.messageId}")
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

        // 设备绑定界面
        binding.bindDevice.apply {
            knowMore.setOnClickListener {
                // todo 跳转新的图文界面
                val intent = Intent(activity, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_PAGE_NOT_PURCHASED)
                startActivity(intent)
            }
            connectDevice.setOnClickListener {
                //  连接设备
                // todo 删除设备跳转到首页
                checkPer()
            }
            tvScan.setOnClickListener {
                // todo 删除设备跳转到首页
                checkPer()
            }
        }
    }

    /**
     * 添加化肥弹窗
     */
    private val plantFeed by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false).maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomePlantFeedPop(context = it, onNextAction = {
                    // 涂鸦指令，添加化肥
                    DeviceControl.get().success {
                        if (Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_FEET_POP, true)) {
                            pop.isDestroyOnDismiss(false).maxHeight(dp2px(600f)).enableDrag(false)
                                .dismissOnTouchOutside(false).asCustom(
                                    BaseBottomPop(it,
                                        backGround = ContextCompat.getDrawable(
                                            it, com.cl.common_base.R.mipmap.base_feet_fall_bg
                                        ),
                                        text = getString(com.cl.common_base.R.string.base_feet_fall),
                                        buttonText = getString(com.cl.common_base.R.string.base_feet_fall_button_text),
                                        bottomText = getString(com.cl.common_base.R.string.base_dont_show),
                                        onNextAction = {
                                            // 如果是在换水的三步当中的最后一步，加肥
                                            if (mViewMode.getUnreadMessageList()
                                                    .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                                            ) {
                                                // 完成任务
                                                mViewMode.popPeriodStatus.value?.let { map ->
                                                    mViewMode.finishTask(FinishTaskReq(map[HomeViewModel.KEY_TASK_ID]))
                                                }
                                                // 点击按钮就表示已读，已读会自动查看有没有下一条
                                                mViewMode.getRead(
                                                    "${
                                                        mViewMode.getUnreadMessageList()
                                                            .firstOrNull()?.messageId
                                                    }"
                                                )
                                                return@BaseBottomPop
                                            }

                                            // 第六个弹窗
                                            // plant6后记“3”
                                            mViewMode.setCurrentReqStatus(3)
                                            mViewMode.saveOrUpdate("3")
                                        },
                                        bottomTextAction = {
                                            // 如果是在换水的三步当中的最后一步，加肥
                                            if (mViewMode.getUnreadMessageList()
                                                    .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                                            ) {
                                                // 完成任务
                                                mViewMode.popPeriodStatus.value?.let { map ->
                                                    mViewMode.finishTask(FinishTaskReq(map[HomeViewModel.KEY_TASK_ID]))
                                                }
                                                // 点击按钮就表示已读，已读会自动查看有没有下一条
                                                mViewMode.getRead(
                                                    "${
                                                        mViewMode.getUnreadMessageList()
                                                            .firstOrNull()?.messageId
                                                    }"
                                                )
                                                return@BaseBottomPop
                                            }

                                            // 第六个弹窗
                                            // plant6后记“3”
                                            mViewMode.setCurrentReqStatus(3)
                                            mViewMode.saveOrUpdate("3")
                                        })
                                ).show()
                        } else {
                            // 加肥气泡
                            if (mViewMode.getUnreadMessageList()
                                    .firstOrNull()?.type == UnReadConstants.Device.KEY_ADD_MANURE
                            ) {
                                // 完成任务
                                mViewMode.popPeriodStatus.value?.let { map ->
                                    mViewMode.finishTask(FinishTaskReq(map[HomeViewModel.KEY_TASK_ID]))
                                }
                                mViewMode.getRead(
                                    "${
                                        mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                    }"
                                )
                                return@success
                            }
                        }

                    }.error { code, error ->
                        ToastUtil.shortShow(
                            """
                                    feedAbby:
                                    code-> $code
                                    errorMsg-> $error
                                """.trimIndent()
                        )
                    }.feedAbby(true)
                })
            })
    }

    /**
     * 添加排水弹窗
     */
    private val plantDrain by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false).maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false).asCustom(drainagePop)
    }

    /**
     * 排水完成弹窗
     */
    private val plantDrainFinished by lazy {
        context?.let {
            XPopup.Builder(it).isDestroyOnDismiss(false).enableDrag(false).maxHeight(dp2px(600f))
                .dismissOnTouchOutside(false).asCustom(context?.let {
                    BasePumpWaterFinishedPop(it, onSuccessAction = {
                        // 排水成功弹窗，点击OK按钮
                        mViewMode.getUnreadMessageList().firstOrNull()?.let { bean ->
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
    //    private val plantExtendPop by lazy {
    //        XPopup.Builder(context)
    //            .isDestroyOnDismiss(false)
    //            .enableDrag(false)
    //            .maxHeight(dp2px(600f))
    //            .dismissOnTouchOutside(false)
    //            .asCustom(context?.let {
    //                HomePlantExtendPop(
    //                    context = it,
    //                    onNextAction = { status ->
    //                        when (status) {
    //                            HomePlantExtendPop.KEY_NEW_PLANT -> {
    //                                ViewUtils.setGone(binding.plantExtendBg.root)
    //                                ViewUtils.setVisible(binding.plantFirst.root)
    //                            }
    //                            HomePlantExtendPop.KEY_EXTEND -> {
    //                                // 直接跳转到种植界面
    //                                ViewUtils.setGone(binding.plantExtendBg.root)
    //                                ViewUtils.setGone(binding.plantFirst.root)
    //                                ViewUtils.setVisible(binding.pplantNinth.root)
    //                                ViewUtils.setGone(binding.pplantNinth.clContinue)
    //                                // mViewMode.startRunning(null, true)
    //                            }
    //                        }
    //                    }
    //                )
    //            })
    //    }


    // 升级弹窗
    private val updatePop by lazy {
        context?.let {
            FirmwareUpdatePop(it, onConfirmAction = { isBoolean ->
                // 跳转到固件升级界面
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_FIRMWARE_UPDATE)
                    .withBoolean(Constants.Global.KEY_GLOBAL_MANDATORY_UPGRADE, isBoolean)
                    .navigation()
            })
        }
    }


    /**
     * plant4 弹窗
     */
    private val plantFour by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomePlantFourPop(context = it, onNextAction = {
                    plantFive.show()
                })
            })
    }

    /**
     * plant5 弹窗 加水
     */
    private val plantFive by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomePlantFivePop(context = it, onCancelAction = {
                    // 在执行未读消息时，不需要showView
                    if (mViewMode.getUnreadMessageList()
                            .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER || mViewMode.getUnreadMessageList()
                            .firstOrNull()?.type == UnReadConstants.Device.KEY_ADD_WATER
                    ) {
                        return@HomePlantFivePop
                    }

                    // 种植引导时，点击取消弹窗时，处理得事。
                    // 状态改为2，然后
                    // 当作 plantGuideFlag = 2 来处理
                    //                        showView(plantFlag, "2")
                }, onNextAction = {
                    // 如果是在换水的三步当中
                    if (mViewMode.unreadMessageList.value?.isNotEmpty() == true) {
                        if (mViewMode.getUnreadMessageList()
                                .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                        ) {
                            // 弹出加肥
                            mViewMode.getUnreadMessageList().firstOrNull()?.let { bean ->
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
                        if (mViewMode.getUnreadMessageList()
                                .firstOrNull()?.type == UnReadConstants.Device.KEY_ADD_WATER
                        ) {
                            // 任务完成
                            // 完成任务
                            mViewMode.popPeriodStatus.value?.let { map ->
                                mViewMode.finishTask(FinishTaskReq(map[HomeViewModel.KEY_TASK_ID]))
                            }
                            mViewMode.getRead(
                                "${
                                    mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                }"
                            )
                            return@HomePlantFivePop
                        }
                    }

                    // plant5后记“2”
                    mViewMode.setCurrentReqStatus(2)
                    mViewMode.saveOrUpdate("2")
                })
            })
    }

    private val drainagePop by lazy {
        context?.let {
            HomePlantDrainPop(context = it, onNextAction = {
                // 请求接口
                mViewMode.advertising()
            }, onCancelAction = {

            }, onTvSkipAddWaterAction = {
                XPopup.Builder(it).isDestroyOnDismiss(false).enableDrag(false)
                    .maxHeight(dp2px(600f)).dismissOnTouchOutside(false)
                    .asCustom(skipWaterConfirmPop).show()
            })
        }
    }

    /**
     * 确认跳过换水啥的弹窗
     */
    private val skipWaterConfirmPop by lazy {
        context?.let {
            HomeSkipWaterPop(it, onConfirmAction = {
                // 跳过换水、加水、加肥
                mViewMode.getUnreadMessageList().firstOrNull()?.let { bean ->
                    mViewMode.deviceOperateStart(
                        "${bean.messageId}",
                        UnReadConstants.StatusManager.VALUE_STATUS_SKIP_CHANGING_WATERE
                    )
                    // 上报排水结束
                    mViewMode.deviceOperateFinish(UnReadConstants.StatusManager.VALUE_STATUS_PUMP_WATER)
                }
            })
        }
    }

    /**
     * plant6 弹窗 加肥
     */
    private val plantSix by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).maxHeight(dp2px(600f)).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomePlantSixPop(context = it, onNextAction = {
                    // 需要先发送指令喂食
                    DeviceControl.get().success {
                        if (Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_FEET_POP, true)) {
                            pop.isDestroyOnDismiss(false).maxHeight(dp2px(600f)).enableDrag(false)
                                .dismissOnTouchOutside(false).asCustom(
                                    BaseBottomPop(it,
                                        backGround = ContextCompat.getDrawable(
                                            it, com.cl.common_base.R.mipmap.base_feet_fall_bg
                                        ),
                                        text = getString(com.cl.common_base.R.string.base_feet_fall),
                                        buttonText = getString(com.cl.common_base.R.string.base_feet_fall_button_text),
                                        bottomText = getString(com.cl.common_base.R.string.base_dont_show),
                                        onNextAction = {
                                            // 如果是在换水的三步当中的最后一步，加肥
                                            if (mViewMode.getUnreadMessageList()
                                                    .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                                            ) {
                                                // 完成任务
                                                mViewMode.popPeriodStatus.value?.let { map ->
                                                    mViewMode.finishTask(FinishTaskReq(map[HomeViewModel.KEY_TASK_ID]))
                                                }
                                                // 点击按钮就表示已读，已读会自动查看有没有下一条
                                                mViewMode.getRead(
                                                    "${
                                                        mViewMode.getUnreadMessageList()
                                                            .firstOrNull()?.messageId
                                                    }"
                                                )
                                                return@BaseBottomPop
                                            }

                                            // 第六个弹窗
                                            // plant6后记“3”
                                            mViewMode.setCurrentReqStatus(3)
                                            mViewMode.saveOrUpdate("3")
                                        },
                                        bottomTextAction = {
                                            // 如果是在换水的三步当中的最后一步，加肥
                                            if (mViewMode.getUnreadMessageList()
                                                    .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                                            ) {
                                                // 完成任务
                                                mViewMode.popPeriodStatus.value?.let { map ->
                                                    mViewMode.finishTask(FinishTaskReq(map[HomeViewModel.KEY_TASK_ID]))
                                                }
                                                // 点击按钮就表示已读，已读会自动查看有没有下一条
                                                mViewMode.getRead(
                                                    "${
                                                        mViewMode.getUnreadMessageList()
                                                            .firstOrNull()?.messageId
                                                    }"
                                                )
                                                return@BaseBottomPop
                                            }

                                            // 第六个弹窗
                                            // plant6后记“3”
                                            mViewMode.setCurrentReqStatus(3)
                                            mViewMode.saveOrUpdate("3")
                                        })
                                ).show()
                        } else {
                            // 如果是在换水的三步当中的最后一步，加肥
                            if (mViewMode.getUnreadMessageList()
                                    .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                            ) {
                                // 完成任务
                                mViewMode.popPeriodStatus.value?.let { map ->
                                    mViewMode.finishTask(FinishTaskReq(map.get(HomeViewModel.KEY_TASK_ID)))
                                }
                                // 点击按钮就表示已读，已读会自动查看有没有下一条
                                mViewMode.getRead(
                                    "${
                                        mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                    }"
                                )
                                return@success
                            }

                            // 第六个弹窗
                            // plant6后记“3”
                            mViewMode.setCurrentReqStatus(3)
                            mViewMode.saveOrUpdate("3")
                        }

                    }.error { code, error ->
                        ToastUtil.shortShow(
                            """
                                    feedAbby:
                                    code-> $code
                                    errorMsg-> $error
                                """.trimIndent()
                        )
                    }.feedAbby(true)
                })
            })
    }

    /**
     * 通用图文 弹窗
     */
    private val plantUsually by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).maxHeight(dp2px(600f)).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(custom)
    }

    /**
     * 风扇故障弹窗
     */
    private val fanFailPop by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(customFanFailPop)
    }

    private val customFanFailPop by lazy {
        context?.let {
            FanFailPop(it, {
                // todo 风扇故障，跳转环信客服
                // 并且表示已读
                mViewMode.getRead("${mViewMode.getUnreadMessageList().firstOrNull()?.messageId}")
                // todo 暂时不跳环信，跳转邮件转发
                sendEmail()
            }, {
                // 并且表示已读
                mViewMode.getRead("${mViewMode.getUnreadMessageList().firstOrNull()?.messageId}")
            })
        }
    }


    /**
     * 通用弹窗
     * 网络请求
     */
    private val custom by lazy {
        context?.let {
            BasePlantUsuallyGuidePop(context = it, onNextAction = { weight ->
                val unReadList = mViewMode.unreadMessageList.value
                // 应该是判断当前的种植周期。
                if (unReadList.isNullOrEmpty() && mViewMode.popPeriodStatus.value.isNullOrEmpty()) {
                    // 这个是引导阶段
                    /**
                     * 这个状态是自己自定义的状态，主要用于上报到第几步
                     * 上报步骤
                     * 引导阶段
                     */
                    when (mViewMode.typeStatus.value) {
                        "0" -> {
                            // 当前表示开始种植
                            // 上报当前的步骤 1
                            mViewMode.setCurrentReqStatus(1)
                            mViewMode.saveOrUpdate("1")
                        }
                        "1" -> {
                            // 这是第9个弹窗，开始种植，需要传入步骤为 4
                            mViewMode.setCurrentReqStatus(4)
                            mViewMode.saveOrUpdate("4")
                        }
                        else -> {}
                    }
                    return@BasePlantUsuallyGuidePop
                }

                // 如果是从解锁周期弹窗过来的，
                if (!mViewMode.popPeriodStatus.value.isNullOrEmpty()) {
                    val taskType = mViewMode.popPeriodStatus.value?.get(HomeViewModel.KEY_GUIDE_ID)
                    val taskId = mViewMode.popPeriodStatus.value?.get(HomeViewModel.KEY_TASK_ID)

                    // 判断当前的解锁周期是否需要添加新的引导图
                    // seed -> to veg 周期解锁
                    if (taskType == UnReadConstants.PlantStatus.TASK_TYPE_CHECK_TRANSPLANT) {
                        context?.let { context ->
                            SeedGuideHelp(context).showGuidePop {
                                mViewMode.popPeriodStatus.value?.let { map ->
                                    mViewMode.finishTask(FinishTaskReq(taskId, weight))
                                }
                            }
                        }
                        return@BasePlantUsuallyGuidePop
                    }

                    // 解锁接口
                    mViewMode.finishTask(FinishTaskReq(taskId, weight))
                }
            },
                // 是否展示提醒周期文案，那么只根据taskTime是否为空，来展示，目目前只用到seed to veg 才会有taskTime
                isShowRemindMe = mViewMode.popPeriodStatus.value?.get(HomeViewModel.KEY_TASK_TIME)
                    ?.isNotEmpty(), onRemindMeAction = {
                    //  推迟两天执行
                    val taskTime = mViewMode.popPeriodStatus.value?.get(HomeViewModel.KEY_TASK_TIME)
                    val taskId = mViewMode.popPeriodStatus.value?.get(HomeViewModel.KEY_TASK_ID)
                    // 60 * 60 * 1000 = 1小时
                    taskTime?.let {
                        // 这个只有出现在气泡、气泡会带时间
                        kotlin.runCatching {
                            mViewMode.updateTask(
                                UpdateReq(
                                    taskId = taskId,
                                    taskTime = "${it.toLong() + 60 * 60 * 1000 * 48}"
                                )
                            )
                        }
                    }
                })
        }
    }

    /**
     * plant8 弹窗
     */
    private val plantEight by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomePlantEightPop(context = it, onNextAction = {
                    // 跳转到第9个弹窗
                    // 开始种植 传入1
                    mViewMode.getGuideInfo("1")
                })
            })
    }

    /**
     * 这是画的周期弹窗
     */
    private val periodPopDelegate by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(true)
            .dismissOnTouchOutside(true).asCustom(periodPop)

    }

    // 这是周期弹窗解锁周期的
    private val periodPop by lazy {
        context?.let {
            HomePeriodPop(it, unLockAction = { guideType, taskId, lastOneGuideType, taskTime ->
                /*if (lastOneGuideType == UnReadConstants.PlantStatus.TASK_TYPE_CHECK_TRANSPLANT) {
                    // 表示是seed to veg
                    SeedGuideHelp(it).showGuidePop {
                        mViewMode.setPopPeriodStatus(guideId = guideType, taskId = taskId, taskTime = null)
                    }
                    return@HomePeriodPop
                }*/
                // 判断是否是Vip、如果是Vip那么就直接跳转到日历。反之就主页解锁
                if (mViewMode.userDetail.value?.data?.isVip == 1) {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                        .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                    return@HomePeriodPop
                }
                // todo 此处是用于周期弹窗解锁的
                mViewMode.setPopPeriodStatus(
                    guideId = guideType, taskId = taskId, taskTime = taskTime
                )
            })
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
            HomeUnlockCuringPop(it, { status ->
                // 直接解锁
                mViewMode.finishTask(FinishTaskReq(taskId = status))
            })
        }
    }

    /**
     * 环境弹窗
     */
    private val envirPopDelete by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(true)
            .dismissOnTouchOutside(true).asCustom(envirPop)
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
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(versionPop)
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
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomeRepairSnPop(it) {
                    // 跳转到找到SN码的地方
                    ARouter.getInstance().build(RouterPath.PairConnect.PAGE_SCAN_CODE).navigation()
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
            // 检查是否是第一次激活订阅码
            checkFirstSubscriber.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                    /*// 这个检查接口报错、那么就知道跳转到选择界面
                    // 需要去引导开启订阅
                    mViewMode.start()*/
                }
                success {
                    hideProgressLoading()
                    if (data == true) {
                        pop.asCustom(
                            context?.let {
                                BaseCenterPop(
                                    it,
                                    onConfirmAction = {
                                        // 需要去引导开启订阅
                                        mViewMode.startSubscriber()
                                    },
                                    onCancelAction = {
                                    },
                                    spannedString =
                                    buildSpannedString {
                                        // Add the subscription to the account
                                        // dee@baypac.com
                                        // After the confirmation, the time of the digital service will be counted from now on
                                        bold { append("Add the subscription to the account") }
                                        appendLine()
                                        color(it.getColor(com.cl.common_base.R.color.mainColor)) {
                                            bold {
                                                appendLine("${mViewMode.refreshToken.value?.data?.email}")
                                            }
                                        }
                                        bold { append("After the confirmation, the time of the digital service will be counted from now on") }
                                    }
                                )
                            }
                        ).show()
                    } else {
                        // 需要去引导开启订阅
                        mViewMode.start()
                    }
                }
            })

            // 开始订阅
            startSubscriber.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 需要去引导开启订阅
                    mViewMode.start()
                }
            })

            // 首页循环刷新消息
            /*userDetail.observe(viewLifecycleOwner, resourceObserver {
                success {
                    if (null == data) return@success
                    // "pump_water_finished":false
                    LiveEventBus.get().with(Constants.APP.KEY_IN_APP, String::class.java)
                        .postEvent(
                            GSON.toJson(hashMapOf(Constants.APP.KEY_IN_APP_VIP to "${data?.isVip}"))
                        )
                }
            })*/
            // 消息统计
            getHomePageNumber.observe(viewLifecycleOwner, resourceObserver {
                loading { }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                }
            })

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
                    mViewMode.clearPopPeriodStatus()

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
                        pop.isDestroyOnDismiss(false).enableDrag(true).maxHeight(dp2px(700f))
                            .dismissOnTouchOutside(false).asCustom(plantFinishUsuallyPop).show()
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
                    pop.isDestroyOnDismiss(false).enableDrag(true).maxHeight(dp2px(700f))
                        .dismissOnTouchOutside(false).asCustom(plantFinishUsuallyPop).show()
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
            // 气泡点击事件
            // 气泡按钮点击事件
            bubbleOnClickEvent.observe(viewLifecycleOwner) { clickEvent ->
                if (clickEvent == false) return@observe
                // 自定义开始种植弹窗
                // 判断点击时长
                if (mViewMode.unreadMessageList.value.isNullOrEmpty()) {
                    // 解锁第一个周期
                    // 显示startRunning气泡的时候，必定是发芽了的
                    // todo 这个地方直接调用startRunning接口了。
                    mViewMode.startRunning(botanyId = "", goon = false)
                    return@observe
                }

                // 未读消息弹窗，获取极光消息弹窗
                // 极光消息来了，只需要把这个消息添加到当前list就好了。
                mViewMode.unreadMessageList.value?.let {
                    // 调用图文信息
                    if (it.size == 0) return@let
                    // How to do LTS
                    mViewMode.getUnreadMessageList().firstOrNull()?.jumpType?.let { jumpType ->
                        if (jumpType == UnReadConstants.JumpType.KEY_LEARN_MORE) {
                            // 单独处理， 弹窗
                            mViewMode.getMessageDetail(
                                "${
                                    mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                }"
                            )
                            mViewMode.getRead(
                                "${
                                    mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                }"
                            )
                            return@observe
                        }
                    }

                    // 状态赋值
                    mViewMode.getUnreadMessageList().firstOrNull()?.type?.let { type ->
                        // 目前只处理了种植状态
                        // 周期的解锁
                        // 气泡解锁
                        // 判断是否是Vip、如果是Vip那么就直接跳转到日历。反之就主页解锁
                        if (mViewMode.userDetail.value?.data?.isVip == 1) {
                            ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                                .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                            return@observe
                        }
                        mViewMode.setPopPeriodStatus(
                            guideId = type,
                            taskId = mViewMode.getUnreadMessageList().firstOrNull()?.taskId,
                            taskTime = mViewMode.getUnreadMessageList().firstOrNull()?.taskTime
                        )

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
                        }
                    }

                }
            }

            // 未读消息
            unreadMessageList.observe(viewLifecycleOwner) {
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
            })

            // 更新信息,刷新token
            refreshToken.observe(viewLifecycleOwner, resourceObserver {
                success {
                    hideProgressLoading()

                    // 环信消息
                    getEaseUINumber()

                    // 保存刷新token信息
                    GSON.toJson(data)?.let { data ->
                        Prefs.putStringAsync(Constants.Login.KEY_REFRESH_LOGIN_DATA, data)
                    }

                    // 保存当前的信息.
                    GSON.toJson(data)?.let {
                        logI("refreshToken: $it")
                        Prefs.putStringAsync(Constants.Login.KEY_LOGIN_DATA, it)
                    }
                    // 保存Token
                    data?.token?.let { it ->
                        Prefs.putStringAsync(
                            Constants.Login.KEY_LOGIN_DATA_TOKEN, it
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
                        // 显示出绑定设备界面
                        ViewUtils.setVisible(binding.bindDevice.root)
                        ViewUtils.setGone(binding.plantOffLine.root)
                        ViewUtils.setGone(binding.clRoot)

                        // 如果是第一次、也从未绑定过设备、显示出气泡
                        ViewUtils.setVisible(data?.notBound == 0, binding.bindDevice.clContinue)
                        ViewUtils.setVisible(data?.notBound == 0, binding.bindDevice.connectDevice)
                        ViewUtils.setGone(binding.bindDevice.tvScan, data?.notBound == 0)
                        // 跳转到绑定设备界面，
                        // 跳转绑定界面
                        //                        ARouter.getInstance()
                        //                            .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                        //                            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        //                            .navigation()
                    }
                }
                error { msg, code ->
                    hideProgressLoading()
                    msg?.let { it1 -> ToastUtil.shortShow(it1) }
                    // 容错处理、不管接口报错都显示
                    showView(plantFlag, plantGuideFlag)
                    // 请求未读消息数据，只有在种植之后才会开始有数据返回
                    mViewMode.getUnread()
                    checkOtaUpdateInfo()
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
                    if (mViewMode.popPeriodStatus.value?.containsValue(UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_CURING) == true) {
                        // 如果在解锁During周期的时候。填入了weight，那么在解锁curing时，这个字段不会为null
                        if ((mViewMode.plantInfo.value?.data?.flushingWeight ?: 0) <= 0) {
                            // 弹出解锁弹窗，然后直接跳转种植完成界面
                            mViewMode.popPeriodStatus.value?.let {
                                mViewMode.popPeriodStatus.value?.get(HomeViewModel.KEY_TASK_ID)
                                    ?.let { taskId -> mViewMode.finishTask(FinishTaskReq(taskId = taskId)) }
                            }
                            return@success
                        }
                    }

                    hideProgressLoading()
                    // 给弹窗赋值
                    custom?.setData(data)
                    mViewMode.popPeriodStatus.value?.get(HomeViewModel.KEY_TASK_TIME)?.isNotEmpty()
                        ?.let { custom?.setIsRemind(it) }
                    // todo 暂时不用引导上报
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
                            mViewMode.startRunning(botanyId = "", goon = false)
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

                    // 发送植物种植消息、主要用来判断消息小红点是否展示
                    LiveEventBus.get().with(Constants.APP.KEY_IN_APP, String::class.java)
                        .postEvent(
                            GSON.toJson(hashMapOf(Constants.APP.KEY_IN_APP_START_RUNNING to "true"))
                        )
                }
                error { errorMsg, _ ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

            // 开始种植
            start.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, _ ->
                    errorMsg?.let { ToastUtil.show(it) }
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 优先跳转选择种子还是继承界面
                    // seed or clone
                    ARouter.getInstance().build(RouterPath.My.PAGE_MT_CLONE_SEED)
                        .withString(Constants.Global.KEY_PLANT_ID, data)
                        .withBoolean(Constants.Global.KEY_IS_SHOW_CHOOSER_TIPS, true)
                        .navigation(activity, KEY_FOR_CLONE_RESULT)
                }
            })

            // 获取植物基本信息
            plantInfo.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // 属性名优先
                    if (data?.attribute.isNullOrEmpty()) {
                        ARouter.getInstance().build(RouterPath.My.PAGE_MT_CLONE_SEED)
                            .withString(Constants.Global.KEY_PLANT_ID, data?.id.toString())
                            .withBoolean(Constants.Global.KEY_USER_NO_ATTRIBUTE, true)
                            .navigation(activity, KEY_FOR_USER_NAME)
                        return@success
                    }

                    // 用来判断当前用户是否拥有名字 or 属性名，如果没有拥有名字，那么直接需要选择
                    if (data?.strainName.isNullOrEmpty()) {
                        pop.isDestroyOnDismiss(false).isDestroyOnDismiss(false)
                            .asCustom(context?.let {
                                // 显示居中弹窗文案
                                BaseCenterPop(
                                    it,
                                    onConfirmAction = {
                                        ARouter.getInstance()
                                            .build(RouterPath.My.PAGE_MT_CLONE_SEED).withString(
                                                Constants.Global.KEY_PLANT_ID, data?.id.toString()
                                            ).withBoolean(
                                                Constants.Global.KEY_USER_NO_STRAIN_NAME, true
                                            ).navigation(activity, KEY_FOR_USER_NAME)
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
                            // 植物信息数据显示
                            binding.pplantNinth.tvWeekDay.text = """
                                ${
                                if (info.journeyName == UnReadConstants.PeriodStatus.KEY_AUTOFLOWERING) getString(
                                    com.cl.common_base.R.string.base_autoflowering_abbreviations
                                ) else info.journeyName
                            }
                                Week ${data?.week ?: "-"}
                                Day ${data?.day ?: "-"}
                            """.trimIndent()
                            ViewUtils.setVisible(
                                info.journeyName != HomePeriodPop.KEY_SEED,
                                binding.pplantNinth.ivWaterStatus
                            )
                            if (info.journeyName == UnReadConstants.PeriodStatus.KEY_SEED || info.journeyName == UnReadConstants.PeriodStatus.KEY_GERMINATION) {
                                // 显示种子背景图
                                // 根据总天数判断
                                binding.pplantNinth.ivBowl.background = when (info.totalDay) {
                                    0, 1 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_one
                                            )
                                        }
                                    }
                                    2 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_two
                                            )
                                        }
                                    }
                                    3 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_three
                                            )
                                        }
                                    }
                                    4, 5 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_four
                                            )
                                        }
                                    }
                                    6, 7 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_five
                                            )
                                        }
                                    }
                                    8, 9 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_six
                                            )
                                        }
                                    }
                                    10, 11 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_seven
                                            )
                                        }
                                    }
                                    12 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_eight
                                            )
                                        }
                                    }
                                    else -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_seed_bg_eight
                                            )
                                        }
                                    }
                                }
                            } else {
                                // 树苗的状态
                                // 也是需要根据植物的信息来,需要找到当前的周期
                                var number: Int = 1

                                // 植物周期状态
                                when (info.journeyName) {
                                    UnReadConstants.PeriodStatus.KEY_VEGETATION -> {
                                        number = if ((info.totalDay ?: 0) in 1..7) {
                                            1
                                        } else if ((info.totalDay ?: 0) in 8..14) {
                                            2
                                        } else if ((info.totalDay ?: 0) in 15..21) {
                                            3
                                        } else {
                                            4
                                        }
                                    }
                                    UnReadConstants.PeriodStatus.KEY_FLOWERING -> {
                                        number = if ((info.totalDay ?: 0) in 1..7) {
                                            5
                                        } else if ((info.totalDay ?: 0) in 8..14) {
                                            6
                                        } else if ((info.totalDay ?: 0) in 15..21) {
                                            7
                                        } else if ((info.totalDay ?: 0) in 22..28) {
                                            8
                                        } else if ((info.totalDay ?: 0) in 29..35) {
                                            9
                                        } else if ((info.totalDay ?: 0) in 36..42) {
                                            10
                                        } else if ((info.totalDay ?: 0) in 43..49) {
                                            11
                                        } else {
                                            12
                                        }
                                    }
                                    UnReadConstants.PeriodStatus.KEY_AUTOFLOWERING -> {
                                        // Photo （seed & Clone） 没有这个周期
                                        // Auto才会有这个周期
                                        number = if ((info.totalDay ?: 0) in 1..6) {
                                            1
                                        } else if ((info.totalDay ?: 0) in 7..12) {
                                            2
                                        } else if ((info.totalDay ?: 0) in 13..18) {
                                            3
                                        } else if ((info.totalDay ?: 0) in 19..24) {
                                            4
                                        } else if ((info.totalDay ?: 0) in 25..30) {
                                            5
                                        } else if ((info.totalDay ?: 0) in 31..36) {
                                            6
                                        } else if ((info.totalDay ?: 0) in 37..42) {
                                            7
                                        } else if ((info.totalDay ?: 0) in 43..48) {
                                            8
                                        } else if ((info.totalDay ?: 0) in 49..54) {
                                            9
                                        } else if ((info.totalDay ?: 0) in 55..60) {
                                            10
                                        } else {
                                            11
                                        }
                                    }
                                    UnReadConstants.PeriodStatus.KEY_FLUSHING -> {
                                        number = 12
                                    }
                                    UnReadConstants.PeriodStatus.KEY_HARVEST -> {
                                        number = 12
                                    }
                                    UnReadConstants.PeriodStatus.KEY_DRYING -> {
                                        number = 12
                                    }
                                    UnReadConstants.PeriodStatus.KEY_CURING -> {
                                        number = 12
                                    }
                                    else -> {
                                        number = 12
                                    }
                                }

                                binding.pplantNinth.ivBowl.background = when (number) {
                                    1 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_one
                                            )
                                        }
                                    }
                                    2 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_two
                                            )
                                        }
                                    }
                                    3 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_three
                                            )
                                        }
                                    }
                                    4 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_four
                                            )
                                        }
                                    }
                                    5 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_five
                                            )
                                        }
                                    }
                                    6 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_six
                                            )
                                        }
                                    }
                                    7 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_seven
                                            )
                                        }
                                    }
                                    8 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_eight
                                            )
                                        }
                                    }
                                    9 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_nine
                                            )
                                        }
                                    }
                                    10 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_ten
                                            )
                                        }
                                    }
                                    11 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_eleven
                                            )
                                        }
                                    }
                                    12 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_twelve
                                            )
                                        }
                                    }
                                    else -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, R.mipmap.home_week_one
                                            )
                                        }
                                    }
                                }

                                logI(
                                    """
                                    number::::
                                $number """
                                )
                            }
                        }

                    // 植物的氧气
                    binding.pplantNinth.tvOxy.text = "${data?.oxygen ?: "---"}"

                    // 植物的健康程度
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
                error { errorMsg, _ ->
                    hideProgressLoading()
                    /*errorMsg?.let { ToastUtil.shortShow(it) }*/
                }
            })

            // 获取排水的图文广告
            advertising.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, _ ->
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                success {
                    context?.let {
                        android.os.Handler().postDelayed({
                            // 传递的数据为空
                            val intent = Intent(it, BasePumpActivity::class.java)
                            intent.putExtra(BasePumpActivity.KEY_DATA, data as? Serializable)
                            intent.putExtra(
                                BasePumpActivity.KEY_UNREAD_MESSAGE_DATA,
                                mViewMode.getUnreadMessageList() as? Serializable
                            )
                            myActivityLauncher.launch(intent)
                        }, 50)
                        /*XPopup.Builder(context)
                            .enableDrag(false)
                            .maxHeight(dp2px(700f))
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
                                                if (mViewMode.getUnreadMessageList()
                                                        .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER
                                                ) {
                                                    mViewMode.deviceOperateStart(
                                                        business = "${mViewMode.getUnreadMessageList().firstOrNull()?.messageId}",
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
                            ).show()*/
                    }
                }
            })

            // 获取环境信息
            environmentInfo.observe(viewLifecycleOwner, resourceObserver {
                success {
                    // 弹出环境框
                    data?.let { it.environments?.let { it1 -> envirPop?.setData(it1) } }
                }
                error { errorMsg, _ ->
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

            // 获取未读消息
            getUnread.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    // 气泡会消失掉，需要防止当前点了之后，这是状态之后，气泡消失掉了，但是数据还保存着, 查找当前任务不在了，那么直接消失掉
                    if (data?.firstOrNull {
                            it.taskId == mViewMode.popPeriodStatus.value?.get(
                                HomeViewModel.KEY_TASK_ID
                            )
                        } == null) mViewMode.clearPopPeriodStatus()

                    // 显示气泡
                    // 这个气泡只有在开始种植之后才会弹出
                    logI(data.toString())
                    logI("size: ${data?.size}")
                    data?.let {
                        mViewMode.setUnreadMessageList(it)
                    }
                }
                error { errorMsg, _ ->
                    hideProgressLoading()
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
                error { errorMsg, _ ->
                    hideProgressLoading()
                    errorMsg?.let {
                        ToastUtil.shortShow(it)
                    }
                }
            })

            // 更新任务回调
            updateTask.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, _ ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 解锁周期之后，清空保存的周期状态
                    mViewMode.clearPopPeriodStatus()

                    hideProgressLoading()
                    // 又重新请求一次咯.主要是为了消失周期上面的红点,其他没啥作用
                    mViewMode.plantInfo()
                    // 看看下面还有没有弹窗，有的话，就继续弹出来
                    checkBubble()
                }
            })

            // 解锁花期回调
            finishTask.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, _ ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    // 解锁完成之后的、弹出图文
                   /* mViewMode.popPeriodStatus.value?.let { map ->
                        val intent = Intent(context, BasePopActivity::class.java)
                        map.forEach {
                            when (it.key) {
                                UnReadConstants.PlantStatus.TASK_TYPE_CHECK_TRANSPLANT -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_ABOUT_CHECK_FLOWERING)
                                    startActivity(intent)
                                }
                                UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_FLOWERING -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_ABOUT_CHECK_FLUSHING)
                                    startActivity(intent)
                                }
                                UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_FLUSHING -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_ABOUT_CHECK_DRYING)
                                    startActivity(intent)
                                }
                                UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_DRYING -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_ABOUT_CHECK_CURING)
                                    startActivity(intent)
                                }
                                UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_CURING -> {
                                    //                            intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_ABOUT_CHECK_CURING)
                                    //                            startActivity(intent)
                                }
                                UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_AUTOFLOWERING -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_ABOUT_CHECK_FLOWERING)
                                    startActivity(intent)
                                }
                            }
                        }
                    }*/

                    if (mViewMode.popPeriodStatus.value?.containsValue(UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_CURING) == true) {
                        showCompletePage()
                        return@success
                    }
                    // 解锁周期之后，清空保存的周期状态
                    mViewMode.clearPopPeriodStatus()

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
                            mViewMode.getUnreadMessageList().firstOrNull()?.let {
                                mViewMode.userMessageFlag(
                                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_TWO,
                                    "${it.messageId}"
                                )
                                mViewMode.deviceOperateStart(
                                    "${it.messageId}",
                                    UnReadConstants.StatusManager.VALUE_STATUS_ADD_WATER
                                )
                                // 手动修改状态
                                it.extension = UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_TWO
                                it.type = UnReadConstants.StatusManager.VALUE_STATUS_ADD_WATER
                            }
                            plantFour.show()
                        }
                        else -> {
                            mViewMode.getUnreadMessageList().firstOrNull()?.let {
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

    /**
     * 展示完成界面逻辑
     */
    private fun showCompletePage() {
        // 跳转解锁完成界面
        ViewUtils.setGone(binding.pplantNinth.root)
        ViewUtils.setVisible(binding.plantComplete.root)
        // 解锁周期之后，清空保存的周期状态
        mViewMode.clearPopPeriodStatus()
        // 种植完成获取参数
        mViewMode.getFinishPage()
    }

    private fun offLineTextSpan() {
        // 设置当前span文字
        binding.plantOffLine.tvSpan.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
        binding.plantOffLine.tvSpan.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
            resources, com.cl.common_base.R.color.transparent, context?.theme
        )
        binding.plantOffLine.tvSpan.text = buildSpannedString {
            appendLine("1.Check if abby is plugged in and turned on")
            appendLine("2.Check your Wi-Fi network connection")
            appendLine("3.Try to power off and restart your abby")
            append("4.If the problem persists, try to ")
            context?.let { context ->
                ContextCompat.getColor(
                    context, com.cl.common_base.R.color.mainColor
                )
            }?.let { color ->
                color(
                    color
                ) {
                    appendClickable("Reconnect abby") {
                        // 跳转到ReconnectActivity

                        ARouter.getInstance().build(RouterPath.PairConnect.KEY_PAIR_RECONNECTING)
                            .navigation()
                    }
                }
            }
        }
    }

    override fun HomeBinding.initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = mViewMode
        binding.executePendingBindings()
    }

    override fun onResume() {
        super.onResume()
        // 从聊天退出来之后需要刷新消息环信数量
        mViewMode.getHomePageNumber()
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
        val unRead = unReadList.firstOrNull()
        //  判断气泡时间是否过期，如果过期那么就直接不用了。
        //  LocalReceiveTime == null, 表示长期
        if (unRead?.LocalReceiveTime?.isNotEmpty() == true) {
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
        if (UnReadConstants.malfunction.contains(unRead?.type)) {
            customFanFailPop?.setData(unRead?.title)
            fanFailPop.show()
            ViewUtils.setGone(binding.pplantNinth.clContinue)
            return
        }


        // 显示气泡
        ViewUtils.setVisible(binding.pplantNinth.clContinue)

        // 按钮
        binding.pplantNinth.tvBtnDesc.text =
            if (UnReadConstants.plantStatus.contains(unRead?.type)) {
                "Unlock"
            } else if (unRead?.jumpType == UnReadConstants.JumpType.KEY_TREND) {
                "View"
            } else if (UnReadConstants.noCancel.contains(unRead?.type) && unRead?.extension.isNullOrEmpty()) {
                "Start"
            } else if (unRead?.jumpType == UnReadConstants.JumpType.KEY_LEARN_MORE) {
                "Learn More"
            } else if (unRead?.extension?.startsWith(UnReadConstants.Extension.KEY_EXTENSION_CONTINUE) == true) {
                "Continue"
            } else if (unRead?.jumpType == UnReadConstants.JumpType.KEY_GUIDE) {
                "Start"
            } else {
                ""
            }.toString()

        // 新的气泡如果需要解锁周期那么就需要显示红点
        // 隐藏周期右上角红点
        /*ViewUtils.setVisible(
            UnReadConstants.plantStatus.contains(unRead?.type), binding.pplantNinth.ivNewRed
        )*/

        // 如果jumpType == none 就不显示按钮
        ViewUtils.setVisible(
            unRead?.jumpType != UnReadConstants.JumpType.KEY_NONE, binding.pplantNinth.tvBtnDesc
        )

        // 内容
        binding.pplantNinth.tvPopTitle.text =
            if (unRead?.extension?.contains(UnReadConstants.Extension.KEY_EXTENSION_CONTINUE) == true) {
                "You have Job to do."
            } else {
                unRead?.title
            }

        // 是否显示取消按钮
        binding.pplantNinth.ivClose.visibility =
            if (UnReadConstants.noCancel.contains(unRead?.type)) View.GONE else View.VISIBLE
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
                        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
                            .dismissOnTouchOutside(false).asCustom(updatePop).show()
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
        mViewMode.getUnreadMessageList().firstOrNull()?.let {
            val extension = it.extension
            if (extension.isNullOrEmpty() || extension == UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_ONE) {
                // 换水弹窗
                // 添加当前排水的步骤 1
                mViewMode.userMessageFlag(
                    UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_ONE, "${it.messageId}"
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
                logI("specificStep")
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
                // 返回多少升水
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_STATUS_INSTRUCTIONS -> {
                    logI("KEY_DEVICE_WATER_STATUS： $value")
                    mViewMode.setWaterVolume(value.toString())
                    mViewMode.tuYaDps?.put(TuYaDeviceConstants.KEY_DEVICE_WATER_LEVEL, value.toString())
                }

                // 排水结束
                TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                }

                // SN修复的通知
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_SN_INSTRUCTION -> {
                    logI("KEY_DEVICE_REPAIR_SN： $value")
                }

                // ----- 开始， 下面的都是需要传给后台的环境信息
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_BRIGHT_VALUE_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE, value.toString())
                }
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_HUMIDITY_CURRENT_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(TuYaDeviceConstants.KEY_DEVICE_HUMIDITY_CURRENT, value.toString())
                }
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_INPUT_AIR_FLOW_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(TuYaDeviceConstants.KEY_DEVICE_INPUT_AIR_FLOW, value.toString())
                }
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_TEMP_CURRENT_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(TuYaDeviceConstants.KEY_DEVICE_TEMP_CURRENT, value.toString())
                }
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_VENTILATION_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(TuYaDeviceConstants.KEY_DEVICE_VENTILATION, value.toString())
                }
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_TEMPERATURE_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(TuYaDeviceConstants.KEY_DEVICE_WATER_TEMPERATURE, value.toString())
                }
                // --------- 到这里结束
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
        /*val uriText = "mailto:growsupport@heyabby.com" + "?subject=" + Uri.encode("Support")
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
                .asCustom(SendEmailTipsPop(context!!)).show()
            return
        }
        try {
            startActivity(Intent.createChooser(sendIntent, "Send email"))
        } catch (ex: ActivityNotFoundException) {
            XPopup.Builder(context)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(context?.let { SendEmailTipsPop(it) }).show()
        }*/
        EaseUiHelper.getInstance().startChat(null)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            // 在线、并且绑定了设备
            if (mViewMode.refreshToken.value?.data?.deviceStatus == "1" && mViewMode.refreshToken.value?.data?.deviceOnlineStatus == "1") {
                // 如果没有绑定过设备
                if (plantFlag != KEY_NEW_PLANT) {
                    // 种植过的才可以请求
                    mViewMode.plantInfo()
                    // 环境信息
                    mViewMode.getEnvData()
                }
                // 获取用户信息
                mViewMode.userDetail()
            }
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
                        ARouter.getInstance().build(RouterPath.My.PAGE_MY_GUIDE_SEED).navigation()
                        // 解锁Seed周期
                        mViewMode.startRunning(botanyId = "", goon = false)
                        // 显示植物种植布局
                        ViewUtils.setGone(binding.plantFirst.root)
                        ViewUtils.setGone(binding.pplantNinth.clContinue)
                        ViewUtils.setVisible(binding.pplantNinth.root)
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

                // 日历界面返回刷新
                KEY_FOR_CALENDAR_REFRSH -> {
                    // 是否展示种植完成界面
                    val isShowCompletePage =
                        data?.getBooleanExtra(Constants.Global.KEY_IS_SHOW_COMPLETE, false)
                    if (isShowCompletePage == true) {
                        showCompletePage()
                        return
                    }
                    mViewMode.getUnread()
                    mViewMode.plantInfo()
                }

            }
        }

    }

    private fun checkPer() {
        activity?.let {
            PermissionHelp().checkConnectForTuYaBle(it,
                object : PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        if (!result) return
                        // 如果权限都已经同意了
                        ARouter.getInstance().build(RouterPath.PairConnect.PAGE_PLANT_SCAN)
                            .navigation()
                    }
                })
        }
    }

    /**
     * 排水界面结束回调
     */
    private val myActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                // 排水结束，那么直接弹出
                if (plantDrainFinished?.isShow == false) {
                    plantDrainFinished?.show()
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

        // 为了刷新的回调
        const val KEY_FOR_CALENDAR_REFRSH = 88

        // 跳转继承界面为了老用户输入属性或者名字
        const val KEY_FOR_USER_NAME = 77
    }
}