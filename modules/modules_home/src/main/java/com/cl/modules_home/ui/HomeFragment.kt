package com.cl.modules_home.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeBinding
import com.bbgo.module_home.databinding.HomeItemPlantManualBinding
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.ext.*
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.pop.*
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.pop.activity.BasePumpActivity
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.web.VideoPLayActivity
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.common_base.widget.waterview.bean.Water
import com.cl.common_base.widget.waterview.widget.WaterView
import com.cl.modules_home.activity.HomeNewPlantNameActivity
import com.cl.modules_home.adapter.HomeFinishItemAdapter
import com.cl.modules_home.viewmodel.HomeViewModel
import com.cl.modules_home.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.AbsP2pCameraListener
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.OperationDelegateCallBack
import com.thingclips.smart.camera.ipccamerasdk.p2p.ICameraP2P
import com.thingclips.smart.camera.middleware.p2p.IThingSmartCameraP2P
import com.thingclips.smart.camera.middleware.widget.AbsVideoViewCallback
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import com.tuya.smart.android.demo.camera.utils.DPConstants
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.nio.ByteBuffer
import java.util.Calendar
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

    // 传过来的设备状态
    // 默认为false
    // 是否是第一次登录注册、并且是从未绑定过设备
    private val firstLoginAndNoDevice by lazy {
        arguments?.getBoolean(Constants.Global.KEY_GLOBAL_PLANT_FIRST_LOGIN_AND_NO_DEVICE, false)
    }

    // 是否是手动模式
    private val isManual by lazy {
        arguments?.getBoolean(Constants.Global.KEY_MANUAL_MODE, false)
    }

    // 导航气泡
    private val bubblePopHor by lazy {
        // 居中显示
        XPopup.Builder(context)
            .popupPosition(PopupPosition.Top)
            .dismissOnTouchOutside(false)
            .isCenterHorizontal(false)
            .hasShadowBg(true) // 半透明背景
            //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
            .offsetY(XPopupUtils.dp2px(context, 6f))
    }

    override fun initView(view: View) {
        ARouter.getInstance().inject(this)
        binding.plantOffLine.title.setLeftVisible(false)

        // 获取sn
        mViewMode.getSn()
        // 刷新token、放在customSplashActivity页面了。

        // getAppVersion 检查版本更新
        mViewMode.getAppVersion()

        // 初始化摄像头尺寸
        initCameraSize()

        liveDataObser()
    }

    private fun startCountDownJob() {
        if (mViewMode.shouldRunJob.value == false) return

        job = mViewMode.countDownCoroutines(10 * 6 * 500000, viewLifecycleOwner.lifecycleScope, onTick = {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    if (it % 15 == 0) {
                        // 表示过了15秒
                        mViewMode.getUnread()
                        // 查询植物信息Look
                        mViewMode.plantInfoLoop()
                        // 刷新设备列表
                        mViewMode.listDevice()
                        // 刷新是否获取勋章
                        mViewMode.getMedal()
                        // 获取环境消息
                        mViewMode.getEnvData()
                    }
                } catch (e: Exception) {
                    // Handle exception here
                    job?.cancel()
                }
            }
            if (it == 0) {
                job?.cancel()
            }
        }, onStart = {
            // onStart logic here
        }, onFinish = {
            // onFinish logic here
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
         * InterCome消息监听
         */
        LiveEventBus.get().with(Constants.InterCome.KEY_INTER_COME_UNREAD_MESSAGE, Int::class.java)
            .observe(viewLifecycleOwner) {
                mViewMode.getEaseUINumber()
            }

        /**
         * 设备管理界面、切换设备
         */
        LiveEventBus.get().with(Constants.Global.KEY_IS_SWITCH_DEVICE, LiveDataDeviceInfoBean::class.java)
            .observe(viewLifecycleOwner) { devieInfo ->
                if (null != devieInfo) {
                    logI("LiveDataDeviceInfoBean: ${devieInfo.deviceId},,, ${devieInfo.spaceType}")
                    // 切换设备如果有摄像头的话，都是隐藏，会占用内存,
                    cameraStopOnpause()
                    mViewMode.setDeviceInfo(devieInfo)
                    devieInfo.deviceId?.let {
                        // 保存灯光预设值,保存上一个设备的灯光预设值
                        // 转换设备
                        mViewMode.setDeviceId(it)
                        mViewMode.switchDevice(it)
                    }
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
            firstLoginViewVisibile()
            return
        }

        // 是帐篷
        if (mViewMode.isZp.value == true) {
            // 显示布局
            ViewUtils.setVisible(binding.clRoot)
            ViewUtils.setVisible(binding.pplantNinth.root)
            // ViewUtils.setVisible(binding.pplantNinth.ivZpBg)
            ViewUtils.setGone(binding.pplantNinth.tvPrivacyMode)
            // ViewUtils.setGone(binding.pplantNinth.ivZpCamera)
            // 隐藏当前所有布局
            ViewUtils.setGone(binding.plantExtendBg.root)
            ViewUtils.setGone(binding.plantFirst.root)
            ViewUtils.setGone(binding.plantAddWater.root)
            ViewUtils.setGone(binding.plantClone.root)
            ViewUtils.setGone(binding.bindDevice.root)
            ViewUtils.setGone(binding.plantManual.root)
            ViewUtils.setGone(binding.plantComplete.root)
            ViewUtils.setGone(binding.plantOffLine.root)

            // 隐藏pplantNinth.root 都是这个布局里面的、气泡布局、隐藏种子倒计时布局、隐藏水的图片布局
            ViewUtils.setGone(binding.pplantNinth.clContinue)
            ViewUtils.setGone(binding.pplantNinth.clSeeding)
            ViewUtils.setGone(binding.pplantNinth.ivWaterStatus)

            // 这是里刷新主要是展示当前的帐篷图片信息。
            mViewMode.listDevice()
            // 加载帐篷所需要的植物信息。
            mViewMode.plantInfo()
            // mViewMode.getEnvData()
            return
        }

        // 是否是手动模式
        if (isManual == true && mViewMode.loadFirst.value == false) {
            ViewUtils.setVisible(binding.plantManual.root)
            ViewUtils.setGone(binding.plantOffLine.root)
            ViewUtils.setGone(binding.clRoot)
            // 加载手动模式相关数据
            mViewMode.getPlantHeight()
            mViewMode.getWenDu()
            mViewMode.getHumidity()
            mViewMode.getWaterWenDu()
            mViewMode.getFanIntake()
            mViewMode.getFanExhaust()
            mViewMode.getCurrentProMode(mViewMode.userDetail.value?.data?.deviceId.toString()) // 获取预先配置的灯光强度，放在了获取140Dp点上。
            mViewMode.getCurrentGrowLight()
            mViewMode.getAirPump()
            mViewMode.getLightTime()
            mViewMode.getCloseLightTime()
            mViewMode.setLoadFirst(true)
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
                        ViewUtils.setGone(
                            binding.plantFirst.root,
                            binding.plantAddWater.root,
                            binding.plantClone.root,
                            binding.plantComplete.root
                        )
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

    private fun firstLoginViewVisibile() {
        /* ViewUtils.setVisible(binding.bindDevice.root)
         ViewUtils.setGone(binding.plantOffLine.root)
         ViewUtils.setGone(binding.clRoot)

         // 如果是第一次、也从未绑定过设备、显示出气泡
         ViewUtils.setVisible(binding.bindDevice.tvScan)
         ViewUtils.setGone(binding.bindDevice.clContinue, binding.bindDevice.connectDevice)*/

        //
        mViewMode.checkPlant()
    }

    /**
     * 各种View 点击方法
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun lazyLoad() {
        // 跳转到种植引导界面
        binding.plantFirst.apply {
            // 跳跳转plant2
            ivStart.setOnClickListener {
                mViewMode.whetherSubCompensation()
            }

            ivDeviceList.setOnClickListener {
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                    .navigation(activity)
            }

            ivSupport.setOnClickListener {
                sendEmail()
            }
        }

        // 添加水的步骤
        binding.plantAddWater.apply {
            ivAddWater.setOnClickListener {
                plantFour.show()
            }
            svContinue.setOnClickListener {
                plantSix().show()
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
            //防止点击穿透问题
            this.root.setOnTouchListener { _, _ -> true }

            // 点击live直接打开直播
            ivLive.setSafeOnClickListener {
                startToVideoPlay()
            }

            ivZpCamera.setOnClickListener {
                // 跳转到配件
                mViewMode.listDevice.value?.data?.firstOrNull { it.deviceId == mViewMode.userDetail.value?.data?.deviceId }
                    ?.let {
                        ARouter
                            .getInstance()
                            .build(RouterPath.My.PAGE_ADD_ACCESSORY)
                            .withString("deviceId", mViewMode.userDetail.value?.data?.deviceId)
                            .withString("deviceType", mViewMode.userDetail.value?.data?.deviceType)
                            .withSerializable(
                                "accessoryList",
                                it.accessoryList as Serializable?
                            )
                            .withString("spaceType", it.spaceType)
                            .navigation(context)
                    }
                return@setOnClickListener
            }

            // 未读消息气泡点击事件
            tvBtnDesc.setOnClickListener {
                // 自定义开始种植弹窗
                // 判断点击时长
                if (mViewMode.unreadMessageList.value.isNullOrEmpty()) {
                    // 解锁第一个周期
                    // 显示startRunning气泡的时候，必定是发芽了的
                    // todo 这个地方直接调用startRunning接口了。
                    mViewMode.startRunning(botanyId = "", goon = false)
                    return@setOnClickListener
                }

                // 未读消息弹窗，获取极光消息弹窗
                // 极光消息来了，只需要把这个消息添加到当前list就好了。
                mViewMode.unreadMessageList.value?.let {
                    // 调用图文信息
                    if (it.size == 0) return@let
                    // How to do LTS
                    mViewMode.getUnreadMessageList().firstOrNull()?.jumpType?.let { jumpType ->
                        val messageId = mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                        when (jumpType) {
                            UnReadConstants.JumpType.KEY_LEARN_MORE -> {
                                // 单独处理， 弹窗
                                // mViewMode.getMessageDetail("$messageId")
                                mViewMode.getUnreadMessageList().firstOrNull()?.articleId?.let {articleId ->
                                    InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, articleId)
                                }
                                mViewMode.getRead("$messageId")
                                return@setOnClickListener
                            }

                            UnReadConstants.JumpType.KEY_GUIDE -> {
                                mViewMode.getRead("$messageId")
                                return@setOnClickListener
                            }

                            else -> {}
                        }
                    }

                    mViewMode.getUnreadMessageList().firstOrNull()?.type?.let { type ->
                        val messageId = mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                        when (type) {
                            UnReadConstants.JumpType.KEY_PREVENT_DRY_BURNING, UnReadConstants.JumpType.KEY_CLOSE_DOOR
                            -> {
                                mViewMode.getRead("$messageId")
                                return@setOnClickListener
                            }
                             else -> {}
                        }
                    }

                    // 状态赋值
                    mViewMode.getUnreadMessageList().firstOrNull()?.type?.let { type ->
                        // 目前只处理了种植状态
                        // 周期的解锁
                        // 气泡解锁
                        // 判断是否是Vip、如果是Vip那么就直接跳转到日历。反之就主页解锁
                        /*if (mViewMode.userDetail.value?.data?.isVip == 1) {
                            ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                                .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                            return@observe
                        }*/

                        // 如果不是以下四种那么就会直接调用guideInfo接口。
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

                            // 防烧模式
                            UnReadConstants.Device.KEY_BURN_OUT_PROOF -> {
                                mViewMode.getRead(
                                    "${
                                        mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                    }"
                                )
                                // 直接跳转到日历
                                ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_SETTING)
                                    .withBoolean("isScrollToBurn", true)
                                    .navigation(activity)
                            }

                            UnReadConstants.Device.KEY_CHANGE_CUP_WATER -> {
                                // 种子发芽之后的换水
                                // 跳转到富文本
                                val intent = Intent(context, BasePopActivity::class.java)
                                intent.putExtra(
                                    Constants.Global.KEY_TXT_ID,
                                    Constants.Fixed.KEY_FIXED_ID_WATER_CHANGE_GERMINATION
                                )
                                intent.putExtra(
                                    BasePopActivity.KEY_FIXED_TASK_ID,
                                    Constants.Fixed.KEY_FIXED_ID_WATER_CHANGE_GERMINATION
                                )
                                intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                                intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                                intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                                intent.putExtra(
                                    BasePopActivity.KEY_UNLOCK_TASK_ID,
                                    mViewMode.getUnreadMessageList().firstOrNull()?.taskId
                                )
                                intent.putExtra(
                                    BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE,
                                    "Slide to Next"
                                )
                                startActivityLauncherSeeding.launch(intent)
                            }

                            UnReadConstants.Device.KEY_CLOSE_DOOR -> {
                                mViewMode.getRead(
                                    "${
                                        mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                    }"
                                )
                            }

                            // 灯光
                            UnReadConstants.Device.KEY_REMIND_LIGHT_UP -> {
                                mViewMode.getRead(
                                    "${
                                        mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                                    }"
                                )
                            }

                            else -> {
                                // 直接跳转到日历
                                ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                                    .withString(
                                        Constants.Global.KEY_CATEGORYCODE,
                                        mViewMode.plantInfo.value?.data?.categoryCode
                                    )
                                    .navigation(activity, HomeFragment.KEY_FOR_CALENDAR_REFRSH)
                            }
                        }
                    }

                }
            }

            // 切换摄像头
            ivSwitchCamera.setOnClickListener {
                // 获取, 并且取反
                // 因为点击之后，就是切换摄像头，所以需要取反。
                // 这里取反下面就不能取反了，不然就是双重否定。
                val isSHowCamera = !Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_CAMERA, true)

                // 显示隐藏的布局 、显示摄像头或者是帐篷的情况下
                binding.pplantNinth.ivOne.setBackgroundResource(if (isSHowCamera || mViewMode.isZp.value == true) R.mipmap.home_plant_three_right_angle_bg else R.mipmap.home_plant_three_bg)
                ViewUtils.setVisible(isSHowCamera, binding.pplantNinth.rlBowl)
                ViewUtils.setInvisible(binding.pplantNinth.ivBowl, isSHowCamera || mViewMode.isZp.value == true)

                // 如果是帐篷、那么就需要展示帐篷
                ViewUtils.setVisible(
                    isSHowCamera && mViewMode.isZp.value == true,
                    binding.pplantNinth.ivSwitchZpBg
                )
                // 没有显示摄像头，注意注意，不能取反
                ViewUtils.setVisible(
                    (!isSHowCamera && mViewMode.isZp.value == true),
                    binding.pplantNinth.ivZpBg
                )

                // 没有显示摄像头，注意注意，不能取反
                // 夜间模式下的植物, 不是帐篷的情况下才显示abby的夜间模式
                ViewUtils.setVisible(
                    (!isSHowCamera && mViewMode.getCurrentGrowLight.value == 0 && mViewMode.isZp.value == false) ,
                    binding.pplantNinth.ivThree,
                    binding.pplantNinth.ivTwo
                )

                // 如果是从false -> true
                if (isSHowCamera) {
                    // 加载并且展示视频
                    mViewMode.getCameraFlag { isHave, isLoadCamera, cameraId, devId ->
                        if (!isHave) return@getCameraFlag
                        initCamera(cameraId)
                    }
                }

                // 放入
                Prefs.putBoolean(Constants.Global.KEY_IS_SHOW_CAMERA, isSHowCamera)
            }
            // 跳转到摄像头界面
            ivCamera.setOnClickListener {

                /*ARouter
                   .getInstance()
                   .build(RouterPath.Home.PAGE_CAMERA)
                   .withString(Constants.Global.INTENT_DEV_ID, "123")
                   .navigation(context)*/

                val cameraAccessory =
                    mViewMode.listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }
                        ?.accessoryList?.firstOrNull { it.accessoryType == AccessoryListBean.KEY_CAMERA }
                // 跳转到IPC界面
                com.cl.common_base.util.ipc.CameraUtils.ipcProcess(
                    it.context,
                    cameraAccessory?.accessoryDeviceId
                )
                //                                CameraUtils.ipcProcess(it.context, cameraAccessory?.accessoryDeviceId)
            }

            // 选中门锁开关
            ivDoorLockStatus.setOnClickListener {
                pop.isDestroyOnDismiss(false).dismissOnTouchOutside(false)
                    .asCustom(
                        context?.let { it1 ->
                            OpenDoorPop(
                                it1,
                                onSuccessAction = {
                                    // 开门成功
                                    ViewUtils.setVisible(ivDoorToast)
                                    ivDoorToast.postDelayed({
                                        ViewUtils.setGone(ivDoorToast)
                                    }, 2000)

                                    ivDoorLockStatus.postDelayed({
                                        ViewUtils.setGone(ivDoorLockStatus)
                                    }, 2000)
                                }
                            )
                        }
                    ).show()
            }

            // 选中日历
            ivCalendar.setOnClickListener {
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                    .withString(
                        Constants.Global.KEY_CATEGORYCODE,
                        mViewMode.plantInfo.value?.data?.categoryCode
                    )
                    .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                // 如果是订阅用户
                /*if (mViewMode.userDetail.value?.data?.isVip == 1) {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                        .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                } else {
                    // 不是订阅用户，直接弹出图文
                    mViewMode.getGuideInfo("unearned_subscription_explain")
                }*/
            }

            // 选中学院
            ivAcademy.setOnClickListener {
                context?.startActivity(Intent(context, AcademyActivity::class.java))
                // 如果是订阅用户
                /*if (mViewMode.userDetail.value?.data?.isVip == 1) {
                    context?.startActivity(Intent(context, AcademyActivity::class.java))
                } else {
                    // 不是订阅用户，直接弹出图文
                    mViewMode.getGuideInfo("unearned_subscription_explain")
                }*/
            }

            // 设备管理界面
            ivDeviceList.setOnClickListener {
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                    .navigation(activity)
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

            // 左滑动
            // 左滑动
            imageLeftSwip.setOnClickListener {
                val listDeviceData = mViewMode.listDevice.value?.data
                listDeviceData?.indexOfFirst { it.currentDevice == 1 }?.apply {
                    val nextIndex = if (this - 1 < 0) listDeviceData.size - 1 else this - 1
                    val deviceBean = listDeviceData[nextIndex]
                    mViewMode.setLeftSwaps(true)
                    // 切换设备
                    deviceBean.deviceId?.let { it1 -> mViewMode.setDeviceId(it1) }
                    deviceBean.deviceId?.let { it1 -> mViewMode.switchDevice(it1) }
                }
            }

            // 右滑动
            imageRightSwip.setOnClickListener {
                val listDeviceData = mViewMode.listDevice.value?.data
                listDeviceData?.indexOfFirst { it.currentDevice == 1 }?.apply {
                    val nextIndex = if (this + 1 >= listDeviceData.size) 0 else this + 1
                    val deviceBean = listDeviceData[nextIndex]
                    mViewMode.setLeftSwaps(false)
                    // 切换设备
                    deviceBean.deviceId?.let { it1 -> mViewMode.setDeviceId(it1) }
                    deviceBean.deviceId?.let { it1 -> mViewMode.switchDevice(it1) }
                }
            }

            // 点击弹出周期弹窗
            clPeroid.setOnClickListener {
                // 调用接口一次
                mViewMode.plantInfo()
                mViewMode.periodData.value?.let { data -> periodPop?.setData(data) }
                periodPopDelegate.show()
            }

            // 点击弹出个氧气币窗口
            clOxy.setOnClickListener {

                // 帐篷下，弹出设置开关灯的时间
                if (mViewMode.isZp.value == true) {
                    xpopup {
                        isDestroyOnDismiss(false)
                        enableDrag(true)
                        dismissOnTouchOutside(false)
                        // maxHeight(dp2px(600f))
                        asCustom(context?.let { it1 ->
                            HomeLightPop(
                                it1,
                                deviceId = mViewMode.userDetail.value?.data?.deviceId ?: "",
                                onTime = mViewMode.plantInfo.value?.data?.lightOn ?: 0,
                                onOffTime = mViewMode.plantInfo.value?.data?.lightOff ?: 0,
                                onConfirmAction = { _, _ -> mViewMode.plantInfo() }
                            )
                        }).show()
                    }
                    return@setOnClickListener
                }

                // 跳转到资产界面
                ARouter
                    .getInstance()
                    .build(RouterPath.My.PAGE_DIGITAL)
                    .navigation(context)

                /*xpopup {
                    isDestroyOnDismiss(false)
                    enableDrag(true)
                    dismissOnTouchOutside(true)
                    maxHeight(dp2px(600f))
                    asCustom(context?.let { it1 -> HomeOxyPop(it1) }).show()
                }*/
            }

            // 点击环境弹窗
            clEnvir.setOnClickListener {
                if (mViewMode.isZp.value == true) {
                    val data = mViewMode.plantInfoLoop.value?.data?.envirVO
                    if (null == data || (data.temp.isNullOrEmpty() && data.roomTemp.isNullOrEmpty() && data.humiture.isNullOrEmpty() && data.roomHumiture.isNullOrEmpty())) {
                        // 跳转到配件
                        mViewMode.listDevice.value?.data?.firstOrNull { it.deviceId == mViewMode.userDetail.value?.data?.deviceId }
                            ?.let {
                                ARouter
                                    .getInstance()
                                    .build(RouterPath.My.PAGE_ADD_ACCESSORY)
                                    .withString("deviceId", mViewMode.userDetail.value?.data?.deviceId)
                                    .withSerializable(
                                        "accessoryList",
                                        it.accessoryList as Serializable?
                                    )
                                    .withString("spaceType", it.spaceType)
                                    .withString("deviceType", mViewMode.userDetail.value?.data?.deviceType)
                                    .navigation(context)
                            }
                    }
                    return@setOnClickListener
                }
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

        //  手动模式
        binding.plantManual.apply {
            ivLive.setSafeOnClickListener {
                startToVideoPlay()
            }

            ivChart.setSafeOnClickListener {
                context?.let {
                    it.startActivity(Intent(it, PeriodActivity::class.java))
                }
            }

            tvSave.setSafeOnClickListener(viewLifecycleOwner.lifecycleScope) {
                val bean = PresetData(
                    fanIntake = "${mViewMode.getFanIntake.value}",
                    fanExhaust = "${mViewMode.getFanExhaust.value}",
                    lightIntensity = "${mViewMode.getGrowLight.value}",
                    lightSchedule = ftTimer.text.toString(),
                    muteOn = "${mViewMode.muteOn}",
                    muteOff = "${mViewMode.muteOff}"
                )
                context?.let {
                    xpopup(it) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(true)
                        autoFocusEditText(false)
                        moveUpToKeyboard(true)
                        autoOpenSoftInput(false)
                        asCustom(PresetPop(it, bean, mViewMode.getPreset.value?.data) {  presetData ->
                            // 调用保存预设模版接口
                            mViewMode.addProModeRecord(
                                ProModeInfoBean(
                                    lightSchedule = ftTimer.text.toString(),
                                    bright = mViewMode.getGrowLight.value,
                                    fanIn = presetData?.fanIntake.safeToInt(),
                                    fanOut = presetData?.fanExhaust.safeToInt(),
                                    deviceId = mViewMode.userDetail.value?.data?.deviceId,
                                    id = mViewMode.getNextUniqueId(), // 这个ID不能重复
                                    lightOff = presetData?.muteOff.safeToInt(),
                                    lightOn = presetData?.muteOn.safeToInt(),
                                    name = presetData?.name,
                                    notes = presetData?.note,
                                    //updateTime = "${System.currentTimeMillis()}",
                                )
                            )
                        }).show()
                    }
                }
            }

            tvLoad.setSafeOnClickListener(viewLifecycleOwner.lifecycleScope) {
                // load
                context?.let {
                    xpopup(it) {
                        isDestroyOnDismiss(false)
                        autoFocusEditText(false)
                        autoOpenSoftInput(false)
                        moveUpToKeyboard(false)
                        dismissOnTouchOutside(true)
                        asCustom(PresetLoadPop(it, mViewMode.getPreset.value?.data, onNextAction = { data ->
                            // 0- 12, 12-24
                            val startTime = when (data?.lightOn) {
                                0 -> 12
                                12 -> 24
                                else -> data?.lightOn ?: 12
                            }

                            val endTime = when (data?.lightOff) {
                                0 -> 12
                                12 -> 24
                                else -> data?.lightOff ?: 12
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
                            } ?: "12:00 AM"

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
                            } ?: "12:00 PM"

                            // 发送dp点
                            if (null == data) return@PresetLoadPop
                            val fanIntake = data.fanIn
                            val fanExhaust = data.fanOut
                            val lightIntensity = data.bright
                            val lightSchedule = "$ftTurnOn-$ftTurnOff"
                            val muteOn = startTime
                            val muteOff = endTime
                            logI("toDP: $fanIntake, $fanExhaust, $lightIntensity, $lightSchedule, $muteOn, $muteOff")

                            fanIntake?.let { it1 -> mViewMode.setFanIntake(it1.toString()) }
                            fanExhaust?.let { it1 -> mViewMode.setFanExhaust(it1.toString()) }
                            lightIntensity?.let { it1 -> mViewMode.setGrowLight(it1.toString()) }
                            ftTimer.text = "$lightSchedule"
                            mViewMode.setmuteOff(muteOff.toString())
                            mViewMode.setmuteOn(muteOn.toString())

                            val dpBean = AllDpBean(cmd = "6", gl = lightIntensity.toString(), gls = muteOn.toString(), gle = muteOff.toString(), ex = fanExhaust.toString(), `in` = fanIntake.toString())

                            // 开灯时间
                            dpBean.gls = muteOn.toString()
                            dpBean.gle = muteOff.toString()

                            // 发送多个DP点
                            GSON.toJson(dpBean)?.let { it1 -> DeviceControl.get().success { logI("dp to success") }.error { code, error -> ToastUtil.shortShow(error) }.sendDps(it1) }

                            // 保存到后台
                            data.deviceId = mViewMode.userDetail.value?.data?.deviceId
                            mViewMode.addCurrentProMode(data)
                        })).show()
                    }
                }
            }

            tvDripPumpDesc.setOnClickListener {
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_DRIP)
            }

            ivDeviceList.setOnClickListener {
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                    .navigation(activity)
            }

            // 跳转到摄像头界面
            ivCamera.setOnClickListener {

                /*ARouter
                   .getInstance()
                   .build(RouterPath.Home.PAGE_CAMERA)
                   .withString(Constants.Global.INTENT_DEV_ID, "123")
                   .navigation(context)*/

                val cameraAccessory =
                    mViewMode.listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }
                        ?.accessoryList?.firstOrNull { it.accessoryType == AccessoryListBean.KEY_CAMERA }
                // 跳转到IPC界面
                com.cl.common_base.util.ipc.CameraUtils.ipcProcess(
                    it.context,
                    cameraAccessory?.accessoryDeviceId
                )
                //                                CameraUtils.ipcProcess(it.context, cameraAccessory?.accessoryDeviceId)
            }

            tvAirPumpDesc.setOnClickListener {
                /* pop.isDestroyOnDismiss(false)
                     .dismissOnTouchOutside(false)
                     .asCustom(
                         context?.let { it1 ->
                             BaseCenterPop(
                                 it1,
                                 content = "The air pump will be turned off in the following conditions:\n" +
                                         "1.When draining is on\n" +
                                         "2.When the tank has no water.",
                                 isShowCancelButton = false,
                                 confirmText = "OK"
                             )
                         }
                     ).show()*/

                InterComeHelp.INSTANCE.openInterComeSpace(
                    InterComeHelp.InterComeSpace.Article,
                    Constants.InterCome.KEY_INTER_COME_AIR_PUMP
                )
            }

            ivExclamationMark.setOnClickListener {
                /* pop.isDestroyOnDismiss(false)
                     .dismissOnTouchOutside(false)
                     .asCustom(
                         context?.let { it1 ->
                             BaseCenterPop(
                                 it1, content =
                                 "The minimum height that can be measured starts from 8 inches (20cm)." +
                                         "\n" +
                                         "\n" +
                                         "To ensure accurate measurement of plant height, please remove all objects above the plant (e.g., the fan, towels, etc.).",
                                 isShowCancelButton = false,
                                 confirmText = "OK"
                             )
                         }
                     ).show()*/

                InterComeHelp.INSTANCE.openInterComeSpace(
                    InterComeHelp.InterComeSpace.Article,
                    Constants.InterCome.KEY_INTER_COME_PLANT_HEIGHT
                )
            }

            // 时间模式
            tvLightIntensityTitle.setSafeOnClickListener(viewLifecycleOwner.lifecycleScope) {
                chooserTime()
            }
            ftTimer.setSafeOnClickListener(viewLifecycleOwner.lifecycleScope) {
                chooserTime()
            }

            fanIntakeSeekbar.customSectionTrackColor { colorIntArr ->
                //the length of colorIntArray equals section count
                //                colorIntArr[0] = Color.parseColor("#008961");
                //                colorIntArr[1] = Color.parseColor("#008961");
                // 当刻度为最后4段时才显示红色
                // colorIntArr[6] = Color.parseColor("#F72E47")
                colorIntArr[7] = Color.parseColor("#F72E47")
                colorIntArr[8] = Color.parseColor("#F72E47")
                colorIntArr[9] = Color.parseColor("#F72E47")
                true //true if apply color , otherwise no change
            }
            fanIntakeSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(p0: SeekParams?) {

                }

                override fun onStartTrackingTouch(p0: IndicatorSeekBar?) {
                }

                override fun onStopTrackingTouch(seekbar: IndicatorSeekBar?) {
                    val progress = seekbar?.progress ?: 0
                    if (progress >= 7) {
                        val boolean = Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_FAN_SEVEN_TIP, false)
                        if (!boolean) {
                            context?.let {
                                xpopup(it) {
                                    isDestroyOnDismiss(false)
                                    dismissOnTouchOutside(false)
                                    asCustom(HomeFanBottonPop(it, title = "You're about to set the intake fan to its maximum level. Be aware that this may cause 'wind burn,' leading to rapid water loss in the leaves. We recommend keeping the intake fan level below 7 during the plant's first four weeks.", tag = HomeFanBottonPop.FAN_TAG, remindMeAction = {
                                    }, benOKAction = {})).show()
                                }
                            }
                        }
                    }
                    DeviceControl.get()
                        .success {
                            mViewMode.setFanIntake(seekbar?.progress.toString())
                        }
                        .error { code, error ->
                            ToastUtil.shortShow(
                                """
                              fanIntake: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                            )
                            mViewMode.setFanIntake("${mViewMode.getFanIntake.value}")
                        }
                        .fanIntake(seekbar?.progress ?: 0)
                }
            }

            fanExhaustSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(p0: SeekParams?) {
                }

                override fun onStartTrackingTouch(p0: IndicatorSeekBar?) {
                }

                override fun onStopTrackingTouch(seekbar: IndicatorSeekBar?) {
                    DeviceControl.get()
                        .success {
                            mViewMode.setFanExhaust(seekbar?.progress.toString())
                        }
                        .error { code, error ->
                            ToastUtil.shortShow(
                                """
                              fanExhaust: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                            )
                            mViewMode.setFanExhaust("${mViewMode.getFanExhaust.value}")
                        }
                        .fanExhaust(seekbar?.progress ?: 0)
                }
            }

            ivDripStatus.setOnClickListener {
                xpopup {
                    isDestroyOnDismiss(false)
                    enableDrag(true)
                    dismissOnTouchOutside(false)
                    // maxHeight(dp2px(600f))
                    asCustom(context?.let { it1 -> HomeDripPop(it1) }).show()
                }
            }

            ivDrainStatus.setOnClickListener {
                with(DeviceControl) {
                    get()
                        .success {
                            with(mViewMode) { setDrainageFlag(!(getDrainageFlag.value ?: false)) }
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
                        .pumpWater(!(mViewMode.getDrainageFlag.value ?: false))
                }
                // 背景颜色
                synchronized(this@HomeFragment) {
                    if (mViewMode.getDrainageFlag.value == true) {
                        ivDrainStatus.setBackgroundResource(R.mipmap.home_drain_pause)
                    } else {
                        ivDrainStatus.setBackgroundResource(R.mipmap.home_drain_start)
                    }
                }
            }

            ftAirPump.setSwitchCheckedChangeListener { _, isChecked ->
                if (!isChecked) {
                    pop.isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(false)
                        .asCustom(context?.let {
                            BaseCenterPop(
                                it,
                                content = "Caution! Turning off the air pump may result in the plant roots experiencing a lack of oxygen. Are you sure you want to continue?",
                                cancelText = "No",
                                confirmText = "Yes",
                                onCancelAction = {
                                    // 需要恢复到之前到档位
                                    mViewMode.setAirPump("true")
                                },
                                onConfirmAction = {
                                    // 什么都不做
                                    with(DeviceControl) {
                                        get()
                                            .success {
                                                mViewMode.setAirPump("$isChecked")
                                            }
                                            .error { code, error ->
                                                ToastUtil.shortShow(
                                                    """
                                                      airPump: 
                                                      code-> $code
                                                      errorMsg-> $error
                                                """.trimIndent()
                                                )
                                            }
                                            .airPump(isChecked)
                                    }
                                })
                        }).show()
                    return@setSwitchCheckedChangeListener
                }
                if (mViewMode.getWaterLevel.value == "Low") {
                    pop.isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(false)
                        .asCustom(
                            context?.let {
                                BaseCenterPop(
                                    it,
                                    content = "The air pump cannot be turned on when the tank has no water. Please fill the tank with water before turning on the air pump.",
                                    isShowCancelButton = false,
                                    confirmText = "OK",
                                    onCancelAction = {
                                    },
                                    onConfirmAction = {
                                        // 需要恢复到之前到档位
                                        mViewMode.setAirPump("false")
                                    })
                            }).show()
                    return@setSwitchCheckedChangeListener
                }
                with(DeviceControl) {
                    get()
                        .success {
                            mViewMode.setAirPump("$isChecked")
                        }
                        .error { code, error ->
                            ToastUtil.shortShow(
                                """
                                      airPump: 
                                      code-> $code
                                      errorMsg-> $error
                                """.trimIndent()
                            )
                        }
                        .airPump(isChecked)
                }
            }
        }

        // 设备不在线
        binding.plantOffLine.apply {
            title.setRightButtonImg(R.mipmap.home_device_list)
                .setRightClickListener {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                        .navigation(activity)
                }
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

                // 隐藏
                // 截图
                val layout =
                    binding.plantComplete.rlComplete // replace with your actual RelativeLayout id
                layout.isDrawingCacheEnabled = true
                layout.buildDrawingCache()
                val bitmap = Bitmap.createBitmap(layout.drawingCache)
                layout.isDrawingCacheEnabled = false
                val filePath = FileUtil.saveBitmap(bitmap, context)
                logI("123123123: filepa: $filePath")
                if (filePath.isNotBlank()) {
                    // 分享到朋友圈
                    ARouter.getInstance().build(RouterPath.Contact.PAGE_TREND)
                        .withString(Constants.Global.KEY_SHARE_TYPE, "plant_complete")
                        .withString(
                            Constants.Global.KEY_SHARE_TEXT,
                            "I’m #${mViewMode.getFinishPage.value?.data?.harvestComplete} Complete grower"
                        )
                        .withString(Constants.Global.KEY_SHARE_CONTENT, filePath)
                        .navigation()
                }
            }
        }

        // 设备绑定界面
        binding.bindDevice.apply {
            knowMore.setOnClickListener {
                //  跳转新的图文界面
                val intent = Intent(activity, KnowMoreActivity::class.java)
                intent.putExtra(
                    Constants.Global.KEY_TXT_ID,
                    Constants.Fixed.KEY_FIXED_ID_PAGE_NOT_PURCHASED
                )
                startActivity(intent)
            }
            connectDevice.setOnClickListener {
                //  连接设备
                //  删除设备跳转到首页
                checkPer()
            }
            tvScan.setOnClickListener {
                //  删除设备跳转到首页
                checkPer()
            }
        }
    }

    private fun HomeItemPlantManualBinding.chooserTime() {
        pop.asCustom(context?.let { it1 ->
            ChooseTimePop(
                it1,
                turnOnText = "Turn on Light",
                turnOffText = "Turn off Light",
                isShowNightMode = false,
                isTheSpacingHours = false,
                turnOnHour = mViewMode.muteOn?.safeToInt(),
                turnOffHour = mViewMode.muteOff?.safeToInt(),
                isProMode = true,
                lightIntensity = mViewMode.getGrowLight.value.safeToInt(),
                proModeAction = { onTime, offMinute, timeOn, timeOff, timeOpenHour, timeCloseHour, lightIntensity ->
                    mViewMode.setGrowLight(lightIntensity.toString())

                    ftTimer.text = "$onTime-$offMinute"
                    mViewMode.setmuteOn("$timeOn")
                    mViewMode.setmuteOff("$timeOff")

                    // 开灯时间
                    // 调节灯光事件, 需要用到140字段
                    val dpBean = AllDpBean(
                        cmd = "6", gl = lightIntensity.toString(), gle = when (timeOff) {
                            12 -> 0
                            24 -> 12
                            else -> timeOff
                        }.toString(), gls = when (timeOn) {
                            12 -> 0
                            24 -> 12
                            else -> timeOn
                        }.toString(), ex = mViewMode.getFanExhaust.value.toString(), `in` = mViewMode.getFanIntake.value.toString()
                    )
                    GSON.toJson(dpBean)?.let { it1 -> DeviceControl.get().success { logI("dp to success") }.error { code, error -> ToastUtil.shortShow(error) }.sendDps(it1) }

                    // 保存到后台
                    mViewMode.addCurrentProMode(ProModeInfoBean(deviceId = mViewMode.userDetail.value?.data?.deviceId, bright = lightIntensity, lightOn = timeOn, lightOff = timeOff))
                })
        }).show()
    }

    /**
     * 添加化肥弹窗
     */
    private val plantFeed by lazy {
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false).maxHeight(dp2px(650f))
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomePlantFeedPop(context = it, onNextAction = {
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
                        return@HomePlantFeedPop
                    }

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
                        return@HomePlantFeedPop
                    }

                    // 第六个弹窗
                    // plant6后记“3”
                    mViewMode.setCurrentReqStatus(3)
                    mViewMode.saveOrUpdate("3")
                    // 涂鸦指令，添加化肥
                    /*DeviceControl.get().success {
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
                    }.feedAbby(true)*/
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
                            plantSix().show()
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
                // mViewMode.advertising()
                // 传递的数据为空
                val intent = Intent(it, BasePumpActivity::class.java)
                intent.putExtra(
                    BasePumpActivity.KEY_UNREAD_MESSAGE_DATA,
                    mViewMode.getUnreadMessageList() as? Serializable
                )
                myActivityLauncher.launch(intent)
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
    private fun plantSix(): BasePopupView {
        return XPopup.Builder(context).isDestroyOnDismiss(false).maxHeight(dp2px(600f))
            .enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(context?.let {
                HomePlantSixPop(context = it,
                    isFattening = mViewMode.getUnreadMessageList()
                        .firstOrNull()?.type == UnReadConstants.Device.KEY_CHANGING_WATER,
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
                            return@HomePlantSixPop
                        }

                        // 第六个弹窗
                        // plant6后记“3”
                        mViewMode.setCurrentReqStatus(3)
                        mViewMode.saveOrUpdate("3")


                        // 需要先发送指令喂食
                        /* DeviceControl.get().success {
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
                         }.feedAbby(true)*/
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
                    /*if (taskType == UnReadConstants.PlantStatus.TASK_TYPE_CHECK_TRANSPLANT) {
                        context?.let { context ->
                            SeedGuideHelp(context).showGuidePop {
                                mViewMode.popPeriodStatus.value?.let { map ->
                                    mViewMode.finishTask(FinishTaskReq(taskId, weight))
                                }
                            }
                        }
                        return@BasePlantUsuallyGuidePop
                    }*/

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
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                    .withString(
                        Constants.Global.KEY_CATEGORYCODE,
                        mViewMode.plantInfo.value?.data?.categoryCode
                    )
                    .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                // 判断是否是Vip、如果是Vip那么就直接跳转到日历。反之就主页解锁
                /* if (mViewMode.userDetail.value?.data?.isVip == 1) {
                     ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR)
                         .navigation(activity, KEY_FOR_CALENDAR_REFRSH)
                     return@HomePeriodPop
                 }
                 // todo 此处是用于周期弹窗解锁的
                 mViewMode.setPopPeriodStatus(
                     guideId = guideType, taskId = taskId, taskTime = taskTime
                 )*/
            },
                unLockNow = { pop ->
                    xpopup {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(false)
                        asCustom(
                            context?.let {
                                BaseCenterPop(it, titleText = "Unlock Now", isShowCancelButton = true, cancelText = "Cancel", confirmText = "Unlock", onConfirmAction = {
                                    pop.dismiss()
                                    // 跳转副本界面
                                    // 跳转到富文本界面
                                    val intent = Intent(context, KnowMoreActivity::class.java)
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_PREPARE_UNLOCK_PERIOD)
                                    intent.putExtra(
                                        BasePopActivity.KEY_FIXED_TASK_ID,
                                        Constants.Fixed.KEY_FIXED_ID_PREPARE_UNLOCK_PERIOD
                                    )
                                    intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Unlock")
                                    intent.putExtra(BasePopActivity.KEY_PLANT_ID, mViewMode.plantInfo.value?.data?.plantId.toString())
                                    startActivityLauncherCheck.launch(intent)
                                }, onCancelAction = {
                                })
                            }
                        ).show()
                    }

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
        XPopup.Builder(context).isDestroyOnDismiss(false).enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(true).asCustom(envirPop)
    }
    private val envirPop by lazy {
        context?.let {
            HomeEnvlrPop(
                it,
                disMissAction = {
                    // 消失之后，刷新数据
                    mViewMode.getEnvData()
                    mViewMode.listDevice()
                    mViewMode.userDetail()
                }
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

    @SuppressLint("SetTextI18n")
    override fun observe() {
        mViewMode.apply {
            // 获取当前设备的proMode下的预设灯光。
            getCurrentProMode.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (null == data) {
                        kotlin.runCatching {
                            // 获取当前灯光
                            val currentGrowLight =
                                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_GROW_LIGHT }
                                    ?.get(TuYaDeviceConstants.KEY_DEVICE_GROW_LIGHT).toString().safeToDouble().safeToInt()
                            setGrowLight(currentGrowLight.toString())
                        }
                        return@success
                    }
                    setGrowLight(data?.bright.toString())
                }
            })

            addPreset.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    mViewMode.userDetail.value?.data?.deviceId?.let { mViewMode.getProModeRecord(it) }
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
            })

            getMedal.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.show(errorMsg) }
                success {
                    data?.forEach { bean ->
                        context?.let { cx ->
                            xpopup(cx) {
                                isDestroyOnDismiss(false)
                                dismissOnTouchOutside(true)
                                maxHeight(dp2px(700f))
                                asCustom(MedalPop(cx, bean)).show()
                            }
                        }
                    }
                }
            })

            getFanIntake.observe(viewLifecycleOwner) {
                binding.plantManual.fanIntakeSeekbar.setProgress(it.toFloat())
            }
            getFanExhaust.observe(viewLifecycleOwner) {
                binding.plantManual.fanExhaustSeekbar.setProgress(it.toFloat())
            }
            getGrowLight.observe(viewLifecycleOwner) {
                // binding.plantManual.lightIntensitySeekbar.setProgress(it.toFloat())
            }

            // transPlant周期自行处理逻辑
            transplantPeriodicity.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) return@observe
                // 跳转到富文本
                val intent = Intent(context, BasePopActivity::class.java)
                intent.putExtra(
                    Constants.Global.KEY_TXT_ID,
                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_SEED_CHECK
                )
                intent.putExtra(
                    BasePopActivity.KEY_FIXED_TASK_ID,
                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_SEED_CHECK
                )
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                intent.putExtra(BasePopActivity.KEY_UNLOCK_TASK_ID, it)
                intent.putExtra(
                    BasePopActivity.KEY_CATEGORYCODE,
                    mViewMode.plantInfo.value?.data?.categoryCode
                )
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "I am ready")
                intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                startActivity(intent)
            }

            childLockStatus.observe(viewLifecycleOwner) {
                logI("123123333: $it,,,, ${mViewMode.thingDeviceBean()?.devId}, ${mViewMode.isShowDoorDrawable()}")
                ViewUtils.setVisible(
                    mViewMode.isShowDoorDrawable(),
                    binding.pplantNinth.ivDoorLockStatus
                )
                binding.pplantNinth.ivDoorLockStatus.setImageResource(
                    if (it == "true") {
                        R.drawable.home_plant_close_door
                    } else {
                        R.drawable.home_plant_open_door
                    }
                )
            }
            openDoorStatus.observe(viewLifecycleOwner) {
                logI("123123: $it,,,, ${mViewMode.thingDeviceBean()?.devId}, ${mViewMode.isShowDoorDrawable()}")
                ViewUtils.setVisible(
                    mViewMode.isShowDoorDrawable(),
                    binding.pplantNinth.ivDoorLockStatus
                )
                binding.pplantNinth.ivDoorLockStatus.setImageResource(
                    if (it == "true") {
                        R.drawable.home_plant_close_door
                    } else {
                        R.drawable.home_plant_open_door
                    }
                )
            }

            /* getCloseLightTime.observe(viewLifecycleOwner) {
                 if (it.isNullOrEmpty()) return@observe
                 if (getLightTime.value.isNullOrEmpty()) return@observe
                 binding.plantManual.ftTimer.itemValue = mViewMode.getTimeText()
             }*/

            // 设备列表
            listDevice.observe(viewLifecycleOwner, resourceObserver {
                success {
                    hideProgressLoading()
                    data?.let { dataList ->
                        // 判断设备数量，设定左右滑动图片的显示
                        val isVisible = dataList.filter { it.isSwitch == 1 }.size > 1
                        ViewUtils.setVisible(
                            isVisible && mViewMode.isZp.value == false,
                            binding.pplantNinth.imageLeftSwip,
                            binding.pplantNinth.imageRightSwip
                        )

                        // 寻找当前设备
                        dataList.firstOrNull { it.currentDevice == 1 }?.let { device ->
                            // 是否显示摄像头
                            val isCameraVisible =
                                device.accessoryList?.firstOrNull { it.accessoryType == AccessoryListBean.KEY_CAMERA } != null
                            ViewUtils.setVisible(isCameraVisible, binding.pplantNinth.ivCamera)
                            ViewUtils.setVisible(isCameraVisible, binding.plantManual.ivCamera)

                            // 是否显示rlInch
                            ViewUtils.setVisible(
                                device.deviceType == "OG" || device.deviceType == "OG_black",
                                binding.plantManual.rlInch
                            )
                        }

                        if (dataList.isNotEmpty()) {
                            getCameraFlag { isHave, isLoadCamera, cameraId, devId ->
                                binding.pplantNinth.ivOne.setBackgroundResource(if ((isLoadCamera && isHave) || mViewMode.isZp.value == true) R.mipmap.home_plant_three_right_angle_bg else R.mipmap.home_plant_three_bg)
                                ViewUtils.setVisible(
                                    isHave && isLoadCamera,
                                    binding.pplantNinth.rlBowl
                                )
                                ViewUtils.setInvisible(
                                    binding.pplantNinth.ivBowl,
                                    (isHave && isLoadCamera) || mViewMode.isZp.value == true
                                )


                                // 如果是帐篷、并且没有摄像头时。
                                val isZpEnabled = mViewMode.isZp.value == true

                                if (isZpEnabled && !isHave) {
                                    ViewUtils.setVisible(binding.pplantNinth.rlBowl)
                                    ViewUtils.setGone(binding.pplantNinth.cameraVideoView, binding.pplantNinth.ivZpBg)
                                    ViewUtils.setVisible(binding.pplantNinth.ivZpCamera, binding.pplantNinth.ivSwitchZpBg)
                                } else {
                                    val shouldShowSwitch = isHave && isLoadCamera && isZpEnabled
                                    ViewUtils.setVisible(shouldShowSwitch, binding.pplantNinth.ivSwitchZpBg)

                                    val shouldShowIvZpBg = ((isHave && !isLoadCamera) || !isHave) && isZpEnabled
                                    ViewUtils.setVisible(shouldShowIvZpBg, binding.pplantNinth.ivZpBg)
                                }


                                ViewUtils.setInvisible(binding.pplantNinth.ivSwitchCamera, !isHave)

                                // 获取摄像头配件信息
                                mViewMode.getAccessoryInfo(devId, cameraId)

                                // 不加载摄像头
                                if (!isHave || cameraId.isBlank() || !isLoadCamera || isPlay) return@getCameraFlag

                                // 初始化摄像头
                                initCamera(cameraId)
                                // 保存摄像头Id
                                setCameraId(cameraId)
                            }
                        }
                    } ?: run { // 数据为空时设为不可见
                        ViewUtils.setGone(
                            binding.pplantNinth.imageLeftSwip,
                            binding.pplantNinth.imageRightSwip
                        )
                    }
                }
            })

            // 获取配件信息
            getAccessoryInfo.observe(viewLifecycleOwner, resourceObserver {
                success {
                    if (data == null) return@success
                    runCatching {
                        val devId =
                            mViewMode.listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }?.accessoryList?.get(
                                0
                            )?.accessoryDeviceId

                        // 隐私模式不能显示
                        /*if (data?.privateModel == true) {
                            // 主动设置为隐私模式
                            devId?.let { mViewMode.tuYaUtils.publishDps(it, DPConstants.PRIVATE_MODE, true) }
                        } else {
                            // 主动设置为隐私模式
                            devId?.let { mViewMode.tuYaUtils.publishDps(it, DPConstants.PRIVATE_MODE, false) }
                        }*/

                        /**
                         * 监听隐私模式、或者是否在先
                         */
                        devId?.let {
                            // 查询开关们的状态
                            mViewMode.deviceId.value?.let { it1 ->
                                mViewMode.tuYaUtils.queryAbbyValueByDPID(
                                    it1,
                                    TuYaDeviceConstants.KEY_DEVICE_DOOR
                                )
                            }
                            mViewMode.tuYaUtils.listenDPUpdate(
                                it,
                                DPConstants.PRIVATE_MODE,
                                onStatusChangedAction = { online ->
                                    logI("隐私模式：$online")
                                    // 在线和不在线都需要提示
                                    if (online) {
                                        //  在线摄像头
                                        binding.pplantNinth.tvPrivacyMode.text =
                                            "Private Mode"
                                    } else {
                                        //  离线摄像头
                                        binding.pplantNinth.tvPrivacyMode.text =
                                            "The camera is offline"
                                    }
                                    /*ViewUtils.setVisible(!online, binding.pplantNinth.tvPrivacyMode)*/
                                })
                        }
                    }
                }
            })

            // 切换设备列表
            switchDevice.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 更新InterCome用户信息
                    InterComeHelp.INSTANCE.updateInterComeUserInfo(
                        map = mapOf(), userDetail.value?.data, refreshToken.value?.data,
                    )

                    // 这是从切换设备中带过来设备信息\如果是帐篷。
                    deviceInfo.value?.spaceType?.let {
                        if (it != ListDeviceBean.KEY_SPACE_TYPE_BOX) {
                            //  切换设备之后、可以直接调用刷新userDtail接口，走到showView方法中、通过plantInfo和listDevice来显示和隐藏当前abby的信息。
                            // mViewMode.userDetail()
                            // 删除未读消息
                            // mViewMode.removeFirstUnreadMessage()
                            // 清空气泡状态
                            // mViewMode.clearPopPeriodStatus()
                            mViewMode.checkPlant()
                            return@success
                        }
                    }

                    // 更新涂鸦Bean
                    ThingHomeSdk.newHomeInstance(mViewMode.homeId)
                        .getHomeDetail(object : IThingHomeResultCallback {
                            override fun onSuccess(bean: HomeBean?) {
                                bean?.let { it ->
                                    val arrayList = it.deviceList as ArrayList<DeviceBean>
                                    logI("123123123: ${arrayList.size}")
                                    arrayList.firstOrNull { dev -> dev.devId == mViewMode.deviceInfo.value?.deviceId.toString() }
                                        .apply {
                                            logI("thingDeviceBean ID: ${mViewMode.deviceId.value?.toString()}")
                                            logI("thingDeviceBean ID: ${mViewMode.deviceInfo.value?.deviceId.toString()}")
                                            // 在线的、数据为空、并且是abby机器
                                            if (null == this && mViewMode.deviceInfo.value?.spaceType == ListDeviceBean.KEY_SPACE_TYPE_BOX && mViewMode.deviceInfo.value?.onlineStatus != "Offline") {
                                                /*val aa = mViewMode.thingDeviceBean
                                                aa()?.devId = mViewMode.deviceId.value
                                                GSON.toJson(aa)?.let {
                                                    Prefs.putStringAsync(
                                                        Constants.Tuya.KEY_DEVICE_DATA,
                                                        it
                                                    )
                                                }
                                                return@applyh*/
                                                ToastUtil.shortShow("Connection error, try to delete device and pair again")
                                            }
                                            GSON.toJson(this)?.let {
                                                Prefs.putStringAsync(
                                                    Constants.Tuya.KEY_DEVICE_DATA,
                                                    it
                                                )
                                            }

                                            // 重新注册服务
                                            // 开启服务
                                            val intent = Intent(
                                                context,
                                                TuYaDeviceUpdateReceiver::class.java
                                            )
                                            context?.startService(intent)
                                            // 切换之后需要重新刷新所有的东西
                                            mViewMode.checkPlant()
                                        }
                                }
                            }

                            override fun onError(errorCode: String?, errorMsg: String?) {

                            }
                        })
                }
            })

            // 检查是否订阅补偿
            whetherSubCompensation.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }

                success {
                    hideProgressLoading()
                    if (data?.compensation == true) {
                        pop.asCustom(
                            context?.let {
                                BaseCenterPop(
                                    it, onConfirmAction = {
                                        // 补偿订阅
                                        mViewMode.compensatedSubscriber()
                                    },
                                    onCancelAction = {},
                                    spannedString = buildSpannedString {
                                        bold { append("To ensure your first grow runs smoothly, we will be extending your subscription until ") }
                                        bold { append(data?.subscriberTime) }
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

            // 订阅补偿
            compensatedSubscriber.observe(viewLifecycleOwner, resourceObserver {
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

            /*shouldRunJob.observe(viewLifecycleOwner) {
                if (it)
            }*/

            getPreset.observe(viewLifecycleOwner, resourceObserver {
                success {
                    // 同步本地模版
                    asyncProMode()
                    // 同步本地当前预设灯光值。
                    val lightValue = Prefs.getString(Constants.Global.KEY_LIGHT_PRESET_VALUE)
                    if (lightValue.isNotEmpty()) {
                        userDetail.value?.data?.deviceId?.let {
                            val mapType = object : TypeToken<Map<String, Any>>() {}.type
                            val map: Map<String, String> = Gson().fromJson(lightValue, mapType)
                            if (map[it].isNullOrEmpty() || map[it].toString() == "-1") {
                                // _getGrowLight.value = currentGrowLight.safeToInt()
                            } else {
                                setGrowLight(map[it].toString())
                                addCurrentProMode(ProModeInfoBean(bright = map[it].toString().safeToInt(), deviceId = mViewMode.userDetail.value?.data?.deviceId))
                                // 添加之后就删除
                                Prefs.removeKey(Constants.Global.KEY_LIGHT_PRESET_VALUE)
                            }
                        }
                    }
                }
            })

            // 首页循环刷新消息
            userDetail.observe(viewLifecycleOwner, resourceObserver {
                error { msg, code ->
                    hideProgressLoading()
                    msg?.let { it1 -> ToastUtil.shortShow(it1) }
                    // 容错处理、不管接口报错都显示
                    showView(plantFlag, plantGuideFlag)
                    // 请求未读消息数据，只有在种植之后才会开始有数据返回
                    mViewMode.getUnread()
                }
                success {
                    hideProgressLoading()

                    // 查看当前是否有拥有proMode预设模版
                    if (isManual == true) {
                        mViewMode.getProModeRecord(data?.deviceId.toString())
                    }

                    // 登录InterCome
                    InterComeHelp.INSTANCE.successfulLogin(
                        map = mapOf(),
                        interComeUserId = data?.externalId,
                        userInfo = data
                    )

                    // 判断是否是帐篷\是帐篷 true 帐篷 false abby box
                    setZp(data?.spaceType != ListDeviceBean.KEY_SPACE_TYPE_BOX)

                    // 如果是帐篷那么就不请求未读数量、包括日历、interCome、环信数量
                    if (data?.spaceType != ListDeviceBean.KEY_SPACE_TYPE_BOX) {
                        // 从聊天退出来之后需要刷新消息环信数量
                        mViewMode.getHomePageNumber()
                        ViewUtils.setGone(binding.pplantNinth.ivDoorLockStatus)
                    }

                    // 获取氧气币列表
                    mViewMode.getOxygenCoinList()

                    // 环信消息
                    getEaseUINumber()

                    // 设置TuYaDeviceId
                    data?.deviceId?.let { mViewMode.setDeviceId(it) }

                    // 保存当前的信息.
                    GSON.toJson(data)?.let {
                        logI("refreshToken: $it")
                        Prefs.putStringAsync(Constants.Login.KEY_LOGIN_DATA, it)
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
                                    // 请求环境信息
                                    mViewMode.getEnvData()
                                    // 检查固件
                                    checkOtaUpdateInfo()
                                }

                                else -> {}
                            }
                        }
                    } else {
                        // 没绑定的状态下，显示这玩意
                        firstLoginViewVisibile()
                        // 跳转到绑定设备界面，
                        // 跳转绑定界面
                        //                        ARouter.getInstance()
                        //                            .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                        //                            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        //                            .navigation()
                    }
                }
            })
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
                    data?.let {
                        PlantCheckHelp().plantStatusCheck(
                            activity,
                            it,
                            true,
                            isLeftSwapAnim = mViewMode.isLeftSwap,
                            isNoAnim = false
                        )
                    }
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
                                    /*data?.learnMoreId?.let { it1 ->
                                        mViewMode.getDetailByLearnMoreId(
                                            it1
                                        )
                                    }*/

                                    // 跳转到interCome
                                    InterComeHelp.INSTANCE.openInterComeSpace(
                                        InterComeHelp.InterComeSpace.Article,
                                        data?.articleId
                                    )
                                }
                            }
                        }
                    }
                }
            })

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
                if (isZp.value == true) return@observe
                setWaterStatus(it)
                if (isManual != true) return@observe
                mViewMode.setWaterLevel(it)
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
                            if (netWorkVersion.safeToInt() > localVersion.safeToInt()) {
                                versionPop?.setData(versionData)
                                versionUpdatePop.show()
                            }
                        }
                    }
                }
            })

            // 获取氧气币列表
            getOxygenCoinList.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (mViewMode.isZp.value == true) return@success
                    ViewUtils.setVisible(data?.size != 0, binding.pplantNinth.waterView)
                    if (data == null) return@success

                    // 判断什么时候才显示氧气币，应该是没气泡的时候
                    if (unreadMessageList.value?.size == 0) {
                        // 显示氧气币
                        /*data?.map {
                            Water(it.oxygen.toString(), it.tips).apply {
                                this.loseEfficacy = it.loseEfficacy.toString()
                                this.orderNo = it.orderNo
                                this.oxygen = it.oxygen.toString()
                                this.tips = it.tips
                            }
                        }.let {
                            // 加载水珠
                            binding.pplantNinth.waterView.apply {
                                setDestroyPoint(WaterView.VIEW_SELF)
                                setViewAnimation(WaterView.TRANSLATE)
                                setTextSize(12)
                                setTextColor(Color.parseColor("#008961"))
                                setImageRes(R.drawable.group4)
                                setLayoutStyle(WaterView.FAN_RANDOM)
                                setWaters(it)
                            }
                        }*/

                        if (data?.size == 0) return@success
                        data?.take(7)?.map {
                            Water(
                                "${it.oxygen}g",
                                it.tips,
                                it.loseEfficacy.toString(),
                                it.orderNo,
                                it.oxygen.toString(),
                                it.tips
                            )
                        }?.toMutableList()?.let {
                            if (it.size == 0) return@success
                            if (isManual == true) return@success
                            binding.pplantNinth.waterView.apply {
                                setDestroyPoint(WaterView.VIEW_CENTER)
                                setDrawablePosition(WaterView.DRAWABLE_TOP)
                                setViewAnimation(WaterView.TRANSLATE)
                                setTextColor(Color.parseColor("#008961"))
                                setImageRes(R.drawable.group4)
                                setLayoutStyle(WaterView.FAN_RANDOM)
                                setWaters(it)
                            }
                        }
                    }

                    // 点击事件
                    binding.pplantNinth.waterView.apply {
                        setClickListener { view, position, water, mViews ->
                            if (isManual == true) return@setClickListener
                            synchronized(context) {
                                // 这块toInt会格式化错误
                                runCatching {
                                    setViewInterpolator(null)
                                    mViews.remove(view)
                                    animRemoveView(view, position)
                                    val oxygenToAdd =
                                        runCatching { water.oxygen.safeToInt() }.getOrDefault(0)
                                    val currentOxygen = runCatching {
                                        binding.pplantNinth.tvOxy.text.toString().safeToInt()
                                    }.getOrDefault(0)
                                    binding.pplantNinth.tvOxy.text =
                                        "${oxygenToAdd + currentOxygen}"
                                    // 直接在次加载氧气币
                                    setViewCount(mViews.size)
                                    logI("mmview: ${mViews.size}")

                                    if (water.loseEfficacy != "1") {
                                        getOxygenCoin(water.orderNo)
                                    }
                                }
                            }
                        }
                    }
                }
            })

            getOxygenCoin.observe(viewLifecycleOwner, resourceObserver {
                success {
                    // 刷新氧气币
                    if (viewCount.value == 0) {
                        getOxygenCoinList()
                    }
                }
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
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
                            plantSix().show()
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
                            // 开始Clone种植
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
                    mViewMode.plantInfoLoop()
                    // 获取环境信息
                    mViewMode.getEnvData()

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
                    // 保存植物ID
                    Prefs.putStringAsync(Constants.Global.KEY_PLANT_ID, data.toString())

                    // 跳转富文本
                    val intent = Intent(context, KnowMoreActivity::class.java)
                    intent.putExtra(
                        Constants.Global.KEY_TXT_ID,
                        Constants.Fixed.KEY_FIXED_ID_A_FEW_TIPS
                    )
                    intent.putExtra(
                        BasePopActivity.KEY_FIXED_TASK_ID,
                        Constants.Fixed.KEY_FIXED_ID_A_FEW_TIPS
                    )
                    intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "Next")
                    context?.startActivity(intent)

                    // 优先跳转选择种子还是继承界面
                    // seed or clone
                    /*  ARouter.getInstance().build(RouterPath.My.PAGE_MT_CLONE_SEED)
                          .withString(Constants.Global.KEY_PLANT_ID, data)
                          .withBoolean(Constants.Global.KEY_IS_SHOW_CHOOSER_TIPS, true)
                          .navigation(activity, KEY_FOR_CLONE_RESULT)*/
                }
            })

            // 循环获取植物基本信息
            plantInfoLoop.observe(viewLifecycleOwner, resourceObserver {
                success {
                    if (null == data) return@success
                    // 植物的休息照片
                    // 植物的健康程度
                    binding.pplantNinth.tvHealthStatus.text = data?.healthStatus ?: "----"

                    // 显示温湿度
                    showTemp()
                }
            })

            // 获取植物基本信息
            plantInfo.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    //  不是abby不走这一块。
                    //  这块是帐篷的处理逻辑
                    if (mViewMode.isZp.value == true) {
                        // 隐藏种植水的图片
                        ViewUtils.setVisible(isZp.value == false, binding.pplantNinth.ivWaterStatus)
                        ViewUtils.setVisible(isZp.value == false, binding.pplantNinth.clSeeding)
                        // 设置帐篷空间名字
                        binding.pplantNinth.tvTitle.text = data?.spaceName

                        // 设置帐篷的光照时间
                        // binding.pplantNinth.tvOxy.text = "${data?.oxygen ?: "---"}"
                        val startTime = data?.lightOn ?: 0
                        val endTime = data?.lightOff ?: 0
                        binding.pplantNinth.tvOxy.text = """
                            ${data?.lightOnOff ?: "---"}
                            ON  ${
                            when (startTime) {
                                0 -> "12 AM"
                                12 -> "12 PM"
                                else -> "${if (startTime > 12) startTime - 12 else startTime} ${if (startTime > 12) "PM" else "AM"}"
                            }
                        }
                            OFF ${
                            when (endTime) {
                                0 -> "12 AM"
                                12 -> "12 PM"
                                else -> "${if (endTime > 12) endTime - 12 else endTime} ${if (endTime > 12) "PM" else "AM"}"
                            }
                        }
                        """.trimIndent()

                        // 设置周期
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
                            }

                        // 植物的period 周期
                        data?.list?.let {
                            mViewMode.setPeriodList(it)
                        }
                        return@success
                    }

                    // 显示温湿度
                    showTemp()

                    // 2. 【获取植物基本信息】接口新增字段 [发芽剩余时间]，类型为时间戳，0为倒计时结束，该字段只在发芽阶段返回，点了Next后返回为NULL。
                    ViewUtils.setVisible(
                        data?.germinationTime?.isNotEmpty() == true && (data?.germinationTime?.toLong()
                            ?: 0) > 0, binding.pplantNinth.clSeeding
                    )
                    if (data?.germinationTime?.isNotEmpty() == true && (data?.germinationTime?.toLong()
                            ?: 0) > 0
                    ) {
                        // 后台返回的时间
                        val backTime = data?.germinationTime?.toLong() ?: 0L
                        binding.pplantNinth.tVHtml.text = buildSpannedString {
                            /*Check for a tap root in 1 day(s) 23 hrs... Lights should be off at this stage*/
                            /*Check the seed status in 24 hours (倒计时).Thr grow light is off during this stage.*/
                            bold { append("Check the seed status in") }
                            appendLine()
                            context?.let {
                                ContextCompat.getColor(
                                    it,
                                    com.cl.common_base.R.color.textRed
                                )
                            }?.let {
                                color(it) {
                                    logI("1231231: ${System.currentTimeMillis()}")
                                    bold {
                                        append(
                                            DateHelper.getDistanceTime(
                                                System.currentTimeMillis(),
                                                backTime,
                                                "day",
                                                "hr",
                                                "min",
                                                "minute"
                                            )
                                        )
                                    }
                                }
                            }
                            appendLine()
                            bold { append("Thr grow light is off during this stage.") }
                        }

                        // 按钮的背景颜色
                        if (DateHelper.after(System.currentTimeMillis(), backTime)) {
                            binding.pplantNinth.btnCheck.setBackgroundResource(com.cl.common_base.R.drawable.background_button_main_color_r100)
                        } else {
                            binding.pplantNinth.btnCheck.setBackgroundResource(com.cl.common_base.R.drawable.background_button_gray_r100)
                        }

                        binding.pplantNinth.btnSkip.setOnClickListener {
                            context?.let {
                                pop.isDestroyOnDismiss(false).dismissOnTouchOutside(false)
                                    .asCustom(
                                        BaseCenterPop(it,
                                            content = "Do you want to skip the germination wait?",
                                            isShowCancelButton = true,
                                            confirmText = "Confirm",
                                            onConfirmAction = {
                                                it.startActivity(Intent(it, SeedCheckActivity::class.java))
                                            })
                                    ).show()
                            }
                        }
                        binding.pplantNinth.btnCheck.setOnClickListener {
                            // 需要判断时间超过48小时了没。
                            if (DateHelper.before(System.currentTimeMillis(), backTime)) {
                                ToastUtil.shortShow("it is not time yet")
                                return@setOnClickListener
                            }
                            context?.let {
                                it.startActivity(Intent(it, SeedCheckActivity::class.java))
                            }
                            /*context?.let {
                                pop.isDestroyOnDismiss(false).dismissOnTouchOutside(false)
                                    .asCustom(
                                        BaseCenterPop(it,
                                            titleText = "The seed has a tap root like this?",
                                            contentBackGround = R.mipmap.home_seed_count_down,
                                            isShowCancelButton = true,
                                            cancelText = "No",
                                            confirmText = "Yes",
                                            onConfirmAction = {
                                                // 跳准到富文本页面
                                                val intent =
                                                    Intent(context, KnowMoreActivity::class.java)
                                                intent.putExtra(
                                                    BasePopActivity.KEY_IS_SHOW_BUTTON,
                                                    true
                                                )
                                                intent.putExtra(
                                                    BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT,
                                                    "Next"
                                                )
                                                intent.putExtra(
                                                    BasePopActivity.KEY_INTENT_JUMP_PAGE,
                                                    true
                                                )
                                                intent.putExtra(
                                                    Constants.Global.KEY_TXT_ID,
                                                    Constants.Fixed.KEY_FIXED_ID_ACTION_NEEDED
                                                )
                                                intent.putExtra(
                                                    BasePopActivity.KEY_FIXED_TASK_ID,
                                                    Constants.Fixed.KEY_FIXED_ID_ACTION_NEEDED
                                                )
                                                startActivityLauncherCheck.launch(intent)
                                            },
                                            onCancelAction = {
                                                // 删除植物弹窗
                                                pop.isDestroyOnDismiss(false)
                                                    .dismissOnTouchOutside(false)
                                                    .asCustom(RestartSeedPop(it, onDeletePlant = {
                                                        tuYaUser?.uid?.let { uid -> plantDelete(uid) }
                                                    })).show()
                                            })
                                    ).show()
                            }*/
                        }
                        // 发芽倒计时
                        /*pop.isDestroyOnDismiss(false).dismissOnTouchOutside(false)
                            .asCustom(
                                context?.let {
                                    GerminationCountdownPop(it, data,
                                        onSkipAction = {
                                        }, onCheckAction = {
                                        })
                                }
                            ).show()*/
                    }

                    val isShowGuidePop =
                        Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_GUIDE_POP, false)
                    if (!isShowGuidePop && isManual == false) {
                        // 只展示一次
                        Prefs.putBoolean(Constants.Global.KEY_IS_SHOW_GUIDE_POP, true)
                        // 显示三种隐藏提示
                        bubblePopHor
                            .popupPosition(PopupPosition.Bottom)
                            .atView(binding.pplantNinth.ivCalendar)
                            .asCustom(context?.let {
                                BaseGuidePop(
                                    it,
                                    confirmText = "Click here to access your growing calendar and view upcoming tasks",
                                    onConfirmAction = {
                                        // 如果不是会员那么不需要展示后面2个
                                        if (mViewMode.userDetail.value?.data?.isVip != 1) return@BaseGuidePop
                                        bubblePopHor
                                            .popupPosition(PopupPosition.Top)
                                            .atView(binding.pplantNinth.ivSupport)
                                            .asCustom(
                                                BaseGuidePop(
                                                    it,
                                                    confirmText = "You can access 1-on-1 support and chat with our growing experts here",
                                                    onConfirmAction = {
                                                        // todo 暂时没有朋友圈，所以暂时不弹出这个guide弹窗
                                                    }).setBubbleBgColor(Color.WHITE)
                                            ).show()
                                    }).setBubbleBgColor(Color.WHITE)  //气泡背景
                            })
                            .show()
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

                            // 植物信息数据显示
                            binding.plantManual.tvWeekDay.text = """
                                Week ${data?.week ?: "-"} Day ${data?.day ?: "-"}
                            """.trimIndent()

                            ViewUtils.setVisible(
                                info.journeyName != HomePeriodPop.KEY_SEED,
                                binding.pplantNinth.ivWaterStatus
                            )
                            // 显示碗or植物
                            getCameraFlag { isHave, isLoadCamera, cameraId, devId ->
                                if ((isHave && isLoadCamera) || mViewMode.isZp.value == true) {
                                    binding.pplantNinth.ivBowl.visibility = View.INVISIBLE
                                } else {
                                    binding.pplantNinth.ivBowl.visibility = View.VISIBLE
                                }

                                // 如果是帐篷、并且没有摄像头时。
                                val isZpEnabled = mViewMode.isZp.value == true

                                if (isZpEnabled && !isHave) {
                                    ViewUtils.setVisible(binding.pplantNinth.rlBowl)
                                    ViewUtils.setGone(binding.pplantNinth.cameraVideoView, binding.pplantNinth.ivZpBg)
                                    ViewUtils.setVisible(binding.pplantNinth.ivZpCamera, binding.pplantNinth.ivSwitchZpBg)
                                } else {
                                    val shouldShowSwitch = isHave && isLoadCamera && isZpEnabled
                                    ViewUtils.setVisible(shouldShowSwitch, binding.pplantNinth.ivSwitchZpBg)

                                    val shouldShowIvZpBg = ((isHave && !isLoadCamera) || !isHave) && isZpEnabled
                                    ViewUtils.setVisible(shouldShowIvZpBg, binding.pplantNinth.ivZpBg)
                                }
                            }
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
                                        if (data?.cupType == 1) {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_five_plast
                                                )
                                            }
                                        } else {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_five
                                                )
                                            }
                                        }
                                    }

                                    8, 9 -> {
                                        if (data?.cupType == 1) {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_six_plast
                                                )
                                            }
                                        } else {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it, com.cl.common_base.R.mipmap.home_seed_bg_six
                                                )
                                            }
                                        }
                                    }

                                    10, 11 -> {
                                        if (data?.cupType == 1) {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_seven_plast
                                                )
                                            }
                                        } else {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_seven
                                                )
                                            }
                                        }
                                    }

                                    12 -> {
                                        if (data?.cupType == 1) {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_eight_plast
                                                )
                                            }
                                        } else {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_eight
                                                )
                                            }
                                        }
                                    }

                                    else -> {
                                        if (data?.cupType == 1) {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_eight_plast
                                                )
                                            }
                                        } else {
                                            context?.let {
                                                ContextCompat.getDrawable(
                                                    it,
                                                    com.cl.common_base.R.mipmap.home_seed_bg_eight
                                                )
                                            }
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
                                                it, com.cl.common_base.R.mipmap.home_week_one
                                            )
                                        }
                                    }

                                    2 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_two
                                            )
                                        }
                                    }

                                    3 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_three
                                            )
                                        }
                                    }

                                    4 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_four
                                            )
                                        }
                                    }

                                    5 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_five
                                            )
                                        }
                                    }

                                    6 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_six
                                            )
                                        }
                                    }

                                    7 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_seven
                                            )
                                        }
                                    }

                                    8 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_eight
                                            )
                                        }
                                    }

                                    9 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_nine
                                            )
                                        }
                                    }

                                    10 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_ten
                                            )
                                        }
                                    }

                                    11 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_eleven
                                            )
                                        }
                                    }

                                    12 -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_twelve
                                            )
                                        }
                                    }

                                    else -> {
                                        context?.let {
                                            ContextCompat.getDrawable(
                                                it, com.cl.common_base.R.mipmap.home_week_one
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

                            // 获取植物高度
                            mViewMode.getPlantHeight()
                            // 植物的点击弹出植物高度。
                            binding.pplantNinth.ivBowl.setQuickClickListener {
                                // 在种子阶段，是不能点击的。
                                if (binding.pplantNinth.clPlantHeight.isVisible) return@setQuickClickListener
                                if (info.journeyName == UnReadConstants.PeriodStatus.KEY_SEED || info.journeyName == UnReadConstants.PeriodStatus.KEY_GERMINATION) return@setQuickClickListener
                                val currentDevice = listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }
                                if ((currentDevice?.deviceType != "OG") || (currentDevice?.deviceType != "OG_black")) return@setQuickClickListener

                                // 隐藏氧气币，和气泡
                                ViewUtils.setGone(binding.pplantNinth.clContinue, binding.pplantNinth.waterView)

                                // 植物高度。
                                binding.pplantNinth.tvPlantHeight.text = formatIncPlant(mViewMode.plantHeights.value)

                                // 开始若隐若现动画
                                startFadeInOutAnimation(binding.pplantNinth.clPlantHeight, 5000) // 500ms为动画周期，可根据需求调整

                                // 设置5秒后执行的操作
                                binding.pplantNinth.clPlantHeight.postDelayed({
                                    // 停止动画并隐藏View
                                    stopFadeInOutAnimation(binding.pplantNinth.clPlantHeight)
                                    ViewUtils.setVisible((unreadMessageList.value?.size ?: 0) > 0, binding.pplantNinth.clContinue)
                                    ViewUtils.setVisible((getOxygenCoinList.value?.data?.size ?: 0) > 0, binding.pplantNinth.waterView)
                                }, 5000) // 5秒后执行
                            }
                        }


                    // 植物的氧气
                    binding.pplantNinth.tvOxy.text = "${data?.oxygen ?: "---"}"

                    // 植物的名字
                    binding.pplantNinth.tvTitle.text = data?.plantName
                    binding.plantManual.tvTitle.text = data?.plantName

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
                error { errorMsg, code ->
                    hideProgressLoading()
                    // 植物不存在时，检查植物是否种植。
                    if (code == 1001) {
                        mViewMode.checkPlant()
                    }
                    /*errorMsg?.let { ToastUtil.shortShow(it) }*/
                }
            })

            // 获取环境信息
            environmentInfo.observe(viewLifecycleOwner, resourceObserver {
                success {
                    // 弹出环境框
                    // 需要经过解析，才能给到data
                    data?.let { dataItem ->
                        val originalSize = dataItem.environments?.size ?: 0
                        val filteredList = mutableListOf<EnvironmentInfoData.Environment>()
                        val list = listOf(EnvironmentInfoData.KEY_TYPE_WATER_TEMPERATURE_TYPE, EnvironmentInfoData.KEY_TYPE_WATER_LEVEL_TYPE, EnvironmentInfoData.KEY_TYPE_HUMIDITY_TYPE, EnvironmentInfoData.KEY_TYPE_TEMPERATURE_TYPE)
                        // Add new data and modify original data
                        for (j in 0 until originalSize) {
                            val insertIndex = j * 2
                            val newTextDesc = dataItem.environments?.get(insertIndex)?.detectionValue  // Use j to get the original item

                            // Insert new data
                            dataItem.environments?.add(insertIndex, EnvironmentInfoData.Environment(environmentType = EnvironmentInfoData.KEY_SPACE_TYPE_TEXT, textDesc = newTextDesc, type = dataItem.environments?.get(insertIndex)?.environmentType))

                            // Modify original data
                            dataItem.environments?.get(insertIndex + 1)?.apply {
                                /*when (environmentType) {
                                    EnvironmentInfoData.KEY_TYPE_FAN_TYPE -> type = EnvironmentInfoData.KEY_TYPE_FAN_TYPE
                                    EnvironmentInfoData.KEY_TYPE_LIGHT_TYPE -> type = EnvironmentInfoData.KEY_TYPE_LIGHT_TYPE
                                }*/

                                // Check for specific detectionValue and add to filteredList
                                if (environmentType in list) {
                                    filteredList.add(this)
                                }
                            }
                        }

                        // Insert filteredList at the beginning
                        dataItem.environments?.add(0, EnvironmentInfoData.Environment(environmentType = EnvironmentInfoData.KEY_SPACE_TYPE_GRID, additionalData = filteredList))

                        // Remove elements with specific environmentType\type
                        dataItem.environments?.removeIf { env ->
                            env.environmentType in list || env.type in list
                        }

                        // Update UI or data display
                        dataItem.environments?.let {
                            envirPop?.setData(it, mViewMode.listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }, mViewMode.userDetail.value?.data)
                        }
                    }

                    envirPop?.setStrainName(mViewMode.plantInfo.value?.data?.plantName)
                }
                error { errorMsg, _ ->
                    // errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

            // 删除植物回调
            plantDelete.observe(viewLifecycleOwner, resourceObserver {
                success {
                    // 删除植物、需要更新信息。
                    mViewMode.checkPlant()
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
                    ViewUtils.setVisible(getOxygenCoinList.value?.data?.size != 0 && isZp.value == false, binding.pplantNinth.waterView)
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
                            plantSix().show()
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

    private fun Resource.Success<PlantInfoData>.showTemp() {
        data?.apply {
            // Ensure envirVO is not null before proceeding.
            val environment = if (null == envirVO) {
                ViewUtils.setVisible(null == data?.envirVO && mViewMode.isZp.value == true, binding.pplantNinth.ivZpAdd)
                return
            } else {
                envirVO
            }

            // Get the user preference for the unit system once.
            val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
            val tempUnit = if (isMetric) "℃" else "℉"
            val humidityUnit = "%"

            // Perform temperature and humidity conversions.
            val roomTemp = environment?.roomTemp?.let { temperatureConversion(it.safeToFloat(), isMetric) } ?: ""
            val roomHumidity = environment?.roomHumiture ?: ""
            val temp = environment?.temp?.let { temperatureConversion(it.safeToFloat(), isMetric) } ?: ""
            val humidity = environment?.humiture ?: ""

            // Update the UI elements.
            with(binding) {
                // Set visibility based on the availability of temperature or humidity data.
                val isDataAvailable = (temp.isNotEmpty() || humidity.isNotEmpty() || roomTemp.isNotEmpty() || roomHumidity.isNotEmpty())
                if (mViewMode.isZp.value == true) {
                    if (isDataAvailable) {
                        pplantNinth.ivZpAdd.visibility = View.GONE
                    } else {
                        pplantNinth.ivZpAdd.visibility = View.VISIBLE
                    }
                } else {
                    pplantNinth.ivZpAdd.visibility = View.GONE
                }

                // 获取温度传感器值 ProMode
                plantManual.tvTemperatureValue.text = mViewMode.temperatureConversionForTemp(mViewMode.getWenDu.value)
                plantManual.tvHumidityValue.text = mViewMode.getRoomHumidity(mViewMode.getHumidity.value)

                // Update the temperature and humidity values.
                pplantNinth.tentHealthStatus.text = buildSpannedString {
                    if (temp.isNotEmpty() && humidity.isNotEmpty()) {
                        append("$temp$tempUnit $humidity$humidityUnit")
                    }
                }

                pplantNinth.tentHealthStatusSmall.text = buildSpannedString {
                    if (roomTemp.isNotEmpty() && roomHumidity.isNotEmpty()) {
                        append("Room $roomTemp$tempUnit $roomHumidity$humidityUnit")
                    }
                }

                // Adjust the text size if the tent health status is empty.
                if (pplantNinth.tentHealthStatus.text.isEmpty()) {
                    pplantNinth.tentHealthStatusSmall.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }
        }
    }

    /**
     * 初始化摄像头的尺寸大小
     */
    private fun initCameraSize() {
        val marginTopPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            32f,
            resources.displayMetrics
        ).safeToInt()
        val windowManager =
            activity?.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
        val width = windowManager.defaultDisplay.width - marginTopPx
        val height = width * 9 / 16
        // 假设你已经有了一个 ConstraintLayout 和一个 RelativeLayout
        val constraintLayout = binding.pplantNinth.clPlantStatus
        val relativeLayout = binding.pplantNinth.rlBowl


        // 创建一个 RelativeLayout.LayoutParams
        val layoutParams = ConstraintLayout.LayoutParams(
            width, height
        )
        // 设置布局参数
        relativeLayout.layoutParams = layoutParams
        // 创建一个 ConstraintSet
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout) // 克隆当前的约束
        // 设置新的约束
        constraintSet.connect(
            relativeLayout.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            relativeLayout.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(
            relativeLayout.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        // 应用新的约束
        constraintSet.applyTo(constraintLayout)

        // Assuming you are using Kotlin for Android

        // Set the corner radius for the bottom left and bottom right corners
        val cornerRadius = 30.0f
        // 角度为顺时针旋转绘制的，
        val radii =
            floatArrayOf(0f, 0f, 0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius)

        // val radii = floatArrayOf(0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f)
        val shapeDrawable = ShapeDrawable(RoundRectShape(radii, null, null))
        binding.pplantNinth.cardView.clipToOutline = true
        binding.pplantNinth.cardView.background = shapeDrawable
    }

    /**
     * 初始化摄像头
     */
    private var mCameraP2P: IThingSmartCameraP2P<Any>? = null
    private var isPlay = false
    private fun initCamera(cameraId: String) {
        if (null == mCameraP2P || mViewMode.cameraId.value != cameraId) {
            ThingIPCSdk.getCameraInstance()?.let {
                mCameraP2P = it.createCameraP2P(cameraId)
            }
            binding.pplantNinth.cameraVideoView.setViewCallback(object : AbsVideoViewCallback() {
                override fun onCreated(o: Any) {
                    super.onCreated(o)
                    mCameraP2P?.generateCameraView(o)
                }
            })
            binding.pplantNinth.cameraVideoView.createVideoView(cameraId)
            if (mCameraP2P == null) {
                // ToastUtil.shortShow("Camera initialization failed")
                 return
            }
        }

        binding.pplantNinth.cameraVideoView.onResume()
        //must register again,or can't callback
        goOnCameraPlay()
    }


    /**
     * 检查是否可以继续显示摄像数据
     */
    private fun goOnCameraPlay() {
        mCameraP2P?.let {
            it.registerP2PCameraListener(p2pCameraListener)
            it.generateCameraView(binding.pplantNinth.cameraVideoView.createdView())
            if (it.isConnecting) {
                it.startPreview(object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        isPlay = true
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        Log.d(TAG, "start preview onFailure, errCode: $errCode")
                    }
                })
            } else {
                /* if (ThingIPCSdk.getCameraInstance()?.isLowPowerDevice(mViewMode.cameraId.value) == true) {
                     ThingIPCSdk.getDoorbell()?.wirelessWake(mViewMode.cameraId.value)
                 }*/
                //Establishing a p2p channel
                it.connect(mViewMode.cameraId.value, object : OperationDelegateCallBack {
                    override fun onSuccess(i: Int, i1: Int, s: String) {
                        activity?.runOnUiThread {
                            preview()
                        }
                    }

                    override fun onFailure(i: Int, i1: Int, i2: Int) {
                        activity?.runOnUiThread {
                            //ToastUtil.shortShow(getString(com.tuya.smart.android.demo.camera.R.string.connect_failed))
                            mViewMode.tuYaUtils.queryValueByDPID("", DPConstants.PRIVATE_MODE)
                        }
                    }
                })
            }
        }
    }

    var reConnect = false
    private val p2pCameraListener: AbsP2pCameraListener = object : AbsP2pCameraListener() {
        override fun onReceiveSpeakerEchoData(pcm: ByteBuffer, sampleRate: Int) {
            mCameraP2P?.let {
                val length = pcm.capacity()
                Log.d(TAG, "receiveSpeakerEchoData pcmlength $length sampleRate $sampleRate")
                val pcmData = ByteArray(length)
                pcm[pcmData, 0, length]
                it.sendAudioTalkData(pcmData, length)
            }
        }

        override fun onSessionStatusChanged(camera: Any?, sessionId: Int, sessionStatus: Int) {
            super.onSessionStatusChanged(camera, sessionId, sessionStatus)
            if (sessionStatus == -3 || sessionStatus == -105) {
                // 遇到超时/鉴权失败，建议重连一次，避免循环调用
                if (!reConnect) {
                    reConnect = true
                    mCameraP2P?.connect(
                        mViewMode.cameraId.value,
                        object : OperationDelegateCallBack {
                            override fun onSuccess(i: Int, i1: Int, s: String) {
                                activity?.runOnUiThread {
                                    preview();
                                }
                            }

                            override fun onFailure(i: Int, i1: Int, i2: Int) {
                                activity?.runOnUiThread {
                                    //ToastUtil.shortShow(getString(com.tuya.smart.android.demo.camera.R.string.connect_failed))
                                }
                            }
                        })
                }
            }
        }

        override fun onReceiveFrameYUVData(
            i: Int,
            byteBuffer: ByteBuffer,
            byteBuffer1: ByteBuffer,
            byteBuffer2: ByteBuffer,
            i1: Int,
            i2: Int,
            i3: Int,
            i4: Int,
            l: Long,
            l1: Long,
            l2: Long,
            o: Any,
        ) {
            super.onReceiveFrameYUVData(
                i,
                byteBuffer,
                byteBuffer1,
                byteBuffer2,
                i1,
                i2,
                i3,
                i4,
                l,
                l1,
                l2,
                o
            )
            /*if (adapters?.focusedPosition?.let { adapters?.getLetter(it) } == "PLAYBACK") {
                binding.timeline.setCurrentTimeInMillisecond(l * 1000L)
            }*/
        }
    }


    /**
     * 初始化摄像头画面
     */
    private fun preview() {
        mCameraP2P?.startPreview(ICameraP2P.HD, object : OperationDelegateCallBack {
            override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                // 查看是否每天需要拍一张照片，
                activity?.runOnUiThread {
                    isScreenshots()
                }
                Log.d(TAG, "start preview onSuccess")
                isPlay = true
            }

            override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                Log.d(TAG, "start preview onFailure, errCode: $errCode")
                isPlay = false
            }
        })
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
        mViewMode.setShouldRunJob(true)
        startCountDownJob()
        // 刷新数据
        mViewMode.userDetail()
        // 刷新设备列表
        mViewMode.listDevice()
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { v, insets ->
            binding.clRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
        queryAllDp()
    }

    private fun queryAllDp() {
        GSON.toJson(AllDpBean(cmd = "2"))?.let { DeviceControl.get().success { }.error { code, error -> }.sendDps(it, mViewMode.thingDeviceBean()?.devId) }
    }

    override fun onPause() {
        super.onPause()
        mViewMode.setShouldRunJob(false)
        job?.cancel()
        cameraStopOnpause()
    }

    private fun cameraStopOnpause() {
        mViewMode.getCameraFlag { isHave, isLoadCamera, cameraId, devId ->
            if (!isHave) return@getCameraFlag
            binding.pplantNinth.cameraVideoView.onPause()
            mCameraP2P?.let { p2p ->
                if (isPlay) {
                    p2p.stopPreview(object : OperationDelegateCallBack {
                        override fun onSuccess(sessionId: Int, requestId: Int, data: String) {}
                        override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {}
                    })
                    isPlay = false
                }
                p2p.removeOnP2PCameraListener()
                p2p.disconnect(object : OperationDelegateCallBack {
                    override fun onSuccess(i: Int, i1: Int, s: String) {}
                    override fun onFailure(i: Int, i1: Int, i2: Int) {}
                })
            }
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
        if (unReadList.isNullOrEmpty() || mViewMode.isZp.value == true) {
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
            if (UnReadConstants.Device.KEY_CHANGE_CUP_WATER == unRead?.type || UnReadConstants.Device.KEY_BURN_OUT_PROOF == unRead?.type) {
                "Go"
            } else if (UnReadConstants.plantStatus.contains(unRead?.type)) {
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
                "Done"
            } else if (unRead?.type == UnReadConstants.JumpType.KEY_PREVENT_DRY_BURNING || unRead?.type == UnReadConstants.JumpType.KEY_CLOSE_DOOR) {
                "Done"
            } else {
                ""
            }.toString()

        // 新的气泡如果需要解锁周期那么就需要显示红点
        // 隐藏周期右上角红点
        /*ViewUtils.setVisible(
            UnReadConstants.plantStatus.contains(unRead?.type), binding.pplantNinth.ivNewRed
        )*/

        // 如果jumpType == none 就不显示按钮
        /*ViewUtils.setVisible(
            unRead?.jumpType != UnReadConstants.JumpType.KEY_NONE, binding.pplantNinth.tvBtnDesc
        )*/
        if (unRead?.jumpType == UnReadConstants.JumpType.KEY_NONE) {
            if (unRead.type == UnReadConstants.JumpType.KEY_PREVENT_DRY_BURNING || unRead.type == UnReadConstants.JumpType.KEY_CLOSE_DOOR) {
                ViewUtils.setVisible(binding.pplantNinth.tvBtnDesc)
            } else {
                ViewUtils.setGone(binding.pplantNinth.tvBtnDesc)
            }
        }  else {
            ViewUtils.setVisible(binding.pplantNinth.tvBtnDesc)
        }

        // 内容
        binding.pplantNinth.tvPopTitle.text =
            if (unRead?.extension?.contains(UnReadConstants.Extension.KEY_EXTENSION_CONTINUE) == true) {
                "You have Job to do."
            } else {
                unRead?.title
            }

        // 是否显示取消按钮
        /*binding.pplantNinth.ivClose.visibility =
            if (UnReadConstants.noCancel.contains(unRead?.type)) View.GONE else View.VISIBLE*/
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
                mViewMode.userDetail.value?.data?.let { data ->
                    // 如果时绑定状态，并且是离线，表示是第一次进来，设备就是离线的。
                    // 当他在线时，就需要刷新状态
                    if (data.deviceStatus == "1") {
                        if (data.deviceOnlineStatus == "0") {
                            mViewMode.userDetail()
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
                        plantSix().show()
                    }
                }
            } else if (extension == UnReadConstants.Extension.KEY_EXTENSION_CONTINUE_THREE) {
                //  加肥的弹窗
                plantSix().show()
            } else {
                logI("specificStep")
            }
        }
    }

    /**
     * 设备指令监听
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onTuYaToAppDataChange(status: String) {
        val map = GSON.parseObject(status, Map::class.java)
        map?.forEach { (key, value) ->
            when (key) {
                // 当用户加了水，是需要动态显示当前水的状态的
                // 返回多少升水
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_STATUS_INSTRUCTIONS -> {
                    logI("KEY_DEVICE_WATER_STATUS： $value")
                    mViewMode.setWaterVolume(value.toString())
                    mViewMode.tuYaDps?.put(
                        TuYaDeviceConstants.KEY_DEVICE_WATER_LEVEL,
                        value.toString()
                    )
                }

                // 排水结束
                TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                    if (isManual != true) return
                    /*binding.plantManual.ivDrainStatus.background =
                        resources.getDrawable(
                            R.mipmap.home_drain_start,
                            context?.theme
                        )*/
                }
                // 排水暂停
                TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_INSTRUCTIONS -> {
                    if (isManual != true) return
                    binding.plantManual.ivDrainStatus.background =
                        if ((value as? Boolean != true)) {
                            resources.getDrawable(
                                R.mipmap.home_drain_start,
                                context?.theme
                            )
                        } else {
                            resources.getDrawable(
                                R.mipmap.home_drain_pause,
                                context?.theme
                            )
                        }
                }

                // SN修复的通知
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_SN_INSTRUCTION -> {
                    logI("KEY_DEVICE_REPAIR_SN： $value")
                }

                // 获取SN的通知
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_REST_STATUS_INSTRUCTION -> {
                    logI("KEY_DEVICE_REPAIR_REST_STATUS： $value")
                    mViewMode.saveSn(value.toString().split("#")[1])
                }

                // 童锁开关
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_CHILD_LOCK_INSTRUCT -> {
                    mViewMode.setChildLockStatus(value.toString())
                }

                // 打开门
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_DOOR_LOOK_INSTRUCT -> {
                    mViewMode.setOpenDoorStatus(value.toString())
                }

                // 是否关闭门
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_DOOR -> {
                    logI("12312312312 KEY_DEVICE_DOOR: $value")
                    // 是否关闭门
                    mViewMode.setDoorStatus(value.toString())

                    // 摄像头相关
                    // 主要用户删除当前的door的气泡消息
                    // true 开门、 fasle 关门
                    isPrivateMode(value)

                    // 主要用户删除当前的door的气泡消息
                    // true 开门、 fasle 关门
                    if (value.toString() == "true") return
                    if (mViewMode.getUnreadMessageList()
                            .firstOrNull()?.type == UnReadConstants.Device.KEY_CLOSE_DOOR
                    ) {
                        // 点击按钮就表示已读，已读会自动查看有没有下一条
                        mViewMode.getRead(
                            "${
                                mViewMode.getUnreadMessageList().firstOrNull()?.messageId
                            }"
                        )
                    }
                }

                // ----- 开始， 下面的都是需要传给后台的环境信息
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_BRIGHT_VALUE_INSTRUCTION -> {
                    /*mViewMode.tuYaDps?.put(
                        TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE,
                        value.toString()
                    )
                    mViewMode.setCurrentGrowLight(value.toString())*/
                    // 查询灯光信息
                    queryAllDp()
                }

                // 140 dp点
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_TIME_STAMP -> {
                    val allDpBean = GSON.parseObject(value.toString(), AllDpBean::class.java)
                    // cmd == 3 返回实际灯光配置参数
                    // cmd == 1 返回实际全部配置参数
                    if (allDpBean?.cmd == "3" || allDpBean?.cmd == "1") {
                        // 这段代码必须在首位。
                        mViewMode.tuYaDps?.put(
                            TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE,
                            allDpBean.gl.toString()
                        )
                        // 更新环境信息， 灯光从黑变成亮，但是没获取环境信息，所以会造成还是为off
                        // 不等于说明灯光刷新， 更新当前环境信息的数据
                        if (isManual == false && mViewMode.getCurrentGrowLight.value != allDpBean.gl.safeToInt()) {
                            mViewMode.getEnvData()
                            mViewMode.listDevice()
                            mViewMode.userDetail()
                        }

                        // 设置灯光
                        mViewMode.setCurrentGrowLight(allDpBean.gl.toString())

                        // 显示是否展示夜间模式 不是手动模式
                        if (isManual == false) {
                            // 根据灯光来显示植物是否在睡觉，是否需要显示zzz
                            mViewMode.getCameraFlag { isHave, isLoadCamera, cameraId, devId ->
                                val isShowIvTwo = ((isHave && !isLoadCamera) || !isHave) && mViewMode.isZp.value == false && allDpBean.gl == "0"
                                ViewUtils.setVisible(
                                    isShowIvTwo,
                                    binding.pplantNinth.ivThree,
                                    binding.pplantNinth.ivTwo
                                )
                            }
                        } else {
                            // 手动模式，设置当前灯光值或者是预设值。
                            mViewMode.getCurrentProMode(mViewMode.userDetail.value?.data?.deviceId.toString())
                        }
                    }
                }

                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_HUMIDITY_CURRENT_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(
                        TuYaDeviceConstants.KEY_DEVICE_HUMIDITY_CURRENT,
                        value.toString()
                    )
                    mViewMode.setHumidity(value.toString())
                }

                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_INPUT_AIR_FLOW_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(
                        TuYaDeviceConstants.KEY_DEVICE_INPUT_AIR_FLOW,
                        value.toString()
                    )
                    mViewMode.setFanIntake(value.toString())
                }

                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_TEMP_CURRENT_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(
                        TuYaDeviceConstants.KEY_DEVICE_TEMP_CURRENT,
                        value.toString()
                    )
                    mViewMode.setWenDu(value.toString())
                }

                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_VENTILATION_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(
                        TuYaDeviceConstants.KEY_DEVICE_VENTILATION,
                        value.toString()
                    )
                    mViewMode.setFanExhaust(value.toString())
                }

                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_TEMPERATURE_INSTRUCTION -> {
                    mViewMode.tuYaDps?.put(
                        TuYaDeviceConstants.KEY_DEVICE_WATER_TEMPERATURE,
                        value.toString()
                    )
                    mViewMode.setWaterWenDu(value.toString())
                }
                // --------- 到这里结束
                // 植物高度
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_PLANT_HEIGHT_INSTRUCTION -> {
                    mViewMode.setPlantHeight(value.toString())
                }

                // 气泵
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_AIR_PUMP_INSTRUCTION -> {
                    mViewMode.setAirPump(value.toString())
                }
            }
        }
    }

    /**
     * 是否开启隐私模式
     */
    private fun isPrivateMode(value: Any?) {
        if (mViewMode.isZp.value == true) return
        mViewMode.getCameraFlag { isHave, isLoadCamera, cameraId, devId ->
            if (isHave && isLoadCamera) {
                val isOpen = value.toString() == "true"
                val isPrivate = mViewMode.getAccessoryInfo.value?.data?.privateModel == true
                logI("isPrivateMode isOpen: $isOpen, isPrivate: $isPrivate")
                ViewUtils.setVisible(isOpen && isPrivate, binding.pplantNinth.tvPrivacyMode)
                if (isOpen && isPrivate) {
                    // 打开隐私模式
                    cameraId.let {
                        mViewMode.tuYaUtils.publishDps(
                            it,
                            DPConstants.PRIVATE_MODE,
                            true
                        )
                    }
                } else {
                    // 关闭隐私模式
                    cameraId.let {
                        mViewMode.tuYaUtils.publishDps(
                            it,
                            DPConstants.PRIVATE_MODE,
                            false
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 销毁job倒计时任务
        job?.cancel()
        mCameraP2P?.destroyP2P()
    }

    /**
     * 发送支持邮件
     */
    private fun sendEmail() {
        InterComeHelp.INSTANCE.openInterComeHome()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            queryAllDp()
            mViewMode.setShouldRunJob(true)
            startCountDownJob()
            // 如果是帐篷，那么就请求这个就好了
            if (mViewMode.isZp.value == true) {
                // 会走到showView、然后会调用listDevice、plantInfo两个借口
                mViewMode.userDetail()
                return
            }

            // ABBY机器
            // 在线、并且绑定了设备
            if (mViewMode.userDetail.value?.data?.deviceStatus == "1" && mViewMode.userDetail.value?.data?.deviceOnlineStatus == "1") {
                // 如果没有绑定过设备
                // 获取用户信息
                mViewMode.userDetail()
                // 种植过的才可以请求
                mViewMode.plantInfo()
                // 环境信息
                mViewMode.getEnvData()
            }

            // 手动模式
            if (isManual == true) {
                mViewMode.apply {
                    binding.plantManual.tvInc.text = incCovert()
                    binding.plantManual.tvTemperature.text = textCovert()
                    binding.plantManual.tvWaterTemperature.text = textCovert()
                    binding.plantManual.tvPlantHeight.text = formatIncPlant(plantHeights.value)
                    binding.plantManual.tvTemperatureValue.text =
                        temperatureConversionForTemp(getWenDu.value).toString()
                    binding.plantManual.tvWaterTemperatureValue.text =
                        temperatureConversion(getWaterWenDu.value).toString()
                }
            }
        } else {
            mViewMode.setShouldRunJob(false)
            job?.cancel()
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

                /*// 主页或者离线页面跳转到设备界面
                KEY_FOR_USER_NAME -> {
                    // 切换设备
                    data?.getStringExtra(Constants.Global.KEY_IS_SWITCH_DEVICE)?.let { mViewMode.switchDevice(it) }
                }*/
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

    /**
     * 点击start跳转到富文本、然后弹窗其他窗户
     */
    private val startActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                startActivity(Intent(context, HomeNewPlantNameActivity::class.java))
            }
        }

    /**
     * 种子发芽换水跳转到富文本
     */
    private val startActivityLauncherSeeding =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                mViewMode.getRead("${mViewMode.getUnreadMessageList().firstOrNull()?.messageId}")
                mViewMode.finishTask(
                    FinishTaskReq(
                        taskId = "${
                            mViewMode.getUnreadMessageList().firstOrNull()?.taskId
                        }"
                    )
                )
                // 刷新植物信息
                mViewMode.plantInfo()
            }
        }

    /**
     * 点击Check跳转到富文本\然后需要刷新植物信息
     */
    private val startActivityLauncherCheck =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                // 刷新植物信息
                mViewMode.plantInfo()
            }
        }


    /**
     * 是否需要截图
     */
    private fun isScreenshots() {
        kotlin.runCatching {
            // todo 其实这一块还是需要判断当前是否是开门的状态，开门的状态就不需要截图了

            // 首先需要判断当前设备的截图是否开启
            val isOpenTimeLapse = Prefs.getBoolean(mViewMode.sn.value.toString(), false)
            if (!isOpenTimeLapse) return


            // 判断当前是否可以截图
            val key = mViewMode.sn.value.toString() + "test"
            val time = System.currentTimeMillis()
            val lastSnapshotTime = Prefs.getLong(key)

            // 初始化日历对象
            val currentCalendar = Calendar.getInstance().apply {
                timeInMillis = time
            }
            val lastSnapshotCalendar = Calendar.getInstance().apply {
                timeInMillis = lastSnapshotTime
            }

            // 判断今天是否已经截图
            if (lastSnapshotTime == 0L || currentCalendar.get(Calendar.YEAR) != lastSnapshotCalendar.get(
                    Calendar.YEAR
                ) || currentCalendar.get(Calendar.DAY_OF_YEAR) != lastSnapshotCalendar.get(Calendar.DAY_OF_YEAR)
            ) {
                // 如果今天还没截图，就执行截图操作并更新截图时间
                snapShotClick()
                Prefs.putLong(key, time)
            }
        }
    }

    // 创建文件夹，并且返回文件夹路径
    // 这是本地路径
    private fun createFileDir(): String {
        FileUtil.createDirIfNotExists(SDCard.getCacheDir(context) + File.separator + mViewMode.sn.value)
        // 文件路径
        return SDCard.getCacheDir(context) + File.separator + mViewMode.sn.value
    }

    /**
     * 根据后台返回的参数来判断，当前存储的路径是否在sd卡中或者相册中
     */
    private fun isExistInSdCard(): Boolean {
        // 0 是手机， 1 是相册
        // 根据后台来返回的参数来判断是否保存在相册中还是sd卡中
        return mViewMode.getAccessoryInfo.value?.data?.storageModel == 0
    }

    // 截屏
    private fun snapShotClick() {
        applyForAuthority {
            if (!it) return@applyForAuthority
            // 创建文件夹
            val picPath = createFileDir()
            // 文件名字
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            mCameraP2P?.snapshot(
                picPath,
                fileName,
                context,
                object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        if (!isExistInSdCard()) {
                            // 保存到相册
                            activity?.let { it1 ->
                                saveFileToGallery(
                                    it1,
                                    filePath = data,
                                    title = System.currentTimeMillis().toString() + ".jpg",
                                    mimeType = "image/jpeg",
                                    albumName = mViewMode.sn.value.toString()
                                )
                            }
                        }
                        logI("snapShotClick : sucess")
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        logI("snapShotClick : $errCode")
                    }
                })
        }
    }

    /**
     * 存储到相册
     */
    private fun saveFileToGallery(
        context: Context,
        filePath: String,
        title: String,
        mimeType: String,
        albumName: String
    ): Uri? {
        val file = File(filePath)
        if (!file.exists()) return null

        val isVideo = mimeType.startsWith("video")
        val contentUri =
            if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val directory = Environment.DIRECTORY_PICTURES

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, title)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$directory/$albumName")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(contentUri, contentValues)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = file.inputStream()
            outputStream = context.contentResolver.openOutputStream(uri!!)
            outputStream?.let { inputStream.copyTo(it) }
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri!!, contentValues, null, null)
        }

        return uri
    }

    /**
     *申请的权限不一致
     */
    private fun applyForAuthority(resultAction: (result: Boolean) -> Unit) {
        // 创建一个 ActivityResultLauncher 对象
        /*val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 如果权限已被授予，则执行相应的操作
                // ...
                resultAction.invoke(true)
            } else {
                // 如果权限未被授予，则提示用户
                resultAction.invoke(false)
                ToastUtil.shortShow("Permission denied")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            )
            // 请求权限
            requestPermissionLauncher.launch(permissions.toString())
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            // 请求权限
            requestPermissionLauncher.launch(permissions.toString())
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.let {
                PermissionHelp().applyPermissionHelp(
                    it,
                    getString(com.cl.common_base.R.string.profile_request_photo),
                    object : PermissionHelp.OnCheckResultListener {
                        override fun onResult(result: Boolean) {
                            resultAction.invoke(result)
                        }
                    },
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                )
            }
        } else {
            activity?.let {
                PermissionHelp().applyPermissionHelp(
                    it,
                    getString(com.cl.common_base.R.string.profile_request_photo),
                    object : PermissionHelp.OnCheckResultListener {
                        override fun onResult(result: Boolean) {
                            resultAction.invoke(result)
                        }
                    },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
        }
    }

    fun xpopup(block: XPopup.Builder.() -> Unit) {
        XPopup.Builder(context).block()
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

    private fun startToVideoPlay() {
        val intent = Intent(context, VideoPLayActivity::class.java)
        intent.putExtra(WebActivity.KEY_WEB_URL, mViewMode.userDetail.value?.data?.liveLink)
        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Live")
        startActivity(intent)
    }

    fun startFadeInOutAnimation(view: View, duration: Long) {
        view.visibility = View.VISIBLE
        val fadeInOut = AlphaAnimation(0.0f, 1.0f).apply {
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
            this.duration = duration
        }
        view.startAnimation(fadeInOut)
    }

    fun stopFadeInOutAnimation(view: View) {
        view.clearAnimation()
        view.visibility = View.GONE
    }
}

