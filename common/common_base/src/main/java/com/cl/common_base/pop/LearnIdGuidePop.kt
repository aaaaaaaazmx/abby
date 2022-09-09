package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.adapter.LearnFinishPopAdapter
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.databinding.HomeFinishGuideBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 种植完成之后的通用弹窗
 */
class LearnIdGuidePop(
    context: Context,
    var list: MutableList<DetailByLearnMoreIdData.ItemBean>? = null,
    var datas: DetailByLearnMoreIdData? = null
) : BottomPopupView(context) {
    private val adapter by lazy {
        LearnFinishPopAdapter(mutableListOf())
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