package com.cl.modules_my.ui

import android.content.*
import android.content.Intent.*
import android.net.Uri
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.pop.*
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MySettingBinding
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.viewmodel.SettingViewModel
import com.cl.modules_my.widget.MyDeleteDevicePop
import com.cl.modules_my.widget.MyRePlantPop
import com.cl.modules_my.widget.SubPop
import com.lxj.xpopup.XPopup
import com.tuya.smart.android.user.bean.User
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 设置界面
 *
 * @author 李志军 2022-08-06 11:33
 */
@AndroidEntryPoint
class SettingActivity : BaseActivity<MySettingBinding>() {
    @Inject
    lateinit var mViewModel: SettingViewModel

    private val tuyaHomeBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    // 登录信息
    private val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    private val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }


    /**
     *  replant 弹窗
     */
    private val rePlantPop by lazy {
        XPopup.Builder(this@SettingActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(true)
            .asCustom(
                MyRePlantPop(
                    context = this@SettingActivity,
                    onNextAction = {
                        tuYaUser?.uid?.let { uid -> mViewModel.plantDelete(uid) }
                    }
                )
            )
    }

    /**
     * APP检测升级弹窗
     */
    private val versionUpdatePop by lazy {
        XPopup.Builder(this@SettingActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(versionPop)
    }
    private val versionPop by lazy {
        VersionUpdatePop(this@SettingActivity)
    }

    /**
     * 添加排水弹窗
     */
    private val plantDrain by lazy {
        XPopup.Builder(this@SettingActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false)
            .asCustom(
                HomePlantDrainPop(
                    context = this@SettingActivity,
                    onNextAction = {
                        // 请求接口
                        mViewModel.advertising()
                    }
                )
            )
    }

    // 统一xPopUp
    private val pop by lazy {
        // 升级提示框
        XPopup.Builder(this@SettingActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
    }

    /**
     * 排水完成弹窗
     */
    private val plantDrainFinished by lazy {
        XPopup.Builder(this@SettingActivity)
            .isDestroyOnDismiss(true)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(false)
            .asCustom(
                BasePumpWaterFinishedPop(this@SettingActivity)
            )
    }

    /**
     * 换水弹窗-附带其他东西
     */
    private val plantDrainNextCustomPop by lazy {
        BasePumpWaterPop(
            this@SettingActivity,
            { status ->
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
                plantDrainFinished.show()
            }
        )
    }

    /**
     * 删除设备弹窗
     */
    private val confirm by lazy {
        XPopup.Builder(this@SettingActivity)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(true)
            .asCustom(MyDeleteDevicePop(this) {
                TuyaHomeSdk.newDeviceInstance(tuyaHomeBean?.devId)
                    .removeDevice(object : IResultCallback {
                        override fun onError(code: String?, error: String?) {
                            logE(
                                """
                        removeDevice:
                        code: $code
                        error: $error
                    """.trimIndent()
                            )
                            mViewModel.deleteDevice()
                        }

                        override fun onSuccess() {
                            //  调用接口请求删除设备
                            mViewModel.deleteDevice()
                        }
                    })
            })
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
        binding.ftSub.setTitleValueEndDrawable(null)
            .setPointClickListener {
                pop.asCustom(SubPop(this@SettingActivity)).show()
            }
    }

    override fun observe() {
        mViewModel.apply {
            deleteDevice.observe(this@SettingActivity, resourceObserver {
                success {
                    //  删除设备之后应该去哪？
                    // 跳转 Adddevice 界面
                    logI("deleteDevice is Success")
                    ARouter.getInstance()
                        .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
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
                    showProgressLoading()
                }

                success {
                    tuYaUser?.uid?.let { mViewModel.checkPlant(it) }
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
                    data?.let { PlantCheckHelp().plantStatusCheck(it, true) }
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
                            if (netWorkVersion.toInt() > localVersion.toInt()) {
                                versionPop.setData(versionData)
                                versionUpdatePop.show()
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
                    // 是否开启通知(1-开启、0-关闭)
                    binding.ftNotif.setItemSwitch(data?.openNotify == 1)
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

            // 换水获取图文
            advertising.observe(this@SettingActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                loading { showProgressLoading() }

                success {
                    hideProgressLoading()
                    data?.let { plantDrainNextCustomPop.setData(it) }
                    pop.maxHeight(dp2px(600f))
                        .asCustom(plantDrainNextCustomPop).show()
                }
            })
        }
    }

    override fun initData() {
        // 重新种植
        binding.ftReplant.setOnClickListener {
            rePlantPop.show()
        }
        // 1v1
        binding.ftSolo.setOnClickListener {
            sendEmail()
        }
        // 删除设备
        binding.dtDeleteDevice.setOnClickListener {
            // 删除设备、弹出提示框
            confirm.show()
        }
        // 检查更新
        binding.ftNewVision.setOnClickListener {
            // 直接跳转GooglePlay
            startGooglePlay()
        }
        // 关闭还是打开推送
        binding.ftNotif.setSwitchCheckedChangeListener { compoundButton, b ->
            // 是否开启通知(1-开启、0-关闭)
            mViewModel.modifyUserDetail(ModifyUserDetailReq(openNotify = if (b) "1" else "0"))
        }
        // 固件升级
        binding.ftFirUpdate.setOnClickListener {
            startActivity(Intent(this@SettingActivity, FirmwareUpdateActivity::class.java))
        }
        // 换水
        binding.ftWaterTank.setOnClickListener {
            plantDrain.show()
        }
        // 去激活
        binding.ftSub.setOnClickListener {
            if (binding.ftSub.svtText.isShown) {
                // 跳转到订阅界面
                ARouter.getInstance()
                    .build(RouterPath.PairConnect.PAGE_SCAN_CODE)
                    .navigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.userDetail()
        // 当前固件版本号
        mViewModel.checkFirmwareUpdateInfo { bean, isShow ->
            bean?.firstOrNull { it.type == 9 }?.let { data ->
                binding.ftCurrentFir.itemValue = data.currentVersion
                binding.ftCurrentFir.setHideArrow(true)
                binding.ftFirUpdate.setShowRedDot(isShow)
            }
        }
        // 获取SN & 并且判断是否是修复了SN的
        mViewModel.getSn()
        mViewModel.getActivationStatus()
    }


    /**
     * 设备状态监听
     */
    override fun onDeviceChange(status: String) {
        super.onDeviceChange(status)
        when (status) {
            Constants.Device.KEY_DEVICE_OFFLINE -> {
                mViewModel.setOffLine(true)
            }
            Constants.Device.KEY_DEVICE_ONLINE -> {
                mViewModel.setOffLine(false)
            }
            Constants.Device.KEY_DEVICE_REMOVE -> {
                mViewModel.setOffLine(true)
            }
        }
    }

    override fun onTuYaToAppDataChange(status: String) {
        super.onTuYaToAppDataChange(status)
        val map = GSON.parseObject(status, Map::class.java)
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
                    //mcu:Abby-1.1.01-220519-T-B#abbyAAYA2021730021#1.4.0#flash:Abby-1.1.01-220519-T-B#1.4.0
                    // 截取, 并且需要置灰
                    kotlin.runCatching {
                        binding.ftSN.setItemValueWithColor(
                            value.toString().split("#")[1],
                            "#000000"
                        )
                    }
                }

                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_SN_INSTRUCTION -> {
                    if (value.toString().isEmpty()) return@forEach
                    if (value.toString() == "NG") {
                        binding.ftSub.setSvText("Activate")
                    } else {
                        binding.ftSub.itemValue = userinfoBean?.subscriptionTime
                    }
                }
            }
        }
    }

    /**
     * 发送支持邮件
     */
    private fun sendEmail() {
        val uriText = "mailto:growsupport@heyabby.com" + "?subject=" + Uri.encode("Support")
        val uri = Uri.parse(uriText)
        val sendIntent = Intent(ACTION_SENDTO)
        sendIntent.data = uri
        val pm = this.packageManager
        // 根据意图查找包
        val activityList = pm.queryIntentActivities(sendIntent, 0)
        if (activityList.size == 0) {
            // 弹出框框
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
            val clipData = ClipData.newPlainText(null, "growsupport@heyabby.com")
            // 把数据集设置（复制）到剪贴板
            clipboard.setPrimaryClip(clipData)
            XPopup.Builder(this@SettingActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(SendEmailTipsPop(this@SettingActivity) { null }).show()
            return
        }
        try {
            startActivity(createChooser(sendIntent, "Send email"))
        } catch (ex: ActivityNotFoundException) {
            XPopup.Builder(this@SettingActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(SendEmailTipsPop(this@SettingActivity) { null }).show()
        }
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

}

