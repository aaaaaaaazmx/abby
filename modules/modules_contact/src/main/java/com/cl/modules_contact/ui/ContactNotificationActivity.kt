package com.cl.modules_contact.ui

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ContactNotificationAdapter
import com.cl.modules_contact.databinding.ContactNotificationBinding
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.response.MessageListData
import com.cl.modules_contact.viewmodel.ContactNotificationViewModel
import com.scwang.smart.refresh.footer.ClassicsFooter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 通知页面
 */
@AndroidEntryPoint
class ContactNotificationActivity : BaseActivity<ContactNotificationBinding>() {
    @Inject
    lateinit var mViewMode: ContactNotificationViewModel

    private val notificationAdapter by lazy {
        ContactNotificationAdapter(mutableListOf())
    }

    override fun onResume() {
        super.onResume()
        mViewMode.messageList(NewPageReq(current = 1, size = REFRESH_SIZE))
    }

    override fun initView() {

        binding.title.setLeftClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.rvNotification.apply {
            layoutManager = LinearLayoutManager(this@ContactNotificationActivity)
            //添加自定义分割线
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context, com.cl.common_base.R.drawable.custom_divider)!!)
            addItemDecoration(divider)
            adapter = notificationAdapter
        }

        // refresh
        binding.refreshLayout.apply {
            ClassicsFooter.REFRESH_FOOTER_LOADING = getString(com.cl.common_base.R.string.string_255) //"正在刷新...";
            ClassicsFooter.REFRESH_FOOTER_REFRESHING = getString(com.cl.common_base.R.string.string_255) //"正在加载...";
            ClassicsFooter.REFRESH_FOOTER_NOTHING = getString(com.cl.common_base.R.string.string_256)
            ClassicsFooter.REFRESH_FOOTER_FINISH = getString(com.cl.common_base.R.string.string_257)
            ClassicsFooter.REFRESH_FOOTER_FAILED = getString(com.cl.common_base.R.string.string_258)

            // 刷新监听
            setOnRefreshListener {
                // 重新加载数据
                logI("setOnRefreshListener: refresh")
                mViewMode.updateCurrent(1)
                mViewMode.messageList(NewPageReq(current = 1, size = REFRESH_SIZE))
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (mViewMode.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                mViewMode.updateCurrent(current)
                mViewMode.messageList(NewPageReq(current = current, size = REFRESH_SIZE))
            }
            // 刷新头部局
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
            // 刷新高度
            setHeaderHeight(60f)
            // 自动刷新
            // autoRefresh()
        }

        initAdapterClick()
    }

    private fun initAdapterClick() {
        notificationAdapter.addChildClickViewIds(R.id.cl_root)
        notificationAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? MessageListData
            when (view.id) {
                R.id.cl_root -> {
                    // 跳转到动态详情界面
                    val intent = Intent(this@ContactNotificationActivity, ContactCommentActivity::class.java)
                    intent.putExtra(ContactCommentActivity.KEY_MOMENT_ID, item?.momentId)
                    intent.putExtra(ContactCommentActivity.KEY_LEARN_MORE_ID, item?.learnMoreId)
                    startActivity(intent)
                }
            }
        }

    }

    override fun observe() {
        mViewMode.apply {
            messageListData.observe(this@ContactNotificationActivity, resourceObserver {
                error { errorMsg, toastuti ->
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
                        if ((data?.size ?: 0) <= 0) {
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            binding.refreshLayout.finishLoadMore()
                        }
                    }
                    if (null == this.data) return@success

                    // 数据相关
                    data?.let {
                        val current = mViewMode.updateCurrent.value
                        if (current == 1) {
                            // 刷新数据
                            notificationAdapter.setList(it)
                        } else {
                            // 追加数据
                            it.let { it1 -> notificationAdapter.addData(notificationAdapter.data.size, it1) }
                        }
                    }

                }
            })
        }
    }

    override fun initData() {
    }

    companion object {
        const val REFRESH_SIZE = 20
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

}