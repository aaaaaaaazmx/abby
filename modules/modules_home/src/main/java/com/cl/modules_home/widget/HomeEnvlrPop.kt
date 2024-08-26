package com.cl.modules_home.widget

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.bean.EnvironmentInfoData
import com.cl.modules_home.adapter.HomeEnvirPopAdapter
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeEnvlrPopBinding
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.ModifyUserDetailReq
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.ChooseTimePop
import com.cl.common_base.pop.HomePlantDrainPop
import com.cl.common_base.pop.NotifyPop
import com.cl.common_base.pop.activity.BasePumpActivity
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.service.HttpHomeApiService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.Calendar
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 干燥程度
 *
 * @author 李志军 2022-08-11 18:00
 */
class HomeEnvlrPop(
    private val activity: FragmentActivity,
    context: Context,
    private var disMissAction: (() -> Unit)? = null,
    private var data: MutableList<EnvironmentInfoData.Environment>? = null,
    private var strainName: String? = null,
    private var currentDeviceInfo: ListDeviceBean? = null,
    private var userInfo: UserinfoBean.BasicUserBean? = null,
) : BottomPopupView(context){

    override fun getImplLayoutId(): Int {
        return R.layout.home_envlr_pop
    }

    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    private val adapter by lazy {
        HomeEnvirPopAdapter(mutableListOf())
    }

    fun setData(data: MutableList<EnvironmentInfoData.Environment>, currentDeviceInfo: ListDeviceBean?, userInfo: UserinfoBean.BasicUserBean?) {
        this.data = data
        this.currentDeviceInfo = currentDeviceInfo
        this.userInfo = userInfo
    }

    fun setStrainName(strainName: String?) {
        if (strainName == "I don’t know") {
            this.strainName = ""
        } else {
            this.strainName = strainName
        }
    }

    override fun beforeShow() {
        super.beforeShow()
        adapter.setList(data)
        // binding?.tvPlantName?.text = strainName
        currentDeviceInfo?.let {
            binding?.apply {
                cbNotify.isChecked = userInfo?.openNotify == 1
                cbNight.isChecked = it.nightMode == 1
                cbLock.isChecked = it.childLock == 1

                // 是否显示排水
                ViewUtils.setVisible(currentDeviceInfo?.waterPump == true, cbDrain, tvDrain)
            }
        }
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
        disMissAction?.invoke()
    }


    private var binding: HomeEnvlrPopBinding? = null
    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomeEnvlrPopBinding>(popupImplView)?.apply {
            /*userInfo?.apply {
                ViewUtils.setGone(noheadShow, TextUtils.isEmpty(avatarPicture))
                ViewUtils.setGone(ivAvatar, !TextUtils.isEmpty(avatarPicture))
                Glide.with(ivAvatar.context).load(avatarPicture)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar)
                noheadShow.text = nickName?.substring(0, 1)

                tvNickname.text = nickName
                tvPlantName.Stext = strainName
            }*/

            // 获取消息配置
            getMessageConfig()

            cbNotify.setSafeOnClickListener {
                val isChecked = cbNotify.isChecked
                notifySetting(isChecked)
            }
            cbNight.setSafeOnClickListener {
                val isChecked = cbNight.isChecked
                nightSetting(isChecked)
            }
            cbDrain.setSafeOnClickListener {
                cbDrain.isChecked = false
                // 跳转到换水页面
                xpopup(context) {
                    maxHeight(dp2px(600f))
                    isDestroyOnDismiss(false)
                    dismissOnTouchOutside(false)
                    enableDrag(false)
                    asCustom(
                        HomePlantDrainPop(context, onNextAction = {
                            // 传递的数据为空
                            val intent = Intent(context, BasePumpActivity::class.java)
                            context.startActivity(intent)
                        })
                    ).show()
                }
            }
            cbLock.setSafeOnClickListener {
                val isChecked = cbLock.isChecked
                logI("123123: ischecked: $isChecked")
                // 是否打开童锁
                DeviceControl.get().success {
                        lifecycleScope.launch {
                            upDeviceInfo(
                                UpDeviceInfoReq(
                                    childLock = if (isChecked) 1 else 0, deviceId = currentDeviceInfo?.deviceId ?: userInfo()?.deviceId
                                ), LOCK, isChecked
                            )
                        }
                    }.error { code, error ->
                        ToastUtil.shortShow(
                            """
                              childLock: 
                              code-> $code
                              errorMsg-> $error
                             """.trimIndent()
                        )
                    }.childLock(isChecked)
            }

            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter
            ivClose.setSafeOnClickListener { dismiss() }
            // 开关监听
            adapter.setOnCheckedChangeListener(object :
                HomeEnvirPopAdapter.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    lifecycleScope.launch {
                        upDeviceInfo(
                            UpDeviceInfoReq(
                                fanAuto = if (isChecked) 1 else 0,
                                deviceId = currentDeviceInfo?.deviceId ?: userInfo?.deviceId
                            )
                        )
                    }
                }
            })
            adapter.addChildClickViewIds(R.id.rl_edit, R.id.iv_refresh)
            adapter.setOnItemChildClickListener { adapter, view, position ->
                when (view.id) {
                    R.id.rl_edit -> {
                        val item = adapter.getItem(position) as EnvironmentInfoData.Environment
                        XPopup.Builder(context)
                            .dismissOnTouchOutside(false)
                            .isDestroyOnDismiss(false)
                            .asCustom(
                                BaseCenterPop(
                                    context,
                                    onConfirmAction = {
                                        // 跳转到InterCome文章详情里面去
                                        InterComeHelp.INSTANCE.openInterComeSpace(
                                            space = InterComeHelp.InterComeSpace.Article,
                                            id = item.articleId
                                        )
                                    },
                                    confirmText = "Detail",
                                    content = item.articleDetails,
                                )
                            ).show()
                    }

                    // 刷新灯光
                    R.id.iv_refresh -> {
                        // 上一次刷新的时间
                        val lastRefreshTime = Prefs.getLong(Constants.Login.KEY_REFRESH_TIME)
                        val time = System.currentTimeMillis()
                        // 初始化日历对象
                        val currentCalendar = Calendar.getInstance().apply {
                            timeInMillis = time
                        }
                        val lastSnapshotCalendar = Calendar.getInstance().apply {
                            timeInMillis = lastRefreshTime
                        }

                        // 判断今天是否已经截图
                        if (lastRefreshTime == 0L || currentCalendar.get(Calendar.YEAR) != lastSnapshotCalendar.get(
                                Calendar.YEAR
                            ) || currentCalendar.get(Calendar.DAY_OF_YEAR) != lastSnapshotCalendar.get(
                                Calendar.DAY_OF_YEAR
                            )
                        ) {
                            XPopup.Builder(context)
                                .dismissOnTouchOutside(false)
                                .isDestroyOnDismiss(false)
                                .asCustom(
                                    BaseCenterPop(
                                        context,
                                        content = "In very rare case, the light is not running on schedule due to the connection issue, if you believe your lighting schedule is not correct, please click refresh.\n" +
                                                "\n" +
                                                "Note: you can only refresh once per day.",
                                        isShowCancelButton = true,
                                        cancelText = "Cancel",
                                        confirmText = "Refresh",
                                        onConfirmAction = {
                                            // 刷新回调、并且记录当前时间。
                                            lifecycleScope.launch {
                                                syncLightParam(currentDeviceInfo?.deviceId ?: userInfo?.deviceId.toString())
                                            }
                                            // 如果今天还没刷新，
                                            Prefs.putLong(Constants.Login.KEY_REFRESH_TIME, time)
                                        }
                                    )
                                ).show()
                        } else {
                            XPopup.Builder(context)
                                .dismissOnTouchOutside(false)
                                .isDestroyOnDismiss(false)
                                .asCustom(
                                    BaseCenterPop(
                                        context,
                                        content = "you can only refresh once per day.",
                                        isShowCancelButton = false,
                                        confirmText = "Confirm",
                                    )
                                ).show()
                        }
                    }
                }
            }
        }
    }

    private fun notifySetting(isChecked: Boolean) {
        /*if (!isChecked) {
            xpopup(context) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                asCustom(BaseCenterPop(context, content = "Whether to disable notifiction", cancelText = "No", confirmText = "Yes", isShowCancelButton = true, onConfirmAction = {
                    lifecycleScope.launch {
                        modifyUserInfo(ModifyUserDetailReq(openNotify = if (isChecked) "1" else "0"), isChecked)
                    }
                }, onCancelAction = {
                    binding?.cbNotify?.isChecked = !isChecked
                })).show()
            }
            return
        }
        lifecycleScope.launch {
            modifyUserInfo(ModifyUserDetailReq(openNotify = if (isChecked) "1" else "0"), isChecked)
        }*/
        xpopup(context) {
            dismissOnTouchOutside(false)
            isDestroyOnDismiss(false)
            asCustom(NotifyPop(context, this@HomeEnvlrPop.activity, getMessageConfigAction = {
                binding?.cbNotify?.isChecked = it
            })).show()
        }
    }

    private fun nightSetting(isChecked: Boolean) {
        if (!isChecked) {
            xpopup(context) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                asCustom(BaseCenterPop(context, content = "Whether to disable night mode", cancelText = "No", confirmText = "Yes", isShowCancelButton = true, onConfirmAction = {
                    lifecycleScope.launch {
                        // 调用接口更新后台夜间模式
                        upDeviceInfo(
                            UpDeviceInfoReq(
                                nightMode = if (isChecked) 1 else 0,
                                deviceId = currentDeviceInfo?.deviceId ?: userInfo?.deviceId,
                            ), NIGHT, isChecked
                        )

                        // 调用关闭点dp点。
                        DeviceControl.get().success {
                            // "141":"muteOn:10,muteOff:22"
                        }.error { code, error ->
                            ToastUtil.shortShow(
                                """
                                                  nightMode: 
                                                  code-> $code
                                                  errorMsg-> $error
                                                    """.trimIndent()
                            )
                        }.nightMode("lightOn:00,lightOff:00")
                    }
                }, onCancelAction = {binding?.cbNight?.isChecked = !isChecked})).show()
            }
            return
        }
        // 设置夜间模式的时间。
        val timerString = parseTime()
        lifecycleScope.launch {
            // 弹出时间调整框
            xpopup(context) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(false)
                asCustom(
                    ChooseTimePop(context,
                        turnOnHour = muteOn?.safeToInt(),
                        turnOffHour = muteOff?.safeToInt(),
                        onCancelAction = {
                            // 这个时间和上面解析时间有问题，需要传递24小时制度
                            lifecycleScope.launch {
                                this@HomeEnvlrPop.upDeviceInfo(
                                    req = UpDeviceInfoReq(
                                        nightMode = if (isChecked) 1 else 0,
                                        nightTimer = timerString,
                                        deviceId = currentDeviceInfo?.deviceId ?: userInfo?.deviceId
                                    ), tag = NIGHT, isChecked = isChecked
                                )
                            }
                        },
                        onConfirmAction = { onTime, offMinute, timeOn, timeOff, timeOpenHour, timeCloseHour ->
                            muteOn = timeOn.toString().padStart(2, '0')
                            muteOff = timeOff.toString().padStart(2, '0')
                            // 这个时间和上面解析时间有问题，需要传递24小时制度
                            lifecycleScope.launch {
                                this@HomeEnvlrPop.upDeviceInfo(
                                    req = UpDeviceInfoReq(
                                        nightMode = if (isChecked) 1 else 0,
                                        nightTimer = "$onTime-$offMinute", deviceId = currentDeviceInfo?.deviceId ?: userInfo?.deviceId
                                    ), tag = NIGHT, isChecked = isChecked
                                )
                            }

                            // 发送dp点
                            DeviceControl.get().success {
                                // "141":"muteOn:10,muteOff:22"
                                logI(
                                    "123312313: lightOn:${
                                        timeOn.toString().padStart(2, '0')
                                    },lightOff:${timeOff.toString().padStart(2, '0')}"
                                )
                            }.error { code, error ->
                                ToastUtil.shortShow(
                                    """
                                                      nightMode: 
                                                      code-> $code
                                                      errorMsg-> $error
                                                        """.trimIndent()
                                )
                            }.nightMode(
                                "lightOn:${
                                    if (timeOn == 12) 24 else timeOn.toString().padStart(2, '0')
                                },lightOff:${
                                    if (timeOff == 24) 12 else timeOff.toString().padStart(2, '0')
                                }"
                            )
                        })
                ).show()
            }
        }
    }

    /**
     * 更新设备信息
     */
    private suspend fun upDeviceInfo(req: UpDeviceInfoReq, tag: Int? = null, isChecked: Boolean? = null) {
        service.updateDeviceInfo(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            logI(it.toString())
            when (it) {
                is Resource.Success -> {
                    // ToastUtil.shortShow("success")
                    when(tag) {
                        NIGHT -> {
                            binding?.cbNight?.isChecked = isChecked!!
                        }
                        LOCK -> {
                            binding?.cbLock?.isChecked = isChecked!!
                        }
                    }
                }
                is Resource.DataError -> {
                    ToastUtil.shortShow(it.errorMsg)
                }
                else -> {}
            }
        }
    }


    /**
     * 同步灯光参数
     */
    private suspend fun syncLightParam(deviceId: String) {
        service.syncLightParam(deviceId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            logI(it.toString())
            when (it) {
                is Resource.Success -> {
                    xpopup(context) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(false)
                        asCustom(BaseCenterPop(context, content = "Light schedule and intensity have been synced with the server. If you still believe there is an error, please contact 1-on-1 support.", isShowCancelButton = false, confirmText = context.getString(
                            com.cl.common_base.R.string.string_10))).show()
                    }
                }

                else -> {}
            }
        }
    }

    // modifyUserDetail
    private suspend fun modifyUserInfo(req: ModifyUserDetailReq, isChecked: Boolean) {
        service.modifyUserDetail(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            logI(it.toString())
            when (it) {
                is Resource.Success -> {
                    binding?.cbNotify?.isChecked = isChecked
                }
                else -> {}
            }
        }
    }

    // 解析时间
    private fun parseTime(): String {
        val str = currentDeviceInfo?.nightTimer.toString()
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
        return "$openTime-$closeTime"
    }

    private var muteOn: String? = null
    private var muteOff: String? = null


    // 获取配置
    private fun getMessageConfig() = lifecycleScope.launch {
        service.messageConfigList().map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            when (it) {
                is Resource.Success -> {
                    binding?.cbNotify?.isChecked = (it.data?.calendar == true || it.data?.alert == true || it.data?.community == true || it.data?.promotion == true)
                }

                is Resource.DataError -> {
                    ToastUtil.shortShow(it.errorMsg)
                }

                is Resource.Loading -> {
                }
            }
        }
    }

    companion object {
        // 1. 通知
        const val NOTIFY = 1
        // 2. 夜间模式
        const val NIGHT = 2
        // 3. 换水
        const val DRAIN = 3
        // 4. 童锁
        const val LOCK = 4

    }
}