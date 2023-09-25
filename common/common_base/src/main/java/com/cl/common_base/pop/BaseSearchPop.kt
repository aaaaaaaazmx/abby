package com.cl.common_base.pop

import android.content.Context
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.adapter.StrainNameSearchAdapter
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BaseSearchPopBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.setVisible
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.SoftInputUtils
import com.lxj.xpopup.core.BubbleAttachPopupView
import com.thingclips.bouncycastle.crypto.params.ECDomainParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.net.ConnectException

class BaseSearchPop(context: Context, private val onItemClickAction: ((strainName: String) -> Unit)? = null): BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_search_pop
    }
    private var editText: EditText? = null

    private val searchAdapter by lazy {
        StrainNameSearchAdapter(mutableListOf())
    }

    fun setData(strainName: String, editText: EditText) {
        this.editText = editText
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseSearchPopBinding>(popupImplView)?.apply {
            rvSearch.layoutManager = LinearLayoutManager(context)
            rvSearch.adapter = searchAdapter

            searchAdapter.setOnItemClickListener { adapter, view, position ->
                editText?.setText(adapter.data[position].toString())
                editText?.setSelection(editText?.text.toString().length)
                onItemClickAction?.invoke(adapter.data[position].toString())
                dismiss()
            }
        }
    }

    fun setDatas(data: MutableList<String>?) {
        if (data.isNullOrEmpty()) {
            dismiss()
            return
        }
        searchAdapter.setList(data)
    }
}