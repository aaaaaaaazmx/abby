package com.cl.modules_contact.ui

import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.TrendListAdapter
import com.cl.modules_contact.databinding.FragmentContactBinding
import com.cl.modules_contact.pop.ContactEnvPop
import com.cl.modules_contact.pop.ContactPotionPop
import com.cl.modules_contact.pop.ContactReportPop
import com.cl.modules_contact.request.ContactEnvData
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.viewmodel.ContactViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.scwang.smart.refresh.footer.ClassicsFooter
import dagger.hilt.android.AndroidEntryPoint
import hilt_aggregated_deps._dagger_hilt_android_internal_managers_ViewComponentManager_ViewWithFragmentComponentBuilderEntryPoint
import java.util.Date
import javax.inject.Inject


/**
 * 联系人首页
 */
@Route(path = RouterPath.Contact.PAGE_CONTACT)
@AndroidEntryPoint
class ContactFragment : BaseFragment<FragmentContactBinding>() {
    @Inject
    lateinit var mViewMode: ContactViewModel

    // 朋友圈适配器
    private val adapter by lazy {
        TrendListAdapter(mutableListOf())
    }

    override fun initView(view: View) {
        // 数量的显示
        ViewUtils.setVisible(mViewMode.userinfoBean?.eventCount != 0, binding.vvMsgNumber)
        ViewUtils.setVisible(TextUtils.isEmpty(mViewMode.userinfoBean?.avatarPicture), binding.noheadShow)
        ViewUtils.setVisible(!TextUtils.isEmpty(mViewMode.userinfoBean?.avatarPicture), binding.ivAvatar)
        context?.let {
            Glide.with(it).load(mViewMode.userinfoBean?.avatarPicture)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivAvatar)
        }
        binding.vvMsgNumber.text = (mViewMode.userinfoBean?.eventCount ?: 0).toString()
        binding.noheadShow.text = mViewMode.userinfoBean?.nickName?.substring(0, 1)

        mViewMode.getNewPage(NewPageReq(current = 1, size = 10))
        binding.rvWxCircle.apply {
            layoutManager = LinearLayoutManager(activity)
            // 添加分割线
            //添加自定义分割线
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.custom_divider)!!)
            addItemDecoration(divider)
            adapter = this@ContactFragment.adapter
        }

        // refresh
        binding.refreshLayout.apply {
            ClassicsFooter.REFRESH_FOOTER_LOADING = "Updating" //"正在刷新...";
            ClassicsFooter.REFRESH_FOOTER_REFRESHING = "Updating" //"正在加载...";

            // 刷新监听
            setOnRefreshListener {
                // 重新加载数据
                logI("setOnRefreshListener: refresh")
                mViewMode.updateCurrent(1)
                mViewMode.getNewPage(NewPageReq(current = mViewMode.updateCurrent.value, size = REFRESH_SIZE))
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (mViewMode.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                mViewMode.updateCurrent(current)
                mViewMode.getNewPage(NewPageReq(current = current, size = REFRESH_SIZE))
            }
            // 刷新头部局
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
            // 刷新高度
            setHeaderHeight(60f)
            // 自动刷新
            // autoRefresh()
        }

        // adapter条目点击时间
        initAdapterClick()
    }

    /**
     * 条目点击事件
     */
    private fun initAdapterClick() {
        adapter.addChildClickViewIds(R.id.tv_link, R.id.cl_avatar, R.id.cl_env, R.id.cl_love, R.id.cl_gift, R.id.cl_chat, R.id.rl_point, R.id.tv_desc)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? NewPageData.Records
            mViewMode.updateCurrentPosition(position)
            when (view.id) {
                R.id.tv_link -> {
                    // 跳转网页
                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(WebActivity.KEY_WEB_URL, item?.link)
                    context?.startActivity(intent)
                }

                R.id.cl_avatar -> {
                    // todo 点击头像、跳转到自己的空间， 用userID来区别是跳转到自己的，还是别人的
                }

                R.id.cl_env -> {
                    // 点击环境信息
                    val envInfoData = GSON.parseObjectList(item?.environment, ContactEnvData::class.java).toMutableList()
                    // 弹出环境信息
                    XPopup.Builder(context).dismissOnTouchOutside(false).isDestroyOnDismiss(false).asCustom(context?.let { ContactEnvPop(it, envInfoData, item?.nickName, item?.avatarPicture) }).show()
                }

                R.id.cl_love -> {
                    // 点赞
                    if (item?.isPraise == 0) {
                        mViewMode.like(LikeReq(learnMoreId = item.learnMoreId, likeId = item.id.toString(), type = "moments"))
                    } else {
                        mViewMode.unlike(LikeReq(learnMoreId = item?.learnMoreId, likeId = item?.id.toString(), type = "moments"))
                    }
                }

                R.id.cl_gift -> {
                    // todo 打赏
                }

                R.id.cl_chat -> {
                    // todo 聊天
                }

                R.id.rl_point -> {
                    // todo 点击三个点
                    XPopup.Builder(context)
                        .popupPosition(PopupPosition.Left)
                        .dismissOnTouchOutside(true)
                        .isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                        .atView(view)
                        .isCenterHorizontal(false)
                        .asCustom(
                            context?.let {
                                ContactPotionPop(
                                    it,
                                    deleteAction = {
                                        //  删除
                                        mViewMode.delete(DeleteReq(momentId = item?.id.toString()))
                                    },
                                    reportAction = {
                                        // 举报弹窗
                                        XPopup.Builder(it)
                                            .isDestroyOnDismiss(false)
                                            .dismissOnTouchOutside(false)
                                            .asCustom(
                                                ContactReportPop(
                                                    it,
                                                    onConfirmAction = {txt ->
                                                        // 举报
                                                        mViewMode.report(ReportReq(momentId = item?.id.toString(), reportContent = txt))
                                                    })
                                            ).show()
                                    },
                                    itemSwitchAction = { isCheck ->
                                        // 关闭分享
                                        mViewMode.public(SyncTrendReq(syncTrend = if (isCheck) 1 else 0, momentId = item?.id.toString()))
                                    },
                                    isShowReport = item?.userId.toString() == mViewMode.userinfoBean?.userId
                                )
                                    .setBubbleBgColor(Color.WHITE) //气泡背景
                                    .setArrowWidth(XPopupUtils.dp2px(context, 3f))
                                    .setArrowHeight(
                                        XPopupUtils.dp2px(
                                            context,
                                            3f
                                        )
                                    )
                                    //.setBubbleRadius(100)
                                    .setArrowRadius(
                                        XPopupUtils.dp2px(
                                            context,
                                            2f
                                        )
                                    )
                            }
                        ).show()
                }

                R.id.tv_desc -> {
                    // todo 跳转到更多聊天记录弹窗
                }
            }
        }

        // floatbutton
        binding.flButton.setOnClickListener {
            // todo 跳转到发布动态页面
            ToastUtil.shortShow("FLAT")
        }

        // 头像点击
        binding.clAvatar.setOnClickListener {
            // todo 头像点击啊
        }

        // 消息点击
        binding.ivBells.setOnClickListener {
            // todo 消息点击啊
        }

    }

    override fun lazyLoad() {
    }

    override fun observe() {
        mViewMode.apply {
            // 获取动态
            newPageData.observe(viewLifecycleOwner, resourceObserver {
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
                        logI("123123: refresh")
                        binding.refreshLayout.finishRefresh()
                    }
                    if (binding.refreshLayout.isLoading) {
                        logI("123123: isLoading")

                        // 没有加载了、或者加载完毕
                        if ((data?.records?.size ?: 0) < REFRESH_SIZE) {
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
                            adapter.setList(it.records)
                        } else {
                            // 追加数据
                            it.records?.let { it1 -> adapter.addData(adapter.data.size, it1) }
                        }
                    }
                }
            })

            // 点赞
            likeData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success

                    val item = adapter.data[position] as? NewPageData.Records
                    item?.let {
                        it.isPraise = 1
                        it.praise = it.praise?.plus(1)
                        adapter.notifyItemChanged(position)
                    }
                }
            })
            // 取消点赞
            unlikeData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success

                    val item = adapter.data[position] as? NewPageData.Records
                    item?.let {
                        it.isPraise = 0
                        it.praise = it.praise?.minus(1)
                        adapter.notifyItemChanged(position)
                    }
                }
            })

            // 删除
            deleteData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })

            // 是否公开
            // 在这个界面都是公开的
            publicData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })

        }
    }


    override fun FragmentContactBinding.initBinding() {

    }

    override fun onResume() {
        super.onResume()
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { v, insets ->
            binding.clRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }


    companion object {
        const val REFRESH_SIZE = 10
    }
}