package com.cl.modules_home.ui

import android.content.Intent
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeBinding
import com.cl.modules_home.databinding.HomeFirstJoinFragmentBinding
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.bean.LiveDataDeviceInfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.adapter.FirstJoinAdapter
import com.cl.modules_home.request.GrowSpaceData
import com.cl.modules_home.viewmodel.HomeViewModel
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@Route(path = RouterPath.Home.PAGE_FIRST_JOIN)
@AndroidEntryPoint
class FirstJoinInFragment : BaseFragment<HomeFirstJoinFragmentBinding>() {


    @Inject
    lateinit var mViewMode: HomeViewModel

    private val adapter by lazy {
        FirstJoinAdapter(
            mutableListOf(
                GrowSpaceData(name = "Hey abby Grow Box", 1),
                GrowSpaceData(name = "Grow Tent", 2),
            )
        )
    }

    override fun HomeFirstJoinFragmentBinding.initBinding() {

    }

    override fun onResume() {
        super.onResume()
        mViewMode.userDetail()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            mViewMode.userDetail()
        }
    }

    // 上一个滚动的视图。
    var lastPosition = 0
    override fun initView(view: View) {
        // 隐藏返回按钮
        binding.title.setLeftVisible(false)

        binding.rvGrow.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val pagerSnapHelper = PagerSnapHelper()  // 创建PagerSnapHelper
            pagerSnapHelper.attachToRecyclerView(this)  // 附加到RecyclerView
            val totalWidth = resources.displayMetrics.widthPixels // 获取屏幕宽度
            val decoration = StartMarginItemDecoration(dp2px(20f), totalWidth, dp2px(235f))
            addItemDecoration(decoration)
            adapter = this@FirstJoinInFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // 停下来的了，并且不是正在不再绘制
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout) {
                        val centerView = pagerSnapHelper.findSnapView(layoutManager)
                        if (centerView != null) {
                            val centerPosition = layoutManager?.getPosition(centerView)
                            if (lastPosition == centerPosition) {
                                return
                            }
                            lastPosition = centerPosition ?: 0
                            // 等于0时，才显示立即购买按钮
                            ViewUtils.setVisible(centerPosition == 0, binding.tvBuyNow)
                        }
                    }
                }
            })
        }
    }

    override fun lazyLoad() {
        binding.tvAddNow.setOnClickListener {
            when (lastPosition) {
                0 -> {
                    // abby
                    activity?.let { it1 ->
                        PermissionHelp().checkConnectForTuYaBle(
                            it1,
                            object : PermissionHelp.OnCheckResultListener {
                                override fun onResult(result: Boolean) {
                                    if (!result) return
                                    // 如果权限都已经同意了
                                    ARouter.getInstance()
                                        .build(RouterPath.PairConnect.PAGE_PLANT_SCAN)
                                        .navigation()
                                }
                            })
                    }
                }

                1 -> {
                    //  这是跳转到添加帐篷
                    // 如果权限都已经同意了
                    ARouter.getInstance()
                        .build(RouterPath.My.PAGE_ADD_TENT)
                        .navigation()
                }
            }
        }
        binding.tvBuyNow.setOnClickListener {
            // abby的购买连接
            val intent = Intent(context, WebActivity::class.java)
            //intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/app-og-edition-grow-box")
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/app-store")
            intent.putExtra(WebActivity.KEY_IS_SHOW_CAR, true)
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Hey abby")
            context?.startActivity(intent)
        }


        /**
         * 设备管理界面、切换设备
         */
        LiveEventBus.get().with(Constants.Global.KEY_IS_SWITCH_DEVICE, LiveDataDeviceInfoBean::class.java)
            .observe(viewLifecycleOwner) { devieInfo ->
                if (null != devieInfo) {
                    logI("LiveDataDeviceInfoBean: ${devieInfo.deviceId},,, ${devieInfo.spaceType}")
                    // 切换设备如果有摄像头的话，都是隐藏，会占用内存,
                    // cameraStopOnpause()
                    // 切换之后需要重新刷新所有的东西
                    mViewMode.checkPlant()
                }
            }
    }

    override fun observe() {
        super.observe()
        mViewMode.apply {
            userDetail.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 保存当前的信息.
                    GSON.toJsonInBackground(data) {
                        logI("refreshToken: $it")
                        Prefs.putStringAsync(Constants.Login.KEY_LOGIN_DATA, it)
                    }
                    if (!data?.deviceId.isNullOrEmpty()) checkPlant()
                }
            })

            checkPlant.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 是否种植过
                    data?.let {
                        PlantCheckHelp().plantStatusCheck(
                            activity,
                            it,
                            true,
                            isLeftSwapAnim = mViewMode.isLeftSwap,
                            isNoAnim = false
                        )
                    }
                }
            })
        }
    }

    class StartMarginItemDecoration(private val marginStart: Int, private val totalWidth: Int, private val itemWidth: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val position = parent.getChildAdapterPosition(view)
            val itemCount = parent.adapter?.itemCount ?: 0
            val padding = (totalWidth - itemWidth) / 2

            if (position == 0) {
                // 为第一个项目添加左边距
                outRect.left = padding
                outRect.right = marginStart
            } else if (position == itemCount - 1) {
                // 为最后一个项目添加右边距
                outRect.right = padding
            }
        }
    }
}