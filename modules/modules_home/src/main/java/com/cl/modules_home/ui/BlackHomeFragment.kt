package com.cl.modules_home.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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
import com.bbgo.module_home.databinding.HomeBlackProModeFragmentBinding
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.R
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.AllDpBean
import com.cl.common_base.bean.FinishPageData
import com.cl.common_base.bean.JpushMessageData
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.LiveDataDeviceInfoBean
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.PresetData
import com.cl.common_base.bean.ProModeInfoBean
import com.cl.common_base.bean.UnreadMessageData
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToDouble
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.temperatureConversion
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.ChooseTimePop
import com.cl.common_base.pop.FirmwareUpdatePop
import com.cl.common_base.pop.MedalPop
import com.cl.common_base.pop.PresetLoadPop
import com.cl.common_base.pop.PresetPop
import com.cl.common_base.pop.VersionUpdatePop
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.web.VideoPLayActivity
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.adapter.HomeFinishItemAdapter
import com.cl.modules_home.viewmodel.BlackHomeViewModel
import com.cl.modules_home.widget.HomeDripPop
import com.cl.modules_home.widget.HomeFanBottonPop
import com.lxj.xpopup.XPopup
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * 黑色机箱的proMode界面
 */

@AndroidEntryPoint
@Route(path = RouterPath.Home.PAGE_BLACK_HOME)
class BlackHomeFragment:BaseFragment<HomeBlackProModeFragmentBinding>() {

    @Inject
    lateinit var mViewMode: BlackHomeViewModel

    // 是否是手动模式
    private val isManual by lazy {
        arguments?.getBoolean(Constants.Global.KEY_MANUAL_MODE, false)
    }

    override fun HomeBlackProModeFragmentBinding.initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = mViewMode
        binding.executePendingBindings()
    }

    override fun initView(view: View) {
        mViewMode.userDetail()
        mViewMode.plantInfo()
        // 版本更新
        mViewMode.getAppVersion()
        // eventbus的消息接收
        liveDataObser()
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.clTop) { v, insets ->
            binding.clTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
        queryAllDp()
    }

    override fun onPause() {
        super.onPause()
        mViewMode.setShouldRunJob(false)
        job?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 销毁job倒计时任务
        job?.cancel()
        // mCameraP2P?.destroyP2P()
    }

    /**
     * 查询所有的dp点
     */
    private fun queryAllDp() {
        GSON.toJson(AllDpBean(cmd = "2"))?.let { DeviceControl.get().success { }.error { code, error -> }.sendDps(it, mViewMode.thingDeviceBean()?.devId) }
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
            mViewMode.apply {
                binding.tvInc.text = incCovert()
                binding.tvTemperature.text = textCovert()
                binding.tvWaterTemperature.text = textCovert()
                binding.tvPlantHeight.text = formatIncPlant(plantHeights.value)
                binding.tvTemperatureValue.text = temperatureConversionForTemp(getWenDu.value)
                binding.tvWaterTemperatureValue.text = temperatureConversion(getWaterWenDu.value).toString()
            }
        } else {
            mViewMode.setShouldRunJob(false)
            job?.cancel()
        }
    }

    private fun Resource.Success<PlantInfoData>.showTemp() {
        data?.apply {
            // Ensure envirVO is not null before proceeding.
            val environment = if (null == envirVO) {
                // ViewUtils.setVisible(null == data?.envirVO && mViewMode.isZp.value == true, binding.pplantNinth.ivZpAdd)
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

                // 获取温度传感器值 ProMode
                tvTemperatureValue.text = mViewMode.temperatureConversionForTemp(mViewMode.getWenDu.value)
                tvHumidityValue.text = mViewMode.getRoomHumidity(mViewMode.getHumidity.value)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun observe() {
        super.observe()
        mViewMode.apply {
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

            listDevice.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    hideProgressLoading()
                    data?.let { dataList ->
                        // 判断设备数量，设定左右滑动图片的显示
                        // 寻找当前设备
                        dataList.firstOrNull { it.currentDevice == 1 }?.let { device ->
                            // 是否显示摄像头
                            val isCameraVisible =
                                device.accessoryList?.firstOrNull { it.accessoryType == AccessoryListBean.KEY_CAMERA } != null
                            ViewUtils.setVisible(isCameraVisible, binding.ivCamera)
                            ViewUtils.setVisible(isCameraVisible, binding.ivCamera)

                            // 是否显示rlInch
                            ViewUtils.setVisible(
                                device.deviceType == "OG" || device.deviceType == "OG_black",
                                binding.rlInch
                            )
                        }
                    }
                }
            })
            // 水的容积
            getWaterVolume.observe(viewLifecycleOwner) {
                if (isZp.value == true) return@observe
                if (isManual != true) return@observe
                mViewMode.setWaterLevel(it)
            }

            getAirPump.observe(viewLifecycleOwner) {
                binding.ftAirPump.isOpened = it
            }

            getFanIntake.observe(viewLifecycleOwner) {
                binding.fanIntakeSeekbar.setProgress(it.toFloat())
            }

            getFanExhaust.observe(viewLifecycleOwner) {
                binding.fanExhaustSeekbar.setProgress(it.toFloat())
            }

            /**
             * 检查app版本更新
             */
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

            userDetail.observe(viewLifecycleOwner, resourceObserver {
                error { msg, code ->
                    hideProgressLoading()
                    msg?.let { it1 -> ToastUtil.shortShow(it1) }
                    if (mViewMode.loadFirst.value == false) {
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
                        // ViewUtils.setGone(binding.pplantNinth.ivDoorLockStatus)
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
                                    if (mViewMode.loadFirst.value == false) {
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
                        // firstLoginViewVisibile()
                        // 跳转到绑定设备界面，
                        // 跳转绑定界面
                        //                        ARouter.getInstance()
                        //                            .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                        //                            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        //                            .navigation()
                    }
                }
            })

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

            // 循环获取植物基本信息
            plantInfoLoop.observe(viewLifecycleOwner, resourceObserver {
                success {
                    if (null == data) return@success
                    // 植物的休息照片
                    // 植物的健康程度
                    // binding.pplantNinth.tvHealthStatus.text = data?.healthStatus ?: "----"

                    // 显示温湿度
                    showTemp()
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
                        plantCompleteItemAdapter.addChildClickViewIds(com.bbgo.module_home.R.id.ll_title)
                        plantCompleteItemAdapter.setOnItemChildClickListener { adapter, view, position ->
                            val data = adapter.data[position] as? FinishPageData.ListBean
                            when (view.id) {
                                com.bbgo.module_home.R.id.ll_title -> {
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

            // 获取植物基本信息
            plantInfo.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // 显示温湿度
                    showTemp()

                    // 获取植物高度
                    mViewMode.getPlantHeight()

                    binding.tvTitle.text = data?.plantName

                    // 植物的period 周期
                    data?.list?.let {
                        mViewMode.setPeriodList(it)
                    }

                    // 植物信息数据显示
                    binding.tvWeekDay.text = """
                                Week ${data?.week ?: "-"} Day ${data?.day ?: "-"}
                            """.trimIndent()
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

        }
    }


    /**
     * 种植完成，条目适配器
     */
    private val plantCompleteItemAdapter by lazy {
        HomeFinishItemAdapter(mutableListOf())
    }

    /**
     * 检查固件是否需要升级
     */
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
                    context, R.color.mainColor
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
                    /*binding.ivDrainStatus.background =
                        resources.getDrawable(
                            R.mipmap.home_drain_start,
                            context?.theme
                        )*/
                }
                // 排水暂停
                TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_INSTRUCTIONS -> {
                    if (isManual != true) return
                    binding.ivDrainStatus.background =
                        if ((value as? Boolean != true)) {
                            resources.getDrawable(
                                com.bbgo.module_home.R.mipmap.home_drain_start_black,
                                context?.theme
                            )
                        } else {
                            resources.getDrawable(
                                com.bbgo.module_home.R.mipmap.home_drain_pause_black,
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
                    // isPrivateMode(value)

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
                        // 手动模式，设置当前灯光值或者是预设值。
                        mViewMode.getCurrentProMode(mViewMode.userDetail.value?.data?.deviceId.toString())
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
                ViewUtils.setGone(binding.clTop)
                offLineTextSpan()
            }

            Constants.Device.KEY_DEVICE_ONLINE -> {
                logI(
                    """
                    deviceStatus: Constants.Device.KEY_DEVICE_ONLINE
                """.trimIndent()
                )
                ViewUtils.setGone(binding.plantOffLine.root)
                if (binding.clTop.visibility == View.GONE) {
                    ViewUtils.setVisible(binding.clTop)
                }
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

    override fun lazyLoad() {
        // 设备不在线
        binding.plantOffLine.apply {
            title.setRightButtonImg(com.bbgo.module_home.R.mipmap.home_device_list)
                .setRightClickListener {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                        .navigation(activity)
                }
        }

        // 手动模式点击事件
        binding.apply {
            // 直播
            ivLive.setSafeOnClickListener {
                startToVideoPlay()
            }

            // 图表
            ivChart.setSafeOnClickListener {
                context?.let {
                    it.startActivity(Intent(it, PeriodActivity::class.java))
                }
            }

            // 保存
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
                        asCustom(PresetPop(it, bean, mViewMode.getPreset.value?.data) { presetData ->
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

            // 加载
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

            // 排水感叹号
            tvDripPumpDesc.setOnClickListener {
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_DRIP)
            }

            // 设备列表
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

            // 气泵感叹号
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

            // 感叹号
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
                context?.let { it1 ->
                    xpopup(it1) {
                        isDestroyOnDismiss(false)
                        enableDrag(true)
                        dismissOnTouchOutside(false)
                        // maxHeight(dp2px(600f))
                        asCustom(context?.let { it1 -> HomeDripPop(it1) }).show()
                    }
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
                synchronized(this@BlackHomeFragment) {
                    if (mViewMode.getDrainageFlag.value == true) {
                        ivDrainStatus.setBackgroundResource(com.bbgo.module_home.R.mipmap.home_drain_pause_black)
                    } else {
                        ivDrainStatus.setBackgroundResource(com.bbgo.module_home.R.mipmap.home_drain_start_black)
                    }
                }
            }

            ftAirPump.setOnClickListener {
                val isChecked: Boolean = binding.ftAirPump.isOpened
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
                    return@setOnClickListener
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
                    return@setOnClickListener
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
    }

    /**
     * 跳转到直播界面
     */
    private fun startToVideoPlay() {
        val intent = Intent(context, VideoPLayActivity::class.java)
        intent.putExtra(WebActivity.KEY_WEB_URL, mViewMode.userDetail.value?.data?.liveLink)
        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Live")
        startActivity(intent)
    }

    /**
     * 跳转选择种子还是继承界面回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                HomeFragment.KEY_FOR_CLONE_RESULT -> {
                }

                HomeFragment.KEY_FOR_USER_NAME -> {
                    // 刷新接口
                    val isRefresh =
                        data?.getBooleanExtra(Constants.Global.KEY_REFRESH_PLANT_INFO, false)
                    if (isRefresh == true) {
                        mViewMode.plantInfo()
                    }
                }

                // 日历界面返回刷新
                HomeFragment.KEY_FOR_CALENDAR_REFRSH -> {

                }

                /*// 主页或者离线页面跳转到设备界面
                KEY_FOR_USER_NAME -> {
                    // 切换设备
                    data?.getStringExtra(Constants.Global.KEY_IS_SWITCH_DEVICE)?.let { mViewMode.switchDevice(it) }
                }*/
            }
        }

    }

    /**
     * pop base
     */
    private val pop by lazy {
        XPopup.Builder(context)
    }

    /**
     * 事件选择
     */
    private fun HomeBlackProModeFragmentBinding.chooserTime() {
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
                    // changUnReadMessageUI()
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
                    // cameraStopOnpause()
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
     * 定时任务开启
     */
    // 倒计时携程任务
    private var job: Job? = null
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
     * 升级弹窗
     */
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
}