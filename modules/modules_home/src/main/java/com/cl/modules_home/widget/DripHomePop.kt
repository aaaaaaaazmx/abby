package com.cl.modules_home.widget

import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.cl.modules_home.adapter.DripAdapter
import com.cl.modules_home.databinding.HomeDripHomePopBinding
import com.cl.modules_home.request.DripListData
import com.cl.modules_home.service.HttpHomeApiService
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.CenterPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class DripHomePop(context: Context, val deviceId: String) : BottomPopupView(context) {
    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    override fun getImplLayoutId(): Int {
        return R.layout.home_drip_home_pop
    }

    private val adapter by lazy {
        DripAdapter(mutableListOf())
    }

    private lateinit var binding: HomeDripHomePopBinding

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<HomeDripHomePopBinding>(popupImplView)?.apply {
            executePendingBindings()

            recyclview.layoutManager = LinearLayoutManager(context)
            recyclview.adapter = this@DripHomePop.adapter

            // 获取滴灌参数列表
            lifecycleScope.launch {
                getDripList()
            }

            // 拷贝上个。
            ivAdd.setSafeOnClickListener {
                if (adapter.data.size == 2) return@setSafeOnClickListener
                val lastData = adapter.data.lastOrNull() ?: return@setSafeOnClickListener
                adapter.addData(lastData)
                recyclview.smoothScrollToPosition(adapter.data.size - 1)
                if (adapter.data.size == 2) ivAdd.visibility = View.GONE else ivAdd.visibility = View.VISIBLE
            }

            tvConfirm.setSafeOnClickListener {
                dismiss()
            }
        }!!

        adapter.addChildClickViewIds(R.id.iv_close)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.iv_close -> {
                    adapter.removeAt(position)
                }
            }
        }
    }

    override fun onDismiss() {
        lifecycleScope.launch {
            updateDripList(DripListData(dripIrrigationTimerStatus = binding.ftSwitch.isItemChecked, list = adapter.data as? MutableList<DripListData.DripData>))
        }
        super.onDismiss()
    }


    private suspend fun updateDripList(req: DripListData) = service.trickleIrrigationConfigList(req).map {
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
        emit(
            Resource.DataError(
                -1, "$it"
            )
        )
    }.collectLatest {
        when (it) {
            is Resource.Success -> {

            }

            is Resource.DataError -> {
                // 弹出错误提示框
                ToastUtil.shortShow(it.errorMsg)
            }

            else -> {}
        }
    }


    private suspend fun getDripList() = service.getTrickleIrrigationConfigList(deviceId).map {
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
        emit(
            Resource.DataError(
                -1, "$it"
            )
        )
    }.collectLatest {
        when (it) {
            is Resource.Success -> {
                binding.ftSwitch.isItemChecked = it.data?.dripIrrigationTimerStatus == true
                adapter.setList(it.data?.list)
                if ((it.data?.list?.size ?: 0) >= 2) binding.ivAdd.visibility = View.GONE else View.VISIBLE
            }

            is Resource.DataError -> {
                // 弹出错误提示框
                ToastUtil.shortShow(it.errorMsg)
            }

            else -> {}
        }
    }
}