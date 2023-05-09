package com.cl.modules_home.widget

import android.content.Context
import android.text.TextUtils
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.bean.EnvironmentInfoData
import com.cl.modules_home.adapter.HomeEnvirPopAdapter
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeEnvlrPopBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.service.HttpHomeApiService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.tuya.smart.sdk.bean.DeviceBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 干燥程度
 *
 * @author 李志军 2022-08-11 18:00
 */
class HomeEnvlrPop(
    context: Context,
    private var disMissAction: (() -> Unit)? = null,
    private var data: MutableList<EnvironmentInfoData.Environment>? = null,
    private var strainName: String? = null,
) : BottomPopupView(context) {

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    override fun getImplLayoutId(): Int {
        return R.layout.home_envlr_pop
    }

    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    private val adapter by lazy {
        HomeEnvirPopAdapter(mutableListOf())
    }

    fun setData(data: MutableList<EnvironmentInfoData.Environment>) {
        this.data = data
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
        binding?.tvPlantName?.text = strainName
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
        disMissAction?.invoke()
    }


    val tuyaHomeBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    private var binding: HomeEnvlrPopBinding? = null
    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomeEnvlrPopBinding>(popupImplView)?.apply {
            userInfo?.apply {
                ViewUtils.setGone(noheadShow, TextUtils.isEmpty(avatarPicture))
                ViewUtils.setGone(ivAvatar, !TextUtils.isEmpty(avatarPicture))
                Glide.with(ivAvatar.context).load(avatarPicture)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar)
                noheadShow.text = nickName?.substring(0, 1)

                tvNickname.text = nickName
                tvPlantName.text = strainName
            }

            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter
            ivClose.setOnClickListener { dismiss() }
            // 开关监听
            adapter.setOnCheckedChangeListener(object :
                HomeEnvirPopAdapter.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    lifecycleScope.launch {
                        upDeviceInfo(UpDeviceInfoReq(fanAuto = if (isChecked) 1 else 0, deviceId = tuyaHomeBean?.devId))
                    }
                }
            })
            adapter.addChildClickViewIds(R.id.rl_edit)
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
                                        InterComeHelp.INSTANCE.openInterComeSpace(space = InterComeHelp.InterComeSpace.Article, id = item.articleId)
                                    },
                                    confirmText = "Detail",
                                    content = item.articleDetails,
                                )
                            ).show()
                    }
                }
            }
        }
    }

    /**
     * 更新设备信息
     */
    private suspend fun upDeviceInfo(req: UpDeviceInfoReq) {
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
        }
    }
}