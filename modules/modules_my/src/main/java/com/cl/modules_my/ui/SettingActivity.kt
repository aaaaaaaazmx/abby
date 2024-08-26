package com.cl.modules_my.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.*
import android.content.Intent.*
import android.net.Uri
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.BuildConfig
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.ListDeviceBean.AccessoryList
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.*
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.*
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.pop.activity.BasePumpActivity
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.cache.CacheUtil
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MySettingBinding
import com.cl.modules_my.pop.AttentionPop
import com.cl.common_base.pop.ChooseTimePop
import com.cl.modules_my.pop.EditPlantProfilePop
import com.cl.modules_my.pop.MergeAccountPop
import com.cl.common_base.bean.ModifyUserDetailReq
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.pop.NotifyPop
import com.cl.common_base.widget.littile.MyAppWidgetProvider
import com.cl.modules_my.R
import com.cl.modules_my.viewmodel.SettingViewModel
import com.cl.modules_my.widget.LoginOutPop
import com.cl.modules_my.widget.MyDeleteDevicePop
import com.cl.modules_my.widget.MyRePlantPop
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lxj.xpopup.XPopup
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.thingclips.smart.android.user.api.ILogoutCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


/**
 * 设置界面
 *
 * @author 李志军 2022-08-06 11:33
 */
@AndroidEntryPoint
@Route(path = RouterPath.My.PAGE_MY_DEVICE_SETTING)
class SettingActivity : BaseActivity<MySettingBinding>() {
    @Inject
    lateinit var mViewModel: SettingViewModel

    private val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    /* private val refreshTokenData by lazy {
         val bean = Prefs.getString(Constants.Login.KEY_REFRESH_LOGIN_DATA)
         GSON.parseObject(bean, AutomaticLoginData::class.java)
     }*/

    /**
     * 是否滚动到burner
     */
    private val isScrollToBurner by lazy {
        intent.getBooleanExtra("isScrollToBurn", false)
    }

    /**
     *  replant 弹窗
     */
    private val rePlantPop by lazy {
        XPopup.Builder(this@SettingActivity).isDestroyOnDismiss(false).enableDrag(false)
            .maxHeight(dp2px(600f)).dismissOnTouchOutside(true)
            .asCustom(MyRePlantPop(context = this@SettingActivity, onNextAction = {
                tuYaUser?.uid?.let { uid -> mViewModel.plantDelete(uid) }
            }))
    }

    /**
     * APP检测升级弹窗
     */
    private val versionUpdatePop by lazy {
        XPopup.Builder(this@SettingActivity).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false).asCustom(versionPop)
    }
    private val versionPop by lazy {
        VersionUpdatePop(this@SettingActivity)
    }

    /**
     * 添加排水弹窗
     */
    private val plantDrain by lazy {
        XPopup.Builder(this@SettingActivity).isDestroyOnDismiss(false).enableDrag(false)
            .maxHeight(dp2px(600f)).dismissOnTouchOutside(false)
            .asCustom(HomePlantDrainPop(context = this@SettingActivity, onNextAction = {
                // 请求接口
                // 传递的数据为空
                val intent = Intent(this@SettingActivity, BasePumpActivity::class.java)
                myActivityLauncher.launch(intent)
            }))
    }

    // 统一xPopUp
    private val pop by lazy {
        // 升级提示框
        XPopup.Builder(this@SettingActivity).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false)
    }

    /**
     * 排水完成弹窗
     */
    private val plantDrainFinished by lazy {
        XPopup.Builder(this@SettingActivity).isDestroyOnDismiss(false).enableDrag(false)
            .maxHeight(dp2px(600f)).dismissOnTouchOutside(false).asCustom(
                BasePumpWaterFinishedPop(this@SettingActivity)
            )
    }

    /**
     * 换水弹窗-附带其他东西
     */
    private val plantDrainNextCustomPop by lazy {
        /*BasePumpWaterPop(
            this@SettingActivity,
            { status ->
                logI(
                    """
                       BasePumpWaterPop: status:
                        $status
                    """.trimIndent()
                )
                // 涂鸦指令，添加排水功能
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
                    .pumpWater(status)
            },
            onWaterFinishedAction = {
                // 排水结束
                // 排水结束，那么直接弹出
                if (plantDrainFinished.isShow) return@BasePumpWaterPop
                plantDrainFinished.show()
            }
        )*/
    }

    override fun MySettingBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@SettingActivity
            data = mViewModel
            executePendingBindings()
        }
    }

    override fun initView() {
        // 当前版本号
        binding.ftVision.itemValue = AppUtil.appVersionName

        ViewUtils.setVisible(BuildConfig.DEBUG, binding.test)
        binding.test.setOnClickListener {
            startActivity(Intent(this@SettingActivity, TestActivity::class.java))
        }

        binding.ftSub.setPointClickListener {
            /*pop.asCustom(
                BaseCenterPop(
                    this@SettingActivity,
                    isShowCancelButton = false,
                    confirmText = "OK",
                    content = "Digital service includes 1 on 1 expert support, oxygen coins, and exclusive digital assets and deals",
                )
            ).show()*/
            InterComeHelp.INSTANCE.openInterComeSpace(
                InterComeHelp.InterComeSpace.Article,
                Constants.InterCome.KEY_INTER_COME_SERVICE
            )
        }
        binding.ftChildLock.setPointClickListener {
            /*pop.asCustom(
                BaseCenterPop(
                    this@SettingActivity,
                    isShowCancelButton = false,
                    confirmText = "OK",
                    content = "When child lock is on, the door will  lock automatically when closed. The door can then only be opened via the app",
                )
            ).show()*/
            InterComeHelp.INSTANCE.openInterComeSpace(
                InterComeHelp.InterComeSpace.Article,
                Constants.InterCome.KEY_INTER_COME_CHILD_LOCK
            )
        }
        binding.ftNight.setPointClickListener {
            /* pop.asCustom(
                 BaseCenterPop(
                     this@SettingActivity,
                     isShowCancelButton = false,
                     confirmText = "OK",
                     content = "While in night mode, notifications will be muted. Both the screen and light strip will be turned off during the specified time",
                 )
             ).show()*/
            InterComeHelp.INSTANCE.openInterComeSpace(
                InterComeHelp.InterComeSpace.Article,
                Constants.InterCome.KEY_INTER_COME_NIGHT_MODE
            )
        }
        binding.ftBurner.setPointClickListener {
            InterComeHelp.INSTANCE.openInterComeSpace(
                InterComeHelp.InterComeSpace.Article,
                Constants.InterCome.KEY_INTER_COME_BURN_PROOF
            )
            /*pop.asCustom(
                BaseCenterPop(
                    this@SettingActivity,
                    isShowCancelButton = false,
                    confirmText = "OK",
                    content = "While in night mode, notifications will be muted. Both the screen and light strip will be turned off during the specified time",
                )
            ).show()*/
        }

        binding.ftUsb.setPointClickListener {
            // 展示一下弹窗
            pop.asCustom(
                BaseCenterPop(
                    this@SettingActivity, content = """
                This option is designed to control the power of 3rd party accessories.
                You are currently connect to a hey abby smart accessory, please remove it first from device manager.
            """.trimIndent(), confirmText = getString(com.cl.common_base.R.string.string_10)
                )
            ).show()
        }

        // 是否可以操作设备相关的功能
        val isBind = mViewModel.deviceInfo?.deviceStatus == "1"
        val isOnline = mViewModel.deviceInfo?.deviceOnlineStatus == "1"
        mViewModel.setOffLine(isBind && isOnline)

        mViewModel.listDevice()


        // 是否滚动到防烧模式
        if (isScrollToBurner) {
            binding.nes.post {
                binding.nes.scrollTo(0, binding.llBurner.top)
                ObjectAnimator.ofPropertyValuesHolder(
                    binding.llBurner,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f)
                ).apply {
                    duration = 100
                    repeatCount = 2
                    interpolator = LinearInterpolator()
                    repeatMode = ValueAnimator.RESTART
                }.start()
            }
        }

    }

    override fun observe() {
        mViewModel.apply {
            updateDeviceInfo.observe(this@SettingActivity, resourceObserver {
                success {

                }

                error { errorMsg, code ->
                    // usb的事情。
                    if (uspUpdate.value == true) {
                        xpopup(this@SettingActivity) {
                            isDestroyOnDismiss(false)
                            dismissOnTouchOutside(false)
                            asCustom(
                                BaseCenterPop(
                                    this@SettingActivity,
                                    titleText = "This option is designed to control the power of 3rd party accessories.",
                                    content = errorMsg,
                                    isShowCancelButton = false,
                                    onConfirmAction = {
                                        // 报错就复原
                                        binding.ftUsb.isItemChecked = !binding.ftUsb.isItemChecked
                                    }
                                )
                            ).show()
                        }
                        setUsbUpdate(false)
                        return@error
                    }
                    ToastUtil.shortShow(errorMsg)
                }
            })

            listDevice.observe(this@SettingActivity, resourceObserver {
                success {
                    data?.firstOrNull { it.currentDevice == 1 }?.let { deviceInfo ->
                        // 保存DeviceId,涂鸦的和后台的返回不一致
                        saveDeviceId(deviceInfo.deviceId)
                        mViewModel.updateDevicesInfo(deviceInfo)

                        if (deviceInfo.deviceType == "OG_black") {
                            // 显示防烧模式
                            ViewUtils.setVisible(binding.ftBurner)
                            // 隐藏usb模式
                            ViewUtils.setGone(binding.ftUsb)
                        } else {
                            // 是否显示防烧模式
                            ViewUtils.setVisible(
                                deviceInfo.isBurnOutProof == 1 && deviceInfo.proMode != "On",
                                binding.ftBurner
                            )
                            ViewUtils.setVisible(
                                deviceInfo.burnOutProof == 1 && deviceInfo.proMode != "On",
                                binding.tvBurnerDesc
                            )
                        }

                        // 是否显示排水
                        ViewUtils.setVisible(deviceInfo.waterPump == true, binding.ftWaterTank)

                        // 是否开启usb电源模式
                        binding.ftUsb.isItemChecked = deviceInfo.smartUsbPowder == 1
                        // 防烧模式是否开启
                        binding.ftBurner.isItemChecked = deviceInfo.burnOutProof == 1

                        // 显示当前的是否是手动模式
                        binding.itemTitle.text =
                            if (deviceInfo.proMode == "On") "Pro Mode: ON" else "Pro Mode: Off"
                        binding.ftName.itemValue = deviceInfo.plantName

                        binding.ftChildLock.isItemChecked = deviceInfo.childLock == 1
                        binding.ftNight.isItemChecked = deviceInfo.nightMode == 1
                        ViewUtils.setVisible(
                            deviceInfo.nightMode == 1,
                            binding.ftTimer
                        )
                        val str = deviceInfo.nightTimer.toString()
                        val pattern = "(\\d{1,2}):\\d{2} [AP]M-(\\d{1,2}):\\d{2} [AP]M"

                        val p: Pattern = Pattern.compile(pattern)
                        val m: Matcher = p.matcher(str)
                        var openTime: String? = null
                        var closeTime: String? = null
                        if (m.find()) {
                            muteOn = m.group(1)
                            muteOff = m.group(2)
                            var onHour = muteOn?.safeToInt() ?: 0
                            var offHour = muteOff?.safeToInt() ?: 0

                            // 判断前缀是AM还是PM
                            val pattern = Pattern.compile("(PM|AM)")
                            val matcher = pattern.matcher(str)
                            var i = 0
                            var openTimeIsAmOrPm: String? = null
                            var closeTimeIsAmOrPm: String? = null
                            while (matcher.find()) {
                                val group = matcher.group()
                                if (i == 0) {
                                    if (group == "PM") {
                                        muteOn = "${(m.group(1)?.safeToInt() ?: 0) + 12}"
                                    }
                                    openTimeIsAmOrPm = if (group == "PM") "PM" else "AM"
                                    i++
                                    continue
                                }

                                if (i > 0) {
                                    if (group == "PM") {
                                        muteOff = "${(m.group(2)?.safeToInt() ?: 0) + 12}"
                                    }
                                    closeTimeIsAmOrPm = if (group == "PM") "PM" else "AM"
                                    i = 0
                                }
                            }
                            openTime = "$onHour:00 $openTimeIsAmOrPm"
                            closeTime = "$offHour:00 $closeTimeIsAmOrPm"

                        } else {
                            logE("No match found.")
                            muteOn = "22"
                            muteOff = "7"

                            openTime = "10:00 PM"
                            closeTime = "7:00 AM"
                        }

                        binding.ftTimer.itemValue = "$openTime-$closeTime"
                    }
                }
            })

            // 放弃种子检查
            giveUpCheck.observe(this@SettingActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (null == data) return@success
                    if (data?.giveUp == true) {
                        // 弹窗
                        pop.asCustom(
                            BaseCenterPop(
                                this@SettingActivity,
                                onConfirmAction = {
                                    val intent =
                                        Intent(this@SettingActivity, WebActivity::class.java)
                                    intent.putExtra(WebActivity.KEY_WEB_URL, data?.url)
                                    startActivity(intent)
                                    // 点了之后就弹这个
                                    rePlantPop.show()
                                },
                                onCancelAction = {
                                    rePlantPop.show()
                                },
                                content = "Sorry for the bad experience you had, if you take a moment to complete the questionnaire, we can give you a month's subscription after approve.",
                                confirmText = getString(com.cl.common_base.R.string.string_10),
                                cancelText = "Replant",
                            )

                        ).show()
                    } else {
                        rePlantPop.show()
                    }
                }
            })

            // 监听设备
            deleteDevice.observe(this@SettingActivity, resourceObserver {
                success {
                    //  删除设备之后应该去哪？
                    // 跳转 Adddevice 界面
                    logI("deleteDevice is Success")
                    /*ARouter.getInstance().build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()*/

                    mViewModel.checkPlant()
                }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
            })

            // 删除植物
            plantDelete.observe(this@SettingActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                loading {
                    // showProgressLoading()
                }

                success {
                    mViewModel.checkPlant()
                }
            })
            // 检查植物
            checkPlant.observe(this@SettingActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                success {
                    // 是否种植过
                    data?.let { PlantCheckHelp().plantStatusCheck(this@SettingActivity, it, true) }
                }
            })

            // 检查App更新
            getAppVersion.observe(this@SettingActivity, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
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
                                if (isClickUpdate.value == true) {
                                    versionPop.setData(versionData)
                                    versionUpdatePop.show()
                                } else {
                                    binding.ftVision.setShowUpdateRedDot(true)
                                }
                            } else {
                                if (isClickUpdate.value == true) {
                                    ToastUtil.shortShow(getString(com.cl.common_base.R.string.my_appversion))
                                } else {
                                }
                            }
                        }
                    }
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
            })

            // 获取用户信息
            userDetail.observe(this@SettingActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    // 缓存信息
                    GSON.toJsonInBackground(data) { it1 -> Prefs.putString(Constants.Login.KEY_LOGIN_DATA, it1) }
                    // 是否开启通知(1-开启、0-关闭)
                    // binding.ftNotif.setItemSwitch(data?.openNotify == 1)
                    // 订阅时间
                    data?.subscriptionTime?.let {
                        binding.ftSub.itemValue = it
                    }
                }
                loading { }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
            })

            // 修改用户信息
            modifyUserDetail.observe(this@SettingActivity, resourceObserver {
                success {
                    hideProgressLoading()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
                loading {
                    hideProgressLoading()
                }
            })
        }
    }

    /**
     * 确认退出弹窗
     */
    private val logOutPop by lazy {
        XPopup.Builder(this@SettingActivity)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
            .asCustom(LoginOutPop(this) {
                InterComeHelp.INSTANCE.logout()
                ThingHomeSdk.onDestroy()
                ThingHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                    override fun onSuccess() {
                        // 清除缓存数据
                        Prefs.removeKey(Constants.Login.KEY_LOGIN_DATA_TOKEN)
                        // 推出firbase账号
                        Firebase.auth.signOut()
                        // 清除上面所有的Activity
                        // 跳转到Login页面
                        ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                            .withFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
                            .navigation()
                    }

                    override fun onError(code: String?, error: String?) {
                        logE(
                            """
                           logout -> onError:
                            code: $code
                            error: $error
                        """.trimIndent()
                        )
                        ToastUtil.shortShow(error)
                        Reporter.reportTuYaError("getUserInstance", error, code)
                    }
                })
            })
    }

    private var muteOn: String? = null
    private var muteOff: String? = null

    override fun initData() {
        // 重新种植
        binding.ftReplant.setOnClickListener {
            // 请求接口
            /*mViewModel.giveUpCheck()*/
            val isVip = mViewModel.userDetail.value?.data?.isVip == 1
            pop.isDestroyOnDismiss(false).asCustom(
                AttentionPop(
                    this@SettingActivity,
                    contentText = if (isVip) "You are about to replant. The current session will be lost, and this operation is irreversible. Our growing expert may help you save the plant" else "You are about to replant. The current session will be lost, and this operation is irreversible.",
                    isShowTalkButton = isVip,
                    talkButtonAction = {
                        // 跳转到客服
                        sendEmail()
                    },
                    rePlantAction = {
                        tuYaUser?.uid?.let { uid -> mViewModel.plantDelete(uid) }
                    }
                )
            ).show()
        }

        // 防烧模式
        binding.ftBurner.setSwitchCheckedChangeListener { _, isChecked ->
            ViewUtils.setVisible(isChecked, binding.tvBurnerDesc)
            // 开启防烧模式
            mViewModel.updateDeviceInfo(
                UpDeviceInfoReq(
                    deviceId = mViewModel.saveDeviceId.value,
                    burnOutProof = if (isChecked) 1 else 0
                )
            )
        }

        // usb模式
        binding.ftUsb.setSwitchCheckedChangeListener { _, isChecked ->
            mViewModel.setUsbUpdate(true)
            // usb模式
            mViewModel.updateDeviceInfo(
                UpDeviceInfoReq(
                    deviceId = mViewModel.saveDeviceId.value,
                    smartUsbPowder = if (isChecked) 1 else 0
                )
            )
        }

        // 童锁
        binding.ftChildLock.setSwitchCheckedChangeListener { _, isChecked ->
            logI("1231231: ${mViewModel.saveDeviceId.value}")
            // 是否打开童锁
            DeviceControl.get()
                .success {
                    mViewModel.updateDeviceInfo(
                        UpDeviceInfoReq(
                            childLock = if (isChecked) 1 else 0,
                            deviceId = mViewModel.saveDeviceId.value
                        )
                    )
                    binding.ftChildLock.isItemChecked = isChecked
                }
                .error { code, error ->
                    ToastUtil.shortShow(
                        """
                      childLock: 
                      code-> $code
                      errorMsg-> $error
                     """.trimIndent()
                    )
                }
                .childLock(isChecked)
        }

        // 手动模式还是自动模式
        binding.ftManualRoot.setOnClickListener {
            val isChecked = binding.itemTitle.text.contains("ON")
            if (!isChecked) {
                // 如果是关闭的
                val intent = Intent(this@SettingActivity, KnowMoreActivity::class.java)
                intent.putExtra(
                    Constants.Global.KEY_TXT_ID,
                    Constants.Fixed.KEY_FIXED_ID_MANUAL_MODE
                )
                intent.putExtra(
                    BasePopActivity.KEY_FIXED_TASK_ID,
                    Constants.Fixed.KEY_FIXED_ID_MANUAL_MODE
                )
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, mViewModel.userDetail.value?.data?.deviceId)
                intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, getString(com.cl.common_base.R.string.string_263))
                startActivity(intent)
            } else {
                // 删除植物、弹出提示框
                XPopup.Builder(this@SettingActivity).isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(true)
                    .asCustom(
                        MyDeleteDevicePop(
                            isShowUnlockButton = true,
                            unLockText = "Slide to Turn Off",
                            titleText = "Notice",
                            context = this,
                            contentText = "Turning off Pro Mode (Beta) will require you to start a new grow session. Please note your current progress will be lost; this action cannot be undone"
                        ) {
                            mViewModel.updateDeviceInfo(
                                UpDeviceInfoReq(
                                    deviceId = mViewModel.saveDeviceId.value,
                                    proMode = "Off"
                                )
                            )
                            tuYaUser?.uid?.let { uid -> mViewModel.plantDelete(uid) }
                        }).show()
            }

        }

        // 夜间模式
        binding.ftNight.setSwitchCheckedChangeListener { _, isChecked ->
            // muteOn:00,muteOff:001

            if (!isChecked) {
                DeviceControl.get()
                    .success {
                        // "141":"muteOn:10,muteOff:22"
                    }
                    .error { code, error ->
                        ToastUtil.shortShow(
                            """
                              nightMode: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                        )
                    }
                    .nightMode("lightOn:00,lightOff:00")
            } else {
                DeviceControl.get()
                    .success {
                        // "141":"muteOn:10,muteOff:22"
                    }
                    .error { code, error ->
                        ToastUtil.shortShow(
                            """
                              nightMode: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                        )
                    }
                    .nightMode("lightOn:${if (muteOn?.safeToInt() == 12) 24 else muteOn},lightOff:${if (muteOff?.safeToInt() == 24) 12 else muteOff}")
            }

            // 调用接口更新后台夜间模式
            mViewModel.updateDeviceInfo(
                UpDeviceInfoReq(
                    nightMode = if (isChecked) 1 else 0,
                    deviceId = mViewModel.saveDeviceId.value
                )
            )
            ViewUtils.setVisible(isChecked, binding.ftTimer)
        }

        binding.ftTimer.setOnClickListener {
            pop.asCustom(
                ChooseTimePop(
                    this@SettingActivity,
                    turnOnHour = muteOn?.safeToInt(),
                    turnOffHour = muteOff?.safeToInt(),
                    onConfirmAction = { onTime, offMinute, timeOn, timeOff, timeOpenHour, timeCloseHour ->
                        binding.ftTimer.itemValue = "$onTime-$offMinute"
                        muteOn = timeOn.toString().padStart(2, '0')
                        muteOff = timeOff.toString().padStart(2, '0')
                        // 这个时间和上面解析时间有问题，需要传递24小时制度
                        mViewModel.updateDeviceInfo(
                            UpDeviceInfoReq(
                                nightTimer = binding.ftTimer.itemValue.toString(),
                                deviceId = mViewModel.saveDeviceId.value
                            )
                        )
                        // 发送dp点
                        DeviceControl.get()
                            .success {
                                // "141":"muteOn:10,muteOff:22"
                                logI(
                                    "123312313: lightOn:${
                                        timeOn.toString().padStart(2, '0')
                                    },lightOff:${timeOff.toString().padStart(2, '0')}"
                                )
                            }
                            .error { code, error ->
                                ToastUtil.shortShow(
                                    """
                              nightMode: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                                )
                            }
                            .nightMode(
                                "lightOn:${
                                    if (timeOn == 12) 24 else timeOn.toString().padStart(2, '0')
                                },lightOff:${
                                    if (timeOff == 24) 12 else timeOff.toString().padStart(2, '0')
                                }"
                            )
                    })
            ).show()
        }

        // 重量单位
        binding.ftWeight.setOnClickListener {
            startActivity(Intent(this@SettingActivity, WeightActivity::class.java))
        }

        // 1v1
        /*binding.ftSolo.setOnClickListener {
            sendEmail()
        }*/
        // 删除设备
        binding.dtDeleteDevice.setOnClickListener {
            XPopup.Builder(this@SettingActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(MyDeleteDevicePop(this) {
                    removeDevice()
                }).show()
        }

        // 检查更新
        binding.ftVision.setOnClickListener {
            mViewModel.setClickUpdate(true)
            mViewModel.getAppVersion()
        }
        binding.ftPurchase.setOnClickListener {
            // 跳转到购买链接网页
            val intent = Intent(
                this@SettingActivity,
                WebActivity::class.java
            )
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/subscription")
            startActivity(
                intent
            )
        }

        binding.ftName.setOnClickListener {
            val deviceBean = mViewModel.devicesInfo.value
            //  修改属性、弹窗pop
            XPopup.Builder(this@SettingActivity).isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .autoOpenSoftInput(false)
                .autoFocusEditText(false)
                .asCustom(EditPlantProfilePop(this@SettingActivity,
                    beanData = deviceBean,
                    plantName = deviceBean?.plantName,
                    strainName = deviceBean?.strainName,
                    onConfirmAction = { plantName, strainName ->
                        // 需要刷新UI
                        mViewModel.listDevice()
                        // 修改属性名
                        if (strainName.isNullOrEmpty() && plantName?.isNotEmpty() == true) {
                            mViewModel.updatePlantInfo(
                                UpPlantInfoReq(
                                    plantName = plantName,
                                    plantId = deviceBean?.plantId,
                                )
                            )
                        }
                        if (plantName.isNullOrEmpty() && strainName?.isNotEmpty() == true) {
                            mViewModel.updatePlantInfo(
                                UpPlantInfoReq(
                                    strainName = strainName,
                                    plantId = deviceBean?.plantId,
                                )
                            )
                        } else {
                            mViewModel.updatePlantInfo(
                                UpPlantInfoReq(
                                    strainName = strainName,
                                    plantName = plantName,
                                    plantId = deviceBean?.plantId,
                                )
                            )
                        }
                        // 更新小组件
                        updateWidget(this@SettingActivity)
                    },
                    onDeviceChanged = {
                        mViewModel.listDevice()
                    }
                )).show()
        }

        // 关闭还是打开推送
        binding.ftNotif.setOnClickListener {
            // 是否开启通知(1-开启、0-关闭)
            // mViewModel.modifyUserDetail(ModifyUserDetailReq(openNotify = if (b) "1" else "0"))
            xpopup(this@SettingActivity) {
                dismissOnTouchOutside(false)
                isDestroyOnDismiss(false)
                asCustom(NotifyPop(this@SettingActivity, this@SettingActivity)).show()
            }
        }
        // 固件升级
        /*binding.ftFirUpdate.setOnClickListener {
            startActivity(Intent(this@SettingActivity, FirmwareUpdateActivity::class.java))
        }*/
        binding.ftCurrentFir.setOnClickListener {
            // 当前固件版本号
            mViewModel.checkFirmwareUpdateInfo { bean, isShow ->
                if (!isShow) {
                    ToastUtil.shortShow(getString(com.cl.common_base.R.string.my_appversion))
                    return@checkFirmwareUpdateInfo
                }
                bean?.firstOrNull { it.type == 9 }?.let { data ->
                    startActivity(Intent(this@SettingActivity, FirmwareUpdateActivity::class.java))
                }
            }
        }
        // 换水
        binding.ftWaterTank.setOnClickListener {
            plantDrain.show()
        }
        // 数字兑换Vip
        binding.ftChargeTime.setOnClickListener {
            startActivity(Intent(this@SettingActivity, ReDeemActivity::class.java))
        }
        // 去激活
        binding.ftSub.setOnClickListener {
            if (binding.ftSub.svtText.isShown) {
                // 跳转到订阅界面
                ARouter.getInstance().build(RouterPath.PairConnect.PAGE_SCAN_CODE).navigation()
            }
        }
        // 合并账号
        binding.ftMergeAccount.setOnClickListener {
            // 弹出合并账号弹窗
            pop.autoOpenSoftInput(false)
                .autoFocusEditText(false)
                .asCustom(
                    MergeAccountPop(this@SettingActivity, onConfirmAction = { email, code ->
                        // 跳转到弹窗合并确认界面
                        val intent =
                            Intent(this@SettingActivity, MergeAccountSureActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("code", code)
                        startActivity(intent)
                    })
                ).show()
        }

        // 修改密码
        binding.ftPassword.setOnClickListener {
            // 跳转到重置密码界面
            startActivity(Intent(this@SettingActivity, ResetPasswordActivity::class.java))
        }

        // 跳转到设备列表界面
        binding.ftManageDevice.setOnClickListener {
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                .navigation(this@SettingActivity)
        }

        // 退出账号
        binding.ftLogOut.setOnClickListener {
            // 退出账号
            logOutPop.show()
        }

        binding.ftCache.setOnClickListener {
            pop.asCustom(
                BaseCenterPop(this@SettingActivity,
                    content = "Clean up all the picture and video cache?",
                    onConfirmAction = {
                        GSYVideoManager.instance().clearAllDefaultCache(this@SettingActivity)
                        kotlin.runCatching {
                            binding.ftCache.itemValue =
                                CacheUtil.getVideoCache(this@SettingActivity)
                        }
                    })
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 缓存
        kotlin.runCatching {
            binding.ftCache.itemValue =
                CacheUtil.getVideoCache(this@SettingActivity)
        }
        mViewModel.userDetail()
        /*mViewModel.getAppVersion()*/

        /**
         * 当有设备的时候，判断当前设备是否在线
         *
         * 1- 绑定状态
         * 2- 解绑状态
         */
        if (mViewModel.deviceInfo?.deviceStatus == "1") {
            mViewModel.deviceInfo?.deviceOnlineStatus?.let {
                when (it) {
                    // 	设备在线状态(0-不在线，1-在线)
                    "0" -> {

                    }

                    "1" -> {
                        // 当前固件版本号
                        mViewModel.checkFirmwareUpdateInfo { bean, isShow ->
                            bean?.firstOrNull { it.type == 9 }?.let { data ->
                                binding.ftCurrentFir.itemValue = data.currentVersion
                                binding.ftCurrentFir.setShowUpdateRedDot(isShow)
                            }
                        }
                        // 获取SN & 并且判断是否是修复了SN的
                        mViewModel.getSn()
                        mViewModel.getActivationStatus()
                    }

                    else -> {}
                }
            }
        }

        // 重量单位
        val weightUnit = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        binding.ftWeight.itemValue =
            if (weightUnit) getString(com.cl.common_base.R.string.my_metric) else getString(com.cl.common_base.R.string.my_us)
    }

    override fun onPause() {
        super.onPause()
        val weightUnit = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        mViewModel.modifyUserDetail(ModifyUserDetailReq(inchMetricMode = if (!weightUnit) "inch" else "mefric"))
    }

    /**
     * 设备状态监听
     */
    override fun onDeviceChange(status: String) {
        super.onDeviceChange(status)
        when (status) {
            Constants.Device.KEY_DEVICE_OFFLINE -> {
                mViewModel.setOffLine(false)
            }

            Constants.Device.KEY_DEVICE_ONLINE -> {
                mViewModel.setOffLine(true)
            }

            Constants.Device.KEY_DEVICE_REMOVE -> {
                mViewModel.setOffLine(false)
            }
        }
    }

    override fun onTuYaToAppDataChange(status: String) {
        super.onTuYaToAppDataChange(status)
        GSON.parseObjectInBackground(status, Map::class.java) { map->
            map?.forEach { (key, value) ->
                when (key) {
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_REST_STATUS_INSTRUCTION -> {
                        if (value.toString().isEmpty()) return@forEach
                        logI(
                            """
                        KEY_DEVICE_REPAIR_REST_STATUS: 
                        value: ${value.toString()}
                    """.trimIndent()
                        )
                        // 修改:mcu:Abby-1.1.01-230313-T-B#abbyAAYA2234130142#1.8.1#flash:Abby-1.1.01-230313-T-B#1.8.1#OG#A0001#B0001#C0001#D0001
                        //mcu:Abby-1.1.01-220519-T-B#abbyAAYA2021730021#1.4.0#flash:Abby-1.1.01-220519-T-B#1.4.0
                        // 截取, 并且需要置灰
                        kotlin.runCatching {
                            binding.ftSN.setItemValueWithColor(
                                value.toString().split("#")[1], "#000000"
                            )
                        }
                    }

                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_SN_INSTRUCTION -> {
                        if (value.toString().isEmpty()) return@forEach
                        if (value.toString() == "NG") {
                            binding.ftSub.setSvText("Activate")
                        } else {
                            mViewModel.userDetail.value?.data?.subscriptionTime?.let {
                                binding.ftSub.itemValue = it
                            }
                        }
                    }

                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_CHILD_LOCK_INSTRUCT -> {
                        // 童锁
                        kotlin.runCatching {
                            binding.ftChildLock.isItemChecked = value.toString().toBoolean()
                        }.onFailure {
                            binding.ftChildLock.isItemChecked = false
                        }
                    }
                }
            }
        }

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
        InterComeHelp.INSTANCE.openInterComeHome()
    }

    private fun startGooglePlay() {
        val playPackage = AppUtil.appPackage
        try {
            val currentPackageUri: Uri = Uri.parse("market://details?id=" + AppUtil.appPackage)
            val intent = Intent(Intent.ACTION_VIEW, currentPackageUri)
            intent.setPackage(playPackage)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val currentPackageUri: Uri =
                Uri.parse(VersionUpdatePop.KEY_GOOGLE_PLAY_URL + AppUtil.appPackage)
            val intent = Intent(Intent.ACTION_VIEW, currentPackageUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


    private val myActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                // 排水结束，那么直接弹出
                if (!plantDrainFinished.isShow) plantDrainFinished.show()
            }
        }

    private fun removeDevice() {
        val deviceId = mViewModel.saveDeviceId.value.toString()
        val deviceInstance = ThingHomeSdk.newDeviceInstance(deviceId)

        deviceInstance.removeDevice(object : IResultCallback {
            override fun onError(code: String?, error: String?) {
                logError(code, error)
                handleDeviceRemoval(deviceId)
            }

            override fun onSuccess() {
                handleDeviceRemoval(deviceId)
            }
        })
    }

    private fun handleDeviceRemoval(deviceId: String) {
        val cameraAccessory = getCameraAccessory()
        if (cameraAccessory != null) {
            TuyaCameraUtils().unBindCamera(cameraAccessory.accessoryDeviceId.toString(), onErrorAction = {
                ToastUtil.shortShow(it)
            }) {
                mViewModel.deleteDevice(deviceId)
            }
        } else {
            mViewModel.deleteDevice(deviceId)
        }
    }

    // 摄像头需要解绑。因为后台解绑不了。
    private fun getCameraAccessory(): AccessoryList? =
        mViewModel.listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }?.accessoryList?.firstOrNull { it.accessoryType == AccessoryListBean.KEY_CAMERA }

    private fun logError(code: String?, error: String?) {
        val logMessage = """
        removeDevice:
        code: $code
        error: $error
    """.trimIndent()
        logE(logMessage)
        ToastUtil.shortShow(error)
        Reporter.reportTuYaError("newDeviceInstance", error, code)
    }

}
