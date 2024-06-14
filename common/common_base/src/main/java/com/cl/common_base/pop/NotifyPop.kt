package com.cl.common_base.pop

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.R
import com.cl.common_base.bean.MessageConfigBean
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.MyPopNotifyBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


/**
 * 通知界面的弹窗设置
 */
class NotifyPop(context: Context, val activity: FragmentActivity, private val getMessageConfigAction: ((Boolean)->Unit)? = null,) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_pop_notify
    }

    // 直接借口调用
    private val service = ServiceCreators.create(BaseApiService::class.java)
    private lateinit var binding: MyPopNotifyBinding

    private val loadingPopup by lazy {
        XPopup.Builder(context).asLoading("Loading...")
    }

    private val userInfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<MyPopNotifyBinding>(popupImplView)?.apply {
            executePendingBindings()

            // 检查是否有权限
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val isHash = PermissionHelp().hasPermissions(context, Manifest.permission.POST_NOTIFICATIONS)
                    if (!isHash) {
                        PermissionHelp().applyPermissionHelp(
                            activity,
                            "This app requires notification permission to keep you updated with the latest information. Please enable notification permissions in the settings.",
                            object : PermissionHelp.OnCheckResultListener {
                                override fun onResult(result: Boolean) {
                                    if (result) {
                                        ToastUtil.shortShow("Notification permission granted")
                                    } else {
                                        ToastUtil.shortShow("Notification permission denied")
                                    }
                                }
                            },
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
            }

            tvDone.setSafeOnClickListener { dismiss() }
            ivClose.setSafeOnClickListener { dismiss() }

            // Recommended: Turn on Calendar and Alert notifications to stay informed about important updates.
            tvDesc.text = buildSpannedString {
                bold { append("Recommended: ") }
                append("Turn on ")
                bold { append("Calendar ") }
                append("and ")
                bold { append("Alert ") }
                append("notifications to stay informed about important updates.")
            }

            switchNotify.setSafeOnClickListener {
                messageConfig(
                    MessageConfigBean(
                        calendar = switchNotify.isChecked,
                        alert = switchWare.isChecked,
                        community = switchComm.isChecked,
                        promotion = switchAsd.isChecked,
                        userId = userInfoBean?.userId?.safeToInt() ?: 0
                    )
                )
            }
            switchWare.setSafeOnClickListener {
                messageConfig(
                    MessageConfigBean(
                        calendar = switchNotify.isChecked,
                        alert = switchWare.isChecked,
                        community = switchComm.isChecked,
                        promotion = switchAsd.isChecked,
                        userId = userInfoBean?.userId?.safeToInt() ?: 0
                    )
                )
            }
            switchComm.setSafeOnClickListener {
                messageConfig(
                    MessageConfigBean(
                        calendar = switchNotify.isChecked,
                        alert = switchWare.isChecked,
                        community = switchComm.isChecked,
                        promotion = switchAsd.isChecked,
                        userId = userInfoBean?.userId?.safeToInt() ?: 0
                    )
                )
            }
            switchAsd.setSafeOnClickListener {
                messageConfig(
                    MessageConfigBean(
                        calendar = switchNotify.isChecked,
                        alert = switchWare.isChecked,
                        community = switchComm.isChecked,
                        promotion = switchAsd.isChecked,
                        userId = userInfoBean?.userId?.safeToInt() ?: 0
                    )
                )
            }

            // 获取配置
            getMessageConfig()
        }!!
    }

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
                    loadingPopup.dismiss()
                    binding.switchNotify.isChecked = it.data?.calendar == true
                    binding.switchWare.isChecked = it.data?.alert == true
                    binding.switchComm.isChecked = it.data?.community == true
                    binding.switchAsd.isChecked = it.data?.promotion == true
                    getMessageConfigAction?.invoke(it.data?.calendar == true || it.data?.alert == true || it.data?.community == true || it.data?.promotion == true)
                }

                is Resource.DataError -> {
                    loadingPopup.dismiss()
                    ToastUtil.shortShow(it.errorMsg)
                }

                is Resource.Loading -> {
                    if (!loadingPopup.isShow) {
                        loadingPopup.show()
                    }
                }
            }
        }
    }

    // 设置配置
    private fun messageConfig(baseBean: MessageConfigBean) = lifecycleScope.launch {
        service.messageConfig(baseBean).map {
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
                    getMessageConfig()
                }

                is Resource.DataError -> {
                    loadingPopup.dismiss()
                    ToastUtil.shortShow(it.errorMsg)
                }

                is Resource.Loading -> {
                    loadingPopup.show()
                }
            }
        }
    }
}