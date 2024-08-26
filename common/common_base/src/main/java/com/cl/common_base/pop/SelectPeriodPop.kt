package com.cl.common_base.pop

import android.content.Context
import android.os.Build
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.R
import com.cl.common_base.bean.MessageConfigBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BaseSelectPeriodPopBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.common_base.widget.wheel.time.StringPicker
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SelectPeriodPop(context: Context, private val periodString: String, private val plantId: String, private val selectAction: ((String) -> Unit)? = null) : CenterPopupView(context),
    StringPicker.OnStringSelectedListener {
    override fun getImplLayoutId(): Int {
        return R.layout.base_select_period_pop
    }

    // 直接借口调用
    private val service = ServiceCreators.create(BaseApiService::class.java)
    private lateinit var binding: BaseSelectPeriodPopBinding


    private val loadingPopup by lazy {
        XPopup.Builder(context).asLoading(context.getString(R.string.string_216))
    }

    private var selectString: String? = null

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<BaseSelectPeriodPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@SelectPeriodPop
            executePendingBindings()
            messageConfig(plantId)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                periodSelect.setOnStringSelectedListener(this@SelectPeriodPop)
            }

            tvCancel.setSafeOnClickListener { dismiss() }
            tvConfirm.setSafeOnClickListener {
                if (!periodSelect.dataList.isNullOrEmpty()) {
                    if (selectString.isNullOrBlank()) {
                        selectString = periodSelect.dataList[0]
                    }
                }
                selectString?.let { it1 -> selectAction?.invoke(it1) }
                dismiss()
            }
        }!!
    }

    // 设置配置
    private fun messageConfig(plantId: String) = lifecycleScope.launch {
        service.getPeriodList(plantId).map {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        binding.periodSelect.setStringList(it.data)
                        it.data?.indexOf(periodString)?.let { it1 -> binding.periodSelect.setSelectedScope(it1) }
                    }
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

    override fun onScopeSelected(index: Int) {
    }

    override fun onScopeSelected(index: String?) {
        selectString = index
    }
}