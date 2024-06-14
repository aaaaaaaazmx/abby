package com.cl.modules_my.ui

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.BaseBean
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.modules_my.adapter.NewWalletAdapter
import com.cl.modules_my.databinding.MyNewWalletActivityBinding
import com.cl.modules_my.viewmodel.WalletViewModel
import com.tuya.smart.android.demo.camera.utils.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 优惠券界面
 */
@AndroidEntryPoint
class NewWalletActivity: BaseActivity<MyNewWalletActivityBinding>() {
    @Inject
    lateinit var mViewModel: WalletViewModel

    private val adapter by lazy {
        NewWalletAdapter(mutableListOf())
    }

    override fun initView() {
        binding.ivBack.setSafeOnClickListener { finish() }
        binding.tvRightImg.setSafeOnClickListener {
            // 跳转氧气币明细界面
            startActivity(Intent(this@NewWalletActivity, OxygenListActivity::class.java))
        }

        binding.rvWallet.layoutManager = LinearLayoutManager(this@NewWalletActivity)
        binding.rvWallet.adapter = this@NewWalletActivity.adapter
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getVoucherList(BaseBean())
    }

    override fun observe() {
        mViewModel.apply {
            getVoucherList.observe(this@NewWalletActivity,  resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    com.cl.common_base.widget.toast.ToastUtil.shortShow(errorMsg) }
                success {
                    hideProgressLoading()
                    adapter.setList(data)
                }
                loading {
                    showProgressLoading()
                }
            })
        }
    }

    override fun initData() {
    }
}