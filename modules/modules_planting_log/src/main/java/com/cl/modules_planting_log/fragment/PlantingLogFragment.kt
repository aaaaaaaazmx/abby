package com.cl.modules_planting_log.fragment

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.adapter.PlantChooserPeriodAdapter
import com.cl.modules_planting_log.adapter.PlantLogAdapter
import com.cl.modules_planting_log.databinding.PlantingMainFragmentBinding
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.PeriodVo
import com.cl.modules_planting_log.ui.PlantingLogActivity
import com.cl.modules_planting_log.viewmodel.PlantingLogViewModel
import com.cl.modules_planting_log.widget.PlantChooseLogTypePop
import com.cl.modules_planting_log.widget.PlantIdListPop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import dagger.hilt.android.AndroidEntryPoint
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
            binding.viewModel = this@PlantingLogFragment.viewModel
            executePendingBindings()
        }
    }

    override fun initView(view: View) {
        initNetData()
        clickView()
    }

    private fun initNetData() {
        // 加载plantID
        val plantId = viewModel.plantId.value
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


    private fun clickView() {
        binding.ivGetPlantList.setOnClickListener {
            // 获取到所有的植物ID
            viewModel.getPlantIdByDeviceId(viewModel.thingDeviceBean()?.devId ?: "")
        }

        binding.ivAddLog.setOnClickListener {
            XPopup.Builder(context).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                .hasShadowBg(true) // 去掉半透明背景
                //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                .atView(binding.ivAddLog).isCenterHorizontal(false).asCustom(context?.let {
                    PlantChooseLogTypePop(it, onConfirmAction = { txt ->
                        // todo 跳转到相对应的界面
                        when (txt) {
                            "Log" -> {
                                logI("click Log")
                                startActivity(Intent(context, PlantingLogActivity::class.java).apply {
                                    putExtra("plantId", viewModel.plantId.value)
                                })
                            }

                            "Actions" -> {
                                logI("click Actions")
                            }

                            "Training" -> {
                                logI("click Training")
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

    private val logAdapter by lazy {
        PlantLogAdapter(mutableListOf())
    }

    private fun initializeRv() {
        // 横向周期选择
        binding.rvPeriod.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPeriod.adapter = chooserPeriodAdapter
        // 竖排log日志展示
        binding.rvLog.layoutManager = LinearLayoutManager(context)
        binding.rvLog.adapter = logAdapter
    }

    override fun observe() {
        viewModel.apply {
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

                    // 1、设置周期的Rv适配器数据
                    val periodVoList = data?.periodVoList
                    // 找到相同周期，然后设置选中属性
                    periodVoList?.find { data?.period == it.period }?.isSelect = true
                    chooserPeriodAdapter.setList(periodVoList)

                    // 2、根据返回的当前周期选择需要展示的log列表
                    updateCurrent(1)
                    getLogList(LogListReq(1, period = data?.period.toString(), plantId = data?.plantId ?: 0, PAGE_SIZE))
                }
            })


            getLogList.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (data.isNullOrEmpty()) return@success
                    // todo 获取日志列表\添加加载更多
                }
            })

            getPlantIdByDeviceId.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 获取3所有的植物ID
                    if (data.isNullOrEmpty()) return@success

                    XPopup.Builder(context).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        /*.offsetX(XPopupUtils.dp2px(context, 5f))*/
                        .atView(binding.ivGetPlantList).isCenterHorizontal(false).asCustom(context?.let {
                            PlantIdListPop(it, plantId.value?.toInt(), data, onConfirmAction = { plantId ->
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
                                        context, 3f
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
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}