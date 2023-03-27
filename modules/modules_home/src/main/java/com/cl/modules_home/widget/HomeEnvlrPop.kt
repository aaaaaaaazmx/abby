package com.cl.modules_home.widget

import android.content.Context
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.bean.EnvironmentInfoData
import com.cl.modules_home.adapter.HomeEnvirPopAdapter
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeEnvlrPopBinding
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.service.HttpHomeApiService
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
    private var disMissAction: (()-> Unit)? = null,
    private var data: MutableList<EnvironmentInfoData.Environment>? = null,
) : BottomPopupView(context) {
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

    override fun beforeShow() {
        super.beforeShow()
        adapter.setList(data)
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
        disMissAction?.invoke()
    }


    val tuyaHomeBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }


    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<HomeEnvlrPopBinding>(popupImplView)?.apply {
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