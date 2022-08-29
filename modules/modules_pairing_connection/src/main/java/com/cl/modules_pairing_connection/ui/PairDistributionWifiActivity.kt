package com.cl.modules_pairing_connection.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.text.InputType
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.widget.doAfterTextChanged
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.pop.GuideBlePop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.ble.BleUtil
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.network.NetWorkUtil
import com.cl.common_base.util.permission.PermissionChecker
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairConnectNetworkBinding
import com.cl.modules_pairing_connection.request.PairBleData
import com.cl.modules_pairing_connection.viewmodel.PairDistributionWifiViewModel
import com.lxj.xpopup.XPopup
import com.permissionx.guolindev.PermissionX
import com.tuya.smart.android.ble.api.ConfigErrorBean
import com.tuya.smart.android.user.bean.User
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback
import com.tuya.smart.sdk.api.IMultiModeActivatorListener
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.smart.sdk.bean.MultiModeActivatorBean
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
class PairDistributionWifiActivity : BaseActivity<PairConnectNetworkBinding>() {

    // 传过来设备数据
    private val bleData by lazy {
        intent.getSerializableExtra(PairOnePageActivity.KEY_DEVICE_DATA) as? PairBleData
    }

    private val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    @Inject
    lateinit var mViewModel: PairDistributionWifiViewModel

    override fun initView() {
        ARouter.getInstance().inject(this)
        // 设置设备名字
        binding.tvBleNane.text = "Device: ${bleData?.subName}"

        // 设置富文本
        binding.tvDescThree.text = buildSpannedString {
            append("1.abby only supports ")
            bold { append("2.4GHz Wi-Fi.") }
            appendLine("Wi-Fi only supports alphanumeric character")
            appendLine("2.Your phone must be connected to the same 2.4G wifi as abby")
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
            bindDevice.observe(this@PairDistributionWifiActivity, resourceObserver {
                success {
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
                    data?.let { PlantCheckHelp().plantStatusCheck(it, true) }
                    finish()
                }

                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                }
                loading {
                    showProgressLoading()
                }
            })
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
                        startNetWork()
                    }
                })
        }

        // 跳转wifi设置界面
        binding.tvWifiName.setOnClickListener {
            val i = Intent()
            if (Build.VERSION.SDK_INT >= 11) {
                //Honeycomb
                i.setClassName(
                    "com.android.settings",
                    "com.android.settings.Settings\$WifiSettingsActivity"
                )
            } else {
                //other versions
                i.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings")
            }
            startActivity(i)
        }
    }

    /**
     * 获取wifi名字
     */
    private fun getWifiName() {
        if (NetWorkUtil.isWifi(this@PairDistributionWifiActivity)) {
            PermissionHelp().applyPermissionHelp(
                this@PairDistributionWifiActivity,
                "Enable the location permission to get the Wi-Fi name automatically.",
                object : PermissionHelp.OnCheckResultListener{
                    override fun onResult(result: Boolean) {
                        if (!result) return
                        // 直接获取wifi名字
                        val wifiName = NetWorkUtil.getConnectWifiSsid(this@PairDistributionWifiActivity)
                        binding.tvWifiName.text = wifiName
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

    /**
     * 开始配网
     *
     * 这部分可以抽到ViewModel当中，但是我不想抽！
     */
    private fun startNetWork() {
        val wifiName = binding.tvWifiName.text.toString()
        val psd = binding.etWifiPwd.text.toString()

        // 首先获取配网token
        showProgressLoading()
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId,
            object : ITuyaActivatorGetToken {
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
                    TuyaHomeSdk.getActivator().newMultiModeActivator()
                        .startActivator(netWorkBean, object : IMultiModeActivatorListener {
                            override fun onSuccess(deviceBean: DeviceBean?) {
                                logI("startActivator DeviceBean : ${deviceBean.toString()}")
                                // 从涂鸦的设备列表里面拿第一个设备
                                TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(object :
                                    ITuyaHomeResultCallback {
                                    override fun onSuccess(bean: HomeBean?) {
                                        // 取数据
                                        bean?.let { homeBean ->
                                            kotlin.runCatching {
                                                // 目前只允许绑定一个，那么只取第一个
                                                val bean = homeBean.deviceList[0]
                                                // 缓存用户第一个设备数据
                                                // 只取第一个
                                                GSON.toJson(bean)?.let {
                                                    Prefs.putStringAsync(
                                                        Constants.Tuya.KEY_DEVICE_DATA,
                                                        it
                                                    )
                                                }
                                                // 调用后台接口绑定
                                                mViewModel.bindDevice(bean.devId, bean.uuid)
                                            }.onFailure { hideProgressLoading() }
                                        }
                                    }

                                    override fun onError(errorCode: String?, errorMsg: String?) {
                                        hideProgressLoading()
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

                                // 3 密码错误 4 路由器连接失败（大概率是密码错误）
                                runOnUiThread {
                                    hideProgressLoading()
                                    if (code == 3 || code == 4) {
                                        // wifi 密码错误
                                        binding.error.visibility = View.VISIBLE
                                    } else {
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
                        })
                }

                override fun onFailure(errorCode: String?, errorMsg: String?) {
                    hideProgressLoading()
                    logE("getActivatorToken: errorCode->${errorCode}, Error->$errorMsg")
                }
            })
    }
}