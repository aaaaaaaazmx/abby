package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeFinishGuideBinding
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.bean.FinishPageData
import com.cl.modules_home.adapter.PlantFinishPopAdapter
import com.cl.modules_home.response.DetailByLearnMoreIdData
import com.lxj.xpopup.core.BottomPopupView

/**
 * 种植完成之后的通用弹窗
 */
class HomeFinishGuidePop(
    context: Context,
    var list: MutableList<DetailByLearnMoreIdData.ItemBean>? = null,
    var datas: DetailByLearnMoreIdData? = null
) : BottomPopupView(context) {
    private val adapter by lazy {
        PlantFinishPopAdapter(mutableListOf())
    }

    override fun getImplLayoutId(): Int {
        return R.layout.home_finish_guide
    }

    fun setData(data: DetailByLearnMoreIdData?) {
        datas = data
        list = data?.items
        binding?.tvTitle?.text = data?.title
    }

    override fun beforeShow() {
        super.beforeShow()
        adapter.setList(list)
        binding?.tvTitle?.text = datas?.title
    }

    var binding: HomeFinishGuideBinding? = null
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<HomeFinishGuideBinding>(popupImplView)?.apply {
            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter

            ivClose.setOnClickListener { dismiss() }
        }
    }
}