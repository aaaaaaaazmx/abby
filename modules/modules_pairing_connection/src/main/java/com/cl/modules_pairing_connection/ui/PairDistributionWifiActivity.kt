package com.cl.modules_pairing_connection.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.InputType
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.widget.doAfterTextChanged
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.pop.GuideBlePop
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.network.NetWorkUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairConnectNetworkBinding
import com.cl.modules_pairing_connection.request.PairBleData
import com.cl.modules_pairing_connection.viewmodel.PairDistributionWifiViewModel
import com.google.zxing.WriterException
import com.lxj.xpopup.XPopup
import com.thingclips.smart.android.ble.api.ConfigErrorBean
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.builder.ThingCameraActivatorBuilder
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IMultiModeActivatorListener
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartCameraActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.bean.MultiModeActivatorBean
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.random.Random


/**
 * 配网界面
 *
 * @author 李志军 2022-08-03 22:26
 */
@AndroidEntryPoint
@Route(path = RouterPath.PairConnect.PAGE_WIFI_CONNECT)
class PairDistributionWifiActivity : BaseActivity<PairConnectNetworkBinding>() {

    // 传过来设备数据
    private val bleData by lazy {
        intent.getSerializableExtra(PairOnePageActivity.KEY_DEVICE_DATA) as? PairBleData
    }

    private val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    // 区分是链接camera还是链接abby的flag
    private val isCameraConnect by lazy {
        intent.getBooleanExtra(Constants.Global.KEY_WIFI_PAIRING_PARAMS, false)
    }

    @Inject
    lateinit var mViewModel: PairDistributionWifiViewModel

    @SuppressLint("SetTextI18n")
    override fun initView() {
        ARouter.getInstance().inject(this)
        /**
         * 摄像头界面需要改变这些文案
         */
        if (isCameraConnect){
            //1.abby only supports 2.4GHz Wi-Fi.
            //Wi-Fi only supports alphanumeric character
            //
            //2.Your phone must be connected to the same 2.4G wifi as abby
            ViewUtils.setVisible(false, binding.tvBleNane)
            binding.titleBar.setTitle("")
            binding.btnSuccess.text = "Next"
            binding.tvDescThree.text = buildSpannedString {
                append("1. smart camera only supports ")
                bold { append("2.4GHz Wi-Fi.") }
                appendLine("\nWi-Fi only supports alphanumeric character")
                appendLine("\n")
                appendLine("2. Your phone must be connected to the same 2.4G wifi as smart camera")
            }
        } else {
            // 设置设备名字
            binding.tvBleNane.text = "Device: ${bleData?.subName}"

            // 设置富文本
            // Hey abby only supports 2.4G Wi-Fi networks.
            // Network names must be comprised of alpahnumeric characters only.
            binding.tvDescThree.text = buildSpannedString {
                append("1. Hey abby only supports ")
                bold { append("2.4GHz Wi-Fi networks.") }
                appendLine("\n")
                appendLine("2. Network names must be comprised of alpahnumeric characters only")
                appendLine("\n")
                appendLine("3. Your phone must be connected to the same 2.4G Wi-Fi as abby.")
            }
        }
    }

    private val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    /**
     * 引导开启蓝牙弹窗
     */
    private val guideBlePop by lazy {
        XPopup.Builder(this)
            .isDestroyOnDismiss(false)
            .asCustom(GuideBlePop(this))
    }

    override fun observe() {
        mViewModel.apply {
            // 同步设备信息
            syncDeviceInfo.observe(this@PairDistributionWifiActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.show(errorMsg)
                }
            })
            bindDevice.observe(this@PairDistributionWifiActivity, resourceObserver {
                success {
                    // 开启服务
                    val intent =
                        Intent(this@PairDistributionWifiActivity, TuYaDeviceUpdateReceiver::class.java)
                    startService(intent)
                    mViewModel.userDetail()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                }
            })

            /**
             * 获取个人信息
             */
            userDetail.observe(this@PairDistributionWifiActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    // 缓存信息
                    GSON.toJson(this)?.let { it1 -> Prefs.putStringAsync(Constants.Login.KEY_USER_INFO, it1) }
                    // 绑定设备JPUSH的别名
                    thread {
                        logI(
                            """
                            setAliasAndTags:
                            abbyId: ${data?.abbyId}
                        """.trimIndent()
                        )
                        // 调用 JPush 接口来设置别名。
                        data?.abbyId?.let {
                            JPushInterface.setAlias(
                                this@PairDistributionWifiActivity,
                                Random.nextInt(100),
                                it
                            )
                        }
                    }
                    // 种植检查
                    tuYaUser?.uid?.let { mViewModel.checkPlant(it) }
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                }
            })


            /**
             * 种植检查
             */
            checkPlant.observe(this@PairDistributionWifiActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    logI(
                        """
                        data: ${data.toString()}
                    """.trimIndent()
                    )
                    data?.let { PlantCheckHelp().plantStatusCheck(this@PairDistributionWifiActivity, it, true) }
                }

                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                }
                loading {
                    showProgressLoading()
                }
            })

            /**
             * 明文
             */
            passWordState.observe(this@PairDistributionWifiActivity) {
                if (it == false) {
                    // 明文
                    binding.etWifiPwd.inputType =
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    // 密码
                    binding.etWifiPwd.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
        }
    }

    override fun initData() {
        binding.etWifiPwd.doAfterTextChanged {
            val psd = it.toString()
            binding.error.visibility = View.INVISIBLE
            binding.btnSuccess.isEnabled =
                (!psd.isNullOrEmpty() && !binding.tvWifiName.text.isNullOrEmpty())
        }

        /**
         * 密码是否是可见到
         */
        binding.flPsdState.setOnClickListener {
            if (mViewModel.passWordState.value == true) {
                // 明文
                binding.etWifiPwd.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.cbCheck.background =
                    ContextCompat.getDrawable(
                        this@PairDistributionWifiActivity,
                        R.mipmap.pair_psd_open
                    )
            } else {
                // 密码
                binding.etWifiPwd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.cbCheck.background = ContextCompat.getDrawable(
                    this@PairDistributionWifiActivity,
                    R.mipmap.pair_psd_close
                )
            }
            binding.etWifiPwd.setSelection(binding.etWifiPwd.text.length)
            mViewModel.setPassWordState(!(mViewModel.passWordState.value ?: true))
        }

        /**
         * 开始配网
         */
        binding.btnSuccess.setOnClickListener {
            PermissionHelp().checkConnect(
                this@PairDistributionWifiActivity,
                supportFragmentManager,
                true,
                object : PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        if (!result) return
                        // 权限都同意之后，那么直接开始配网
                        if (isCameraConnect) startNetWorkForCamera() else startNetWorkForAbby()
                    }
                })
        }

        // 跳转wifi设置界面
        binding.tvWifiName.setOnClickListener {
            kotlin.runCatching {
                val wifiSettingsIntent = Intent("android.settings.WIFI_SETTINGS")
                startActivity(wifiSettingsIntent)
            }.onFailure {
                ToastUtil.shortShow("Please switch wifi manually")
            }

            /*val i = Intent()
            if (Build.VERSION.SDK_INT >= 11) {
                //Honeycomb
                i.setClassName(
                    "com.android.settings",
                    "com.android.settings.Settings\$·"
                )
            } else {
                //other versions
                i.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings")
            }
            startActivity(i)*/
        }
    }

    /**
     * 获取wifi名字
     */
    private fun getWifiName() {
        if (NetWorkUtil.isWifi(this@PairDistributionWifiActivity)) {
            PermissionHelp().applyPermissionHelp(
                this@PairDistributionWifiActivity,
                "Granting Hey abby access to your phone's location will be used to generate a Wi-Fi network list.",
                object : PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        if (!result) return
                        // 直接获取wifi名字
                        val wifiName =
                            NetWorkUtil.getConnectWifiSsid(this@PairDistributionWifiActivity)
                        binding.tvWifiName.text = wifiName
                        // 默认设置账号密码
                        if (wifiName == mViewModel.wifiName) {
                            if (mViewModel.wifiPsd.isNotEmpty()) {
                                binding.etWifiPwd.setText(mViewModel.wifiPsd)
                            }
                        }
                    }
                },
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // 是否是wifi
        ViewUtils.setVisible(
            !NetWorkUtil.isWifi(this@PairDistributionWifiActivity),
            binding.svtNoWifi
        )
        getWifiName()
    }

    private val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    /**
     * 开始配网，链接camera
     */
    private fun startNetWorkForCamera() {
        val wifiName = binding.tvWifiName.text.toString()
        val psd = binding.etWifiPwd.text.toString()
        showProgressLoading()
        // 首先获取配网token
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId, object : IThingActivatorGetToken{
            override fun onSuccess(token: String?) {
                val builder = ThingCameraActivatorBuilder()
                    .setToken(token)
                    .setPassword(psd)
                    .setTimeOut(100)
                    .setContext(this@PairDistributionWifiActivity)
                    .setSsid(wifiName)
                    .setListener(object : IThingSmartCameraActivatorListener {
                        override fun onQRCodeSuccess(qrcodeUrl: String) {
                            hideProgressLoading()
                            try {
                                // 需要返回这个url回去。
                                /*this@PairDistributionWifiActivity.setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra("qrcodeUrl", qrcodeUrl)
                                    putExtra("wifiName", wifiName)
                                    putExtra("wifiPsd", psd)
                                    putExtra("token", token)
                                })
                                finish()*/

                                // 说明绑定成功，跳转到二维码生成界面
                                ARouter.getInstance().build(RouterPath.My.PAGE_CAMERA_QR_CODE)
                                    .withString("qrcodeUrl", qrcodeUrl)
                                    .withString("wifiName", wifiName)
                                    .withString("wifiPsd", psd)
                                    .withString("token", token)
                                    .withString("accessoryId", intent.getStringExtra("accessoryId"))
                                    .withString("deviceId", intent.getStringExtra("deviceId"))
                                    .navigation(this@PairDistributionWifiActivity)
                            } catch (e: WriterException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(errorCode: String, errorMsg: String) {
                            hideProgressLoading()
                            ToastUtil.shortShow("errorCode: $errorCode errorMsg: $errorMsg")
                        }
                        override fun onActiveSuccess(devResp: DeviceBean?) {
                            // Toast.makeText(this@WifiToQrCodeActivity, "config success!", Toast.LENGTH_LONG).show()
                            // todo 绑定成功、 跳转到视频界面
                            hideProgressLoading()
                        }
                    })

                // 开始配对
                ThingHomeSdk.getActivatorInstance().newCameraDevActivator(builder)?.apply {
                    createQRCode()
                    start()
                }
            }

            override fun onFailure(errorCode: String?, errorMsg: String?) {
                hideProgressLoading()
                ToastUtil.shortShow("errorCode: $errorCode errorMsg: $errorMsg")
            }
        })
    }

    /**
     * 开始配网
     *
     * 这部分可以抽到ViewModel当中，但是我不想抽！
     */
    private fun startNetWorkForAbby() {
        val wifiName = binding.tvWifiName.text.toString()
        val psd = binding.etWifiPwd.text.toString()

        // 首先获取配网token
        showProgressLoading(cancelable = false)
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId,
            object : IThingActivatorGetToken {
                override fun onSuccess(token: String) {
                    // Start configuration -- Dual Ble Device
                    logI("getActivatorToken: $token")
                    /**
                     * deviceType	Integer	设备类型，通过扫描可以查询
                    uuid	String	设备 UUID，通过扫描可以查询
                    address	String	设备地址，通过扫描可以查询
                    mac	String	设备 Mac，通过扫描可以查询
                    ssid	String	Wi-Fi SSID
                    pwd	String	Wi-Fi 密码
                    token	String	配网 Token，获取 Token 的方式与 Wi-Fi 设备配网一致，请参考 获取 Token
                    homeId	long	当前家庭的 ID
                    timeout	long	配网总超时，配网超时失败以该参数为准，单位为毫秒
                     */
                    val netWorkBean = MultiModeActivatorBean()
                    netWorkBean.deviceType = bleData?.bleData?.deviceType ?: -1
                    netWorkBean.uuid = bleData?.bleData?.uuid
                    netWorkBean.address = bleData?.bleData?.address
                    netWorkBean.mac = bleData?.bleData?.mac
                    netWorkBean.ssid = wifiName
                    netWorkBean.pwd = psd
                    netWorkBean.token = token
                    netWorkBean.homeId = homeId
                    netWorkBean.timeout = 120000
                    logI("MultiModeActivatorBean: $netWorkBean")
                    // 开始配网
                    ThingHomeSdk.getActivator().newMultiModeActivator()
                        .startActivator(netWorkBean, object : IMultiModeActivatorListener {
                            override fun onSuccess(deviceBean: DeviceBean?) {
                                logI("startActivator DeviceBean : ${deviceBean.toString()}")
                                // 从涂鸦的设备列表里面拿第一个设备
                                ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(object :
                                    IThingHomeResultCallback {
                                    override fun onSuccess(bean: HomeBean?) {
                                        // 取数据
                                        bean?.let { homeBean ->
                                            kotlin.runCatching {
                                                    // todo 需要看下是否调用了bindDevice方法。有可能是没调用
                                                    // logI("DeviceListSize: ${homeBean.deviceList?.size}")
                                                    // homeBean.deviceList.forEach { logI("devId: ${it.devId}, $it") }
                                                    // 重新绑定时、只取最后一个，表示这是新添加的。
                                                    // 缓存用户第一个设备数据
                                                    // 只取第一个
                                                    GSON.toJson(deviceBean)?.let {
                                                        Prefs.putStringAsync(
                                                            Constants.Tuya.KEY_DEVICE_DATA,
                                                            it
                                                        )
                                                    }
                                                    // 开始存储账号和密码
                                                    Prefs.putStringAsync(
                                                        Constants.Pair.KEY_PAIR_WIFI_NAME,
                                                        binding.tvWifiName.text.toString()
                                                    )
                                                    Prefs.putStringAsync(
                                                        Constants.Pair.KEY_PAIR_WIFI_PASSWORD,
                                                        binding.etWifiPwd.text.toString()
                                                    )
                                                    // 先进行数据同步、后绑定
                                                    mViewModel.getDps(deviceBean)

                                            }.onFailure { hideProgressLoading() }
                                        }
                                    }

                                    override fun onError(errorCode: String, errorMsg: String?) {
                                        hideProgressLoading()
                                        ToastUtil.shortShow(errorMsg)
                                        Reporter.reportTuYaError("newHomeInstance", errorMsg, errorCode)
                                    }
                                })

                            }

                            override fun onFailure(code: Int, msg: String?, handle: Any?) {
                                logE(
                                    """
                                startActivator error:
                                code: $code
                                msg: $msg
                                handle: ${(handle as? ConfigErrorBean).toString()}
                            """.trimIndent()
                                )
                                ToastUtil.shortShow(msg)
                                Reporter.reportTuYaError("getActivator", msg, code.toString())
                                // 3 密码错误 4 路由器连接失败（大概率是密码错误）
                                runOnUiThread {
                                    hideProgressLoading()
                                    when (code) {
                                        3 -> {  // wifi 密码错误
                                            binding.error.visibility = View.VISIBLE}
                                        4 -> {  // wifi 密码错误
                                            binding.error.visibility = View.VISIBLE}
                                        207006 -> {
                                            // msg = Doing 不用处理
                                        }
                                        else -> {
                                            // 配网失败跳转失败界面
                                            startActivity(
                                                Intent(
                                                    this@PairDistributionWifiActivity,
                                                    PairFailActivity::class.java
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        })
                }

                override fun onFailure(errorCode: String?, errorMsg: String?) {
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                    logE("getActivatorToken: errorCode->${errorCode}, Error->$errorMsg")
                    Reporter.reportTuYaError("getActivatorInstance", errorMsg, errorCode)
                }
            })
    }
}