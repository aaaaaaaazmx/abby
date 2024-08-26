package com.cl.modules_planting_log.fragment

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.adapter.PlantChooserPeriodAdapter
import com.cl.modules_planting_log.adapter.PlantLogListAdapter
import com.cl.modules_planting_log.databinding.PlantingMainFragmentBinding
import com.cl.modules_planting_log.request.CardInfo
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.PeriodVo
import com.cl.modules_planting_log.request.PlantLogTypeBean
import com.cl.modules_planting_log.ui.PlantActionActivity
import com.cl.modules_planting_log.ui.PlantingLogActivity
import com.cl.modules_planting_log.ui.PlantingTrainActivity
import com.cl.modules_planting_log.viewmodel.PlantingLogViewModel
import com.cl.modules_planting_log.widget.PlantChooseLogTypePop
import com.cl.common_base.pop.PlantIdListPop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.scwang.smart.refresh.footer.ClassicsFooter
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import javax.inject.Inject

/**
 * 种植日志主页
 */
@Route(path = RouterPath.Plant.PAGE_PLANT)
@AndroidEntryPoint
class PlantingLogFragment : BaseFragment<PlantingMainFragmentBinding>() {

    @Inject
    lateinit var viewModel: PlantingLogViewModel

    override fun PlantingMainFragmentBinding.initBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@PlantingLogFragment.viewModel
            executePendingBindings()
        }
    }

    override fun initView(view: View) {
        initRefresh()
        initNetData()
        clickView()
    }

    private fun initRefresh() {
        binding.refreshLayout.apply {
            ClassicsFooter.REFRESH_FOOTER_LOADING = context?.getString(com.cl.common_base.R.string.string_255) //"正在刷新...";
            ClassicsFooter.REFRESH_FOOTER_REFRESHING = context?.getString(com.cl.common_base.R.string.string_255) //"正在加载...";
            ClassicsFooter.REFRESH_FOOTER_NOTHING = context?.getString(com.cl.common_base.R.string.string_256)
            ClassicsFooter.REFRESH_FOOTER_FINISH = context?.getString(com.cl.common_base.R.string.string_257)
            ClassicsFooter.REFRESH_FOOTER_FAILED = context?.getString(com.cl.common_base.R.string.string_258)

            // 刷新监听
            setOnRefreshListener {
                // 重新加载数据
                logI("setOnRefreshListener: refresh")
                viewModel.updateCurrent(1)
                viewModel.getLogList(LogListReq(current = viewModel.updateCurrent.value, size = PAGE_SIZE, period = viewModel.period.value, plantId = viewModel.plantId.value?.toIntOrNull()))
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (viewModel.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                viewModel.updateCurrent(current)
                viewModel.getLogList(LogListReq(current = current, size = PAGE_SIZE, period = viewModel.period.value, plantId = viewModel.plantId.value?.toIntOrNull()))
            }
            // 刷新头部局
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
            // 禁止下拉刷新
            setEnableRefresh(false)
            // 刷新高度
            setHeaderHeight(60f)
            // 自动刷新
            // autoRefresh()
        }
    }

    private fun initNetData() {
        kotlin.runCatching {
            // 加载plantID
            val plantId = viewModel.plantId.value
            val period = viewModel.period.value
            if (!plantId.isNullOrEmpty() && !period.isNullOrEmpty()) {
                // onResume 刷新当前卡片列表
                viewModel.getPlantInfoByPlantId(plantId = plantId.toIntOrNull() ?: 0)
                viewModel.updateCurrent(1)
                viewModel.getLogList(LogListReq(1, period = period.toString(), plantId = plantId.toIntOrNull() ?: 0, PAGE_SIZE))
                return
            }

            // 第一次进来加载的
            if (plantId.isNullOrEmpty()) {
                // 如果plantId为空，则直接请求plant信息
                viewModel.plantInfo()
            } else {
                // 尝试将plantId转换为整数，并根据结果进行处理
                val plantIdInt = plantId.toIntOrNull()
                if (plantIdInt != null) {
                    viewModel.getPlantInfoByPlantId(plantId = plantIdInt)
                } else {
                    // 如果plantId不是有效的整数，则可以记录错误并采取其他措施
                    logE("Invalid plantId: $plantId")
                    // 可选的错误处理，例如默认操作或者用户通知
                    viewModel.plantInfo()
                }
            }
        }
    }


    private fun clickView() {
        binding.flGetPlantList.setOnClickListener {
            // 获取到所有的植物ID
            viewModel.getPlantIdByDeviceId(viewModel.userinfoBean()?.deviceId ?: "")
        }

        binding.ivAddDevice.setSafeOnClickListener(viewLifecycleOwner.lifecycleScope){
            if (null == viewModel.getPlantInfoByPlantId.value?.data) {
                ToastUtil.shortShow("no plants")
                return@setSafeOnClickListener
            }
            XPopup.Builder(context).popupPosition(PopupPosition.Top).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                .hasShadowBg(true) // 去掉半透明背景
                //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                .atView(binding.ivAddDevice).isCenterHorizontal(false).asCustom(context?.let {
                    PlantChooseLogTypePop(it,
                        list = mutableListOf(
                            PlantLogTypeBean("Log", false),
                            PlantLogTypeBean("Actions", false),
                            PlantLogTypeBean("Training", false)
                        ), onConfirmAction = { txt ->
                            // 跳转到相对应的界面
                            when (txt) {
                                "Log" -> {
                                    logI("click Log")
                                    context?.startActivity(Intent(context, PlantingLogActivity::class.java).apply {
                                        putExtra("isAdd", true)
                                        putExtra("plantId", viewModel.plantId.value)
                                        putExtra("period", viewModel.period.value)
                                        putExtra("plantInfoData", (viewModel.getPlantInfoByPlantId.value?.data as? Serializable))
                                    })
                                }

                                "Actions" -> {
                                    logI("click Actions")
                                    context?.startActivity(Intent(context, PlantActionActivity::class.java).apply {
                                        putExtra("isAdd", true)
                                        putExtra("plantId", viewModel.plantId.value)
                                        putExtra("period", viewModel.period.value)
                                        putExtra("plantInfoData", (viewModel.getPlantInfoByPlantId.value?.data as? Serializable))
                                    })
                                }

                                "Training" -> {
                                    logI("click Training")
                                    context?.startActivity(Intent(context, PlantingTrainActivity::class.java).apply {
                                        putExtra("isAdd", true)
                                        putExtra("plantId", viewModel.plantId.value)
                                        putExtra("period", viewModel.period.value)
                                        putExtra("plantInfoData", (viewModel.getPlantInfoByPlantId.value?.data as? Serializable))
                                    })
                                }
                            }
                        }).setBubbleBgColor(Color.WHITE) //气泡背景
                        .setArrowWidth(XPopupUtils.dp2px(context, 6f)).setArrowHeight(
                            XPopupUtils.dp2px(
                                context, 6f
                            )
                        ) //.setBubbleRadius(100)
                        .setArrowRadius(
                            XPopupUtils.dp2px(
                                context, 3f
                            )
                        )
                }).show()
        }

        // 周期适配器点击事件
        chooserPeriodAdapter.addChildClickViewIds(R.id.cl_root)
        chooserPeriodAdapter.setOnItemChildClickListener { adapter, view, position ->
            val data = (adapter.data[position] as? PeriodVo)
            when (view.id) {
                R.id.cl_root -> {
                    //  判断什么是可以点击的。
                    if (data?.optional == true) {
                        // 选择周期之后，就需要刷新日志列表
                        val chooserPeriod = data.period
                        viewModel.setPeriod(chooserPeriod)

                        // 1、更新isSelect周期选择
                        // Update the first selected item to unselected
                        updateAndNotify({ it.isSelect }, false)
                        // Update the item matching chooserPeriod and optional to selected
                        updateAndNotify({ chooserPeriod == it.period && it.optional }, true)

                        // 2、根据返回的当前周期选择需要展示的log列表
                        viewModel.updateCurrent(1)
                        viewModel.getLogList(LogListReq(1, period = chooserPeriod, plantId = (viewModel.plantId.value?.toIntOrNull() ?: 0), PAGE_SIZE))
                    }
                }
            }
        }
    }

    override fun lazyLoad() {
        initializeRv()
    }

    private val chooserPeriodAdapter by lazy {
        PlantChooserPeriodAdapter(mutableListOf())
    }

    private val logListAdapter by lazy {
        PlantLogListAdapter(mutableListOf(),
            onDeleteInterComeCard = {
                // 删除interCome卡片
                viewModel.plantId.value?.let { it1 -> viewModel.closeTips(period = it, plantId = it1) }
            },
            onEditCard = { period, logId, showType ->
                // 通过日志Id获取日志详情
                context?.apply {
                    when (showType) {
                        CardInfo.TYPE_LOG_CARD -> {
                            // 跳转到日志详情界面
                            startActivity(Intent(this, PlantingLogActivity::class.java).apply {
                                putExtra("isAdd", false)
                                putExtra("period", period)
                                putExtra("plantId", viewModel.plantId.value)
                                putExtra("logId", logId)
                                putExtra("plantInfoData", (viewModel.getPlantInfoByPlantId.value?.data as? Serializable))
                            })
                        }

                        CardInfo.TYPE_ACTION_CARD -> {
                            startActivity(Intent(this, PlantActionActivity::class.java).apply {
                                putExtra("isAdd", false)
                                putExtra("period", period)
                                putExtra("plantId", viewModel.plantId.value)
                                putExtra("logId", logId)
                                putExtra("showType", showType)
                                putExtra("plantInfoData", (viewModel.getPlantInfoByPlantId.value?.data as? Serializable))
                            })
                        }

                        CardInfo.TYPE_TRAINING_CARD -> {
                            startActivity(Intent(this, PlantingTrainActivity::class.java).apply {
                                putExtra("isAdd", false)
                                putExtra("period", period)
                                putExtra("plantId", viewModel.plantId.value)
                                putExtra("logId", logId)
                                putExtra("showType", showType)
                                putExtra("plantInfoData", (viewModel.getPlantInfoByPlantId.value?.data as? Serializable))
                            })
                        }
                    }
                }
            }
        )
    }

    /*private val logAdapter by lazy {
        PlantLogAdapter(mutableListOf())
    }*/

    private fun initializeRv() {
        // 横向周期选择
        binding.rvPeriod.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPeriod.adapter = chooserPeriodAdapter
        // 竖排log日志展示
        binding.rvLog.layoutManager = LinearLayoutManager(context)
        binding.rvLog.adapter = logListAdapter
    }

    override fun observe() {
        viewModel.apply {
            closeTips.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {}
            })

            plantInfo.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (null == data) return@success
                    // 设置plantId，然后请求当前的数据
                    setPlantIds(data?.plantId.toString())
                    data?.plantId?.let { getPlantInfoByPlantId(plantId = it) }
                }
            })


            getPlantInfoByPlantId.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (null == data) return@success
                    setPlantIds(data?.plantId.toString())
                    // 设置周期
                    setPeriod(data?.period.toString())

                    // 1、设置周期的Rv适配器数据
                    val periodVoList = data?.periodVoList
                    // 找到相同周期，然后设置选中属性
                    periodVoList?.find { data?.period == it.period }?.isSelect = true
                    chooserPeriodAdapter.setList(periodVoList)
                   /* runCatching {
                        periodVoList?.indexOfFirst { it.isSelect }?.let { if (it != -1) binding.rvPeriod.smoothScrollToPosition(it) }
                    }*/

                    // 2、根据返回的当前周期选择需要展示的log列表
                    updateCurrent(1)
                    getLogList(LogListReq(1, period = data?.period.toString(), plantId = data?.plantId ?: 0, PAGE_SIZE))
                }
            })


            getLogList.observe(viewLifecycleOwner, resourceObserver {
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
                        if ((data?.size ?: 0) <= 0) {
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            binding.refreshLayout.finishLoadMore()
                        }
                    }
                    if (data.isNullOrEmpty()) {
                        if (viewModel.updateCurrent.value == 1) {
                            // 设置空数据
                            logListAdapter.setList(mutableListOf())
                            return@success
                        }
                    }
                    // 数据相关
                    data?.let {
                        val current = viewModel.updateCurrent.value
                        if (current == 1) {
                            // 刷新数据
                            logListAdapter.setList(it)
                        } else {
                            // 追加数据
                            it.let { it1 -> logListAdapter.addData(logListAdapter.data.size, it1) }
                        }
                    }
                }
            })

            getPlantIdByDeviceId.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 获取3所有的植物ID
                    if (data.isNullOrEmpty()) return@success

                    XPopup.Builder(context).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        .offsetX(XPopupUtils.dp2px(context, -5f))
                        .offsetY(XPopupUtils.dp2px(context, 10f))
                        .atView(binding.ivGetPlantList).asCustom(context?.let {
                            PlantIdListPop(it, plantId.value?.safeToInt(), data, onConfirmAction = { plantId ->
                                // 设置植物ID
                                setPlantIds(plantId)
                                // 根据plantId获取植物信息
                                getPlantInfoByPlantId(plantId = plantId.toIntOrNull() ?: 0)
                            }).setBubbleBgColor(Color.WHITE) //气泡背景
                                .setArrowWidth(XPopupUtils.dp2px(context, 6f)).setArrowHeight(
                                    XPopupUtils.dp2px(
                                        context, 6f
                                    )
                                ) //.setBubbleRadius(100)
                                .setArrowRadius(
                                    XPopupUtils.dp2px(
                                        context, 1f
                                    )
                                )
                        }).show()
                }
            })
        }
    }

    /**
     * 更新并且通知
     */
    private fun updateAndNotify(itemCondition: (PeriodVo) -> Boolean, newValue: Boolean) {
        (chooserPeriodAdapter.data as? MutableList<PeriodVo>)?.find(itemCondition)?.let {
            it.isSelect = newValue
            (chooserPeriodAdapter.data as? MutableList<PeriodVo>)?.indexOf(it)?.apply { chooserPeriodAdapter.notifyItemChanged(this) }
        }
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

        initNetData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            initNetData()
        }
    }


    companion object {
        const val PAGE_SIZE = 20
    }
}