package com.cl.modules_my.pop

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.ChooseTimePop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyEditProfilePopBinding
import com.cl.modules_my.service.HttpMyApiService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.thingclips.smart.sdk.bean.DeviceBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 设备修改名字、 修改属性弹窗
 */
class EditPlantProfilePop(
    context: Context,
    private val plantName: String? = null,
    private val strainName: String? = null,
    private val beanData: ListDeviceBean? = null,
    private val onConfirmAction: ((planeName: String?, strainName: String?) -> Unit)? = null,
    private val onDeviceChanged: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_edit_profile_pop
    }

    private val userInfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    private var binding: MyEditProfilePopBinding? = null

    @SuppressLint("StringFormatMatches")
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<MyEditProfilePopBinding>(popupImplView)?.apply {
            etEmail.setText(plantName)
            etCode.setText(strainName)

            ivClose.setOnClickListener { dismiss() }

            tvDec.text = context.getString(com.cl.common_base.R.string.my_which_account, userInfoBean?.email)

            etEmail.doAfterTextChanged {
                (etEmail.text.toString().isNotEmpty()).also { btnSuccess.isEnabled = it }
            }

            etCode.doAfterTextChanged {
                (etEmail.text.toString().isNotEmpty()).also { btnSuccess.isEnabled = it }
            }

            ivClearEmail.setOnClickListener {
                etEmail.setText("")
            }
            ivClearCode.setOnClickListener {
                etCode.setText("")
            }

            btnSendCode.setOnClickListener {
                // 发送验证码
                lifecycleScope.launch {
                    verifyEmail(etEmail.text.toString())
                }
            }

            btnSuccess.setOnClickListener {
                /*var email = etEmail.text.toString()
                // 如果带过来不是空的，但是确认的时候空了，则为默认
                if (plantName?.isNotEmpty() == true && email.isEmpty()) {
                    email = plantName
                }*/

                // 跳转到合并确认页面
                onConfirmAction?.invoke(etEmail.text.toString(), etCode.text.toString())
                dismiss()
            }

            /**
             * 夜间模式
             */
            ftNight.isItemChecked = beanData?.nightMode == 1
            ftChildLock.isItemChecked = beanData?.childLock == 1
            ViewUtils.setVisible(beanData?.nightMode == 1, ftTimer)
            getNightTimer(this)

            // 夜间模式
            ftNight.setSwitchCheckedChangeListener { _, isChecked ->
                logI("1231231: ${beanData?.deviceId}")
                // muteOn:00,muteOff:001
                ViewUtils.setVisible(isChecked, ftTimer)
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
                        .nightMode("lightOn:00,lightOff:00", devId = beanData?.deviceId)
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
                        .nightMode(context.getString(com.cl.common_base.R.string.my_lighton_lightoff, if (muteOn?.safeToInt() == 12) 24 else muteOn, if (muteOff?.safeToInt() == 24) 12 else muteOff))
                }

                // 调用接口更新后台夜间模式
                lifecycleScope.launch {
                    updateDeviceInfo(
                        UpDeviceInfoReq(
                            nightMode = if (isChecked) 1 else 0,
                            deviceId = beanData?.deviceId
                        )
                    )
                }
            }

            /**
             * 夜间模式选择时间
             */
            ftTimer.setOnClickListener {
                XPopup.Builder(context)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(false)
                    .asCustom(
                        ChooseTimePop(
                            context,
                            turnOnHour = muteOn?.safeToInt(),
                            turnOffHour = muteOff?.safeToInt(),
                            onConfirmAction = { onTime, offMinute, timeOn, timeOff, timeOpenHour, timeCloseHour ->
                                ftTimer.itemValue = "$onTime-$offMinute"
                                muteOn = "$timeOn"
                                muteOff = "$timeOff"
                                // todo 这个时间和上面解析时间有问题，需要传递24小时制度
                                lifecycleScope.launch {
                                    updateDeviceInfo(
                                        UpDeviceInfoReq(
                                            nightTimer = binding?.ftTimer?.itemValue.toString(),
                                            deviceId = beanData?.deviceId
                                        )
                                    )
                                }
                                // 发送dp点
                                DeviceControl.get()
                                    .success {
                                        // "141":"muteOn:10,muteOff:22"
                                        logI("123312313: lightOn:$timeOn,lightOff:$timeOff")
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
                                    .nightMode(context.getString(com.cl.common_base.R.string.my_lighton_lightoff_two, if (timeOn == 12) 24 else timeOn, if (timeOff == 24) 12 else timeOff), devId = beanData?.deviceId)
                            })
                    ).show()
            }

            /**
             * 童锁
             */
            // 童锁
            ftChildLock.setSwitchCheckedChangeListener { _, isChecked ->
                onDeviceChanged?.invoke()
                // 是否打开童锁
                DeviceControl.get()
                    .success {
                        lifecycleScope.launch {
                            updateDeviceInfo(
                                UpDeviceInfoReq(
                                    childLock = if (isChecked) 1 else 0,
                                    deviceId = beanData?.deviceId
                                )
                            )
                        }
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
                    .childLock(isChecked, beanData?.deviceId)
            }

        }
    }

    private var muteOn: String? = null
    private var muteOff: String? = null
    private fun getNightTimer(bindings: MyEditProfilePopBinding?) {
        val str = beanData?.nightTimer.toString()
        val pattern = "(\\d{1,2}):\\d{2} [AP]M-(\\d{1,2}):\\d{2} [AP]M"

        val p: Pattern = Pattern.compile(pattern)
        val m: Matcher = p.matcher(str)
        var openTime: String? = "10:00 PM"
        var closeTime: String? = "7:00 AM"
        if (m.find()) {
            muteOn = m.group(1)
            muteOff = m.group(2)
            val onHour = muteOn?.safeToInt() ?: 0
            val offHour = muteOff?.safeToInt() ?: 0

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
        logI("11111: ${"$openTime-$closeTime"}")
        bindings?.ftTimer?.itemValue = "$openTime-$closeTime"
    }


    /**
     * 发送验证码
     */
    private val service = ServiceCreators.create(HttpMyApiService::class.java)
    private suspend fun updateDeviceInfo(body: UpDeviceInfoReq) {
        service.updateDeviceInfo(body).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.Main).onStart {
            emit(Resource.Loading())
        }.catch {
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collect {
            logI(it.toString())
            when (it) {
                is Resource.Success -> {
                    // todo 更新成功
                    onDeviceChanged?.invoke()
                }
                is Resource.DataError -> {
                    // todo 更新失败
                }
                is Resource.Loading -> {
                    // todo 加载中
                }
            }
        }
    }

    /**
     * 发送验证码
     */
    private suspend fun verifyEmail(email: String) {
        service.verifyEmail(email = email, type = "4").map {
            if (it.code != Constants.APP_SUCCESS) {
                if (it.msg == "not exist user") {
                    // todo 弹出没有这个弹窗的pop
                    XPopup.Builder(context)
                        .isDestroyOnDismiss(false)
                        .isDestroyOnDismiss(false)
                        .asCustom(
                            BaseCenterPop(context,
                                content = context.getString(com.cl.common_base.R.string.string_1869),
                                isShowCancelButton = false,
                                onConfirmAction = {
                                })
                        )
                        .show()
                }
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                // todo 发送完毕。
                binding?.btnSendCode?.text = context.getString(com.cl.common_base.R.string.string_1870)
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.Main).onStart {
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
        }
    }
}