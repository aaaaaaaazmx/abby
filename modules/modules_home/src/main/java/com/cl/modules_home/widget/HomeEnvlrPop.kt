package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.bean.EnvironmentInfoData
import com.cl.modules_home.adapter.HomeEnvirPopAdapter
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeEnvlrPopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 干燥程度
 *
 * @author 李志军 2022-08-11 18:00
 */
class HomeEnvlrPop(
    context: Context,
    private var data: MutableList<EnvironmentInfoData>? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_envlr_pop
    }

    private val adapter by lazy {
        HomeEnvirPopAdapter(mutableListOf())
    }

    fun setData(data: MutableList<EnvironmentInfoData>) {
        this.data = data
    }

    override fun beforeShow() {
        super.beforeShow()
        adapter.setList(data)
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<HomeEnvlrPopBinding>(popupImplView)?.apply {
            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter
            ivClose.setOnClickListener { dismiss() }
        }
    }
}