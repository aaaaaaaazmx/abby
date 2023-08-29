package com.cl.modules_my.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.common_base.adapter.OxygenCoinBillAdapter
import com.cl.modules_my.databinding.MyOxyGenActivityBinding
import com.cl.common_base.bean.AccountFlowingReq
import com.cl.common_base.intercome.InterComeHelp
import com.cl.modules_my.viewmodel.OxyGenListViewModel
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.footer.ClassicsFooter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 氧气币流水账单界面
 */
@AndroidEntryPoint
class OxygenListActivity : BaseActivity<MyOxyGenActivityBinding>() {

    @Inject
    lateinit var viewModel: OxyGenListViewModel

    private val adapter by lazy {
        OxygenCoinBillAdapter(mutableListOf())
    }

    @SuppressLint("InflateParams")
    override fun initView() {
        binding.title.setRightButtonImg(com.cl.common_base.R.mipmap.mt_gth)
            .setRightClickListener {
                // 跳转氧气币
                InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_OXYGEN_COIN)
            }

        // refresh
        binding.refreshLayout.apply {
            ClassicsFooter.REFRESH_FOOTER_LOADING = "Updating" //"正在刷新...";
            ClassicsFooter.REFRESH_FOOTER_REFRESHING = "Updating" //"正在加载...";
            ClassicsFooter.REFRESH_FOOTER_NOTHING = "No more data"
            ClassicsFooter.REFRESH_FOOTER_FINISH = "Loading completed"
            ClassicsFooter.REFRESH_FOOTER_FAILED = "Loading failed"

            // 刷新监听
            setOnRefreshListener {
                // 重新加载数据
                logI("setOnRefreshListener: refresh")
                viewModel.updateCurrent(1)
                viewModel.oxygenCoinBillList(AccountFlowingReq(current = viewModel.updateCurrent.value, size = REFRESH_SIZE))
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (viewModel.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                viewModel.updateCurrent(current)
                viewModel.oxygenCoinBillList(AccountFlowingReq(current = viewModel.updateCurrent.value, size = REFRESH_SIZE))
            }
            // 刷新头部局
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
            // 刷新高度
            setHeaderHeight(60f)
            // 自动刷新
            // autoRefresh()
        }


        binding.rvWxCircle.apply {
            layoutManager = LinearLayoutManager(this@OxygenListActivity)
            adapter = this@OxygenListActivity.adapter
            val view = LayoutInflater.from(this@OxygenListActivity).inflate(com.cl.common_base.R.layout.base_empty_view, null, false)
            view.findViewById<ImageView>(com.cl.common_base.R.id.iv_empty).apply {
                setBackgroundResource(com.cl.common_base.R.mipmap.base_no_oxygen)
            }
            this@OxygenListActivity.adapter.setEmptyView(view)
        }
    }

    override fun observe() {
        viewModel.apply {
            oxygenCoinBillList.observe(this@OxygenListActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.finishRefresh()
                    }
                    if (binding.refreshLayout.isLoading) {
                        binding.refreshLayout.finishLoadMore()
                    }
                }
                success {
                    // 刷新相关
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.finishRefresh()
                    }
                    if (binding.refreshLayout.isLoading) {

                        // 没有加载了、或者加载完毕
                        if ((data?.flowing?.size ?: 0) <= 0) {
                            binding.refreshLayout.finishLoadMore()
                        } else {
                            binding.refreshLayout.finishLoadMore()
                        }
                    }
                    if (null == this.data) return@success


                    // 数据相关
                    data?.let {
                        val current = viewModel.updateCurrent.value
                        if (current == 1) {
                            // 刷新数据
                            adapter.setList(it.flowing)

                            binding.tvTotal.text = it.total.toString()
                            binding.tvYesTotal.text = it.yestodayIncome.let {
                                buildSpannedString {
                                    bold {
                                        color(Color.parseColor("#B22234")) {
                                            append("$it ")
                                        }
                                    }
                                }
                            }
                        } else {
                            // 追加数据
                            it.flowing.let { it1 -> adapter.addData(adapter.data.size, it1) }
                        }
                    }
                }
            })
        }
    }

    override fun initData() {
        viewModel.oxygenCoinBillList(AccountFlowingReq(current = 1, size = REFRESH_SIZE))
    }

    override fun MyOxyGenActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@OxygenListActivity
            viewModel = this@OxygenListActivity.viewModel
            executePendingBindings()
        }
    }

    companion object {
        const val REFRESH_SIZE = 10
    }
}