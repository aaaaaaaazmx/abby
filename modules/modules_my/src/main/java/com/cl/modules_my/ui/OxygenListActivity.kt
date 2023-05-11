package com.cl.modules_my.ui

import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyOxyGenActivityBinding
import com.cl.modules_my.request.AccountFlowingReq
import com.cl.modules_my.viewmodel.OxyGenListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 氧气币流水账单界面
 */
@AndroidEntryPoint
class OxygenListActivity: BaseActivity<MyOxyGenActivityBinding>() {

    @Inject
    lateinit var viewModel: OxyGenListViewModel

    override fun initView() {

    }

    override fun observe() {
        viewModel.apply {
            oxygenCoinBillList.observe(this@OxygenListActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {

                }
            })
        }
    }

    override fun initData() {
        viewModel.oxygenCoinBillList(AccountFlowingReq(current = 1, size = REFRESH_SIZE))
    }

    companion object {
        const val REFRESH_SIZE = 10
    }
}