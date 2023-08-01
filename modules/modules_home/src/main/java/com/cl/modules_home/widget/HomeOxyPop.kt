package com.cl.modules_home.widget

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeOxyPopBinding
import com.cl.common_base.adapter.OxygenCoinBillAdapter
import com.cl.common_base.bean.AccountFlowingReq
import com.cl.common_base.bean.Flowing
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.service.HttpHomeApiService
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 氧气币弹窗界面
 */
class HomeOxyPop(context: Context) : BottomPopupView(context) {
    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    override fun getImplLayoutId(): Int {
        return R.layout.home_oxy_pop
    }

    private val adapters by lazy {
        OxygenCoinBillAdapter(mutableListOf())
    }


    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeOxyPopBinding>(popupImplView)?.apply {
            flImg.setOnClickListener {  // 添加
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_OXYGEN_COIN)
            }


            rvWxCircle.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = adapters
            }

            lifecycleScope.launch {
                adapters.addData(getOxygenCoinBillList())
            }
        }
    }


    private suspend fun getOxygenCoinBillList(): MutableList<Flowing> {
        val mutableList = mutableListOf<Flowing>()
        service.oxygenCoinBillList(AccountFlowingReq(1, 500)).map {
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
            when (it) {
                is Resource.Success -> {
                    logI(it.toString())
                    it.data?.let { it1 -> mutableList.addAll(it1.flowing) }
                }

                else -> {ToastUtil.shortShow(it.errorMsg)}
            }
        }
        return mutableList
    }
}