package com.cl.modules_home.activity

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseStringPickPop
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.decoraion.FullyGridLayoutManager
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.cl.modules_home.adapter.ProModePeriodChooserAdapter
import com.cl.modules_home.databinding.HomeProModeStartActivityBinding
import com.cl.modules_home.request.CycleListBean
import com.cl.modules_home.request.PeriodListBody
import com.cl.modules_home.request.PeriodListSaveReq
import com.cl.modules_home.viewmodel.HomeViewModel
import com.cl.modules_home.viewmodel.ProModeViewModel
import com.luck.picture.lib.utils.DensityUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// ProMode周期选择界面
@AndroidEntryPoint
class ProModeStartActivity : BaseActivity<HomeProModeStartActivityBinding>() {

    @Inject
    lateinit var mViewMode: ProModeViewModel

    // 当前接受的step，用来区分从首页进来具体当前是什么周期
    private val step by lazy {
        intent.getStringExtra(STEP)
    }

    // 接受templateId
    private val templateId by lazy {
        intent.getStringExtra(TEMPLATE_ID)
    }

    private val adapter by lazy {
        ProModePeriodChooserAdapter(mutableListOf())
    }

    private val weeksList by lazy {
        mutableListOf(
            "1 Week", "2 Week", "3 Week", "4 Week", "5 Week", "6 Week", "7 Week", "8 Week", "9 Week", "10 Week", "11 Week", "12 Week",
            "13 Week", "14 Week", "15 Week", "16 Week", "17 Week", "18 Week", "19 Week", "20 Week"
        )
    }

    private val daysList by lazy {
        mutableListOf("0 Day", "1 Day", "2 Day", "3 Day", "4 Day", "5 Day", "6 Day")
    }

    override fun initView() {
        binding.recyclerView.apply {
            layoutManager = FullyGridLayoutManager(
                this@ProModeStartActivity,
                3, GridLayoutManager.VERTICAL, false
            )
            addItemDecoration(
                GridSpaceItemDecoration(
                    3,
                    DensityUtil.dip2px(this@ProModeStartActivity, 6f), DensityUtil.dip2px(this@ProModeStartActivity, 6f)
                )
            )
            adapter = this@ProModeStartActivity.adapter
        }
    }

    override fun observe() {
        mViewMode.apply {
            // 获取周期列表接口
            cycleList.observe(this@ProModeStartActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()

                    // 检查数据是否为空
                    val data = data ?: return@success

                    // 判断是否是第一次进来
                    val selectedBean = if (step == null) {
                        data.firstOrNull()?.apply { isSelect = true } // 默认选择第一个
                    } else {
                        data.find { it.step == step }?.apply { isSelect = true } // 从外面跳转过来进行比对
                    }

                    // 更新UI和适配器
                    selectedBean?.let {
                        binding.tvTotalDay.text = getString(R.string.home_total_days_variable, it.stepDay.toString())
                    }
                    // 然后转化成week和Day
                    // 这个是传换成week,比如10/7=1,10%7=3,所以是1week3day
                    val week = selectedBean?.stepDay?.div(7)
                    val day = selectedBean?.stepDay?.rem(7)
                    binding.tvStart.text = getString(R.string.home_weekss, week.toString())
                    binding.tvDay.text = getString(R.string.home_dayss, day.toString())

                    adapter.setList(data)
                }
            })


            // 保存or修改周期
            updateCycle.observe(this@ProModeStartActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 跳转到ProModeEnv设置界面
                    startActivity(Intent(this@ProModeStartActivity, ProModeEnvActivity::class.java).apply {
                        adapter.data.firstOrNull { it.isSelect }?.apply {
                            putExtra(ProModeEnvActivity.STEP_NOW, stepShow)
                            putExtra(ProModeEnvActivity.STEP, step)
                            putExtra(ProModeEnvActivity.TEMPLATE_ID, templateId)
                        }
                    })
                }
            })
        }
    }

    override fun initData() {
        // 请求获取周期列表接口
        mViewMode.getCycleList(PeriodListBody(templateId = templateId))

        adapter.addChildClickViewIds(R.id.container)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.container -> {
                    // 需要把当前的isSelect改为false，然后把当前的改为true
                    (adapter.data as? MutableList<CycleListBean>)?.indexOfFirst { it.isSelect }?.let { index ->
                        if (index != -1) {
                            // 如果2个是一样的，就不需要变。
                            if (index == position) return@setOnItemChildClickListener
                            (adapter.data[index] as? CycleListBean)?.isSelect = false
                            adapter.notifyItemChanged(index)
                        }
                    }
                    val item = adapter.data[position] as? CycleListBean
                    item?.isSelect = !(item?.isSelect ?: false)
                    adapter.notifyItemChanged(position)
                    // 也需要获取当前的总天数
                    binding.tvTotalDay.text = getString(R.string.home_total_days_variable, item?.stepDay.toString())
                    // 然后转化成week和Day
                    // 这个是传换成week,比如10/7=1,10%7=3,所以是1week3day
                    val week = item?.stepDay?.div(7)
                    val day = item?.stepDay?.rem(7)
                    binding.tvStart.text = getString(R.string.home_weekss, week.toString())
                    binding.tvDay.text = getString(R.string.home_dayss, day.toString())
                }
            }
        }


        binding.tvStart.setSafeOnClickListener {
            xpopup(this@ProModeStartActivity) {
                dismissOnTouchOutside(true)
                isDestroyOnDismiss(false)
                // 从adapter里面找到当前的week 和 day
                adapter.data.firstOrNull { it.isSelect }?.apply {
                    var week = stepDay.div(7)
                    val day = stepDay.rem(7)
                    asCustom(BaseStringPickPop(this@ProModeStartActivity, title = "Week", selectIndex = week.minus(1), listString = weeksList.toMutableList(), confirmAction = {
                        week = it.plus(1)
                        binding.tvStart.text = getString(R.string.home_weekss, week.toString())
                        // 如果更换了周，那么还需要计算总天数，week * 7 + day
                        binding.tvTotalDay.text = getString(R.string.home_total_days_variable, (week.times(7).plus(day)).toString())

                        // 也需要同步修改adapter里面的总天数。
                        stepDay = week.times(7).plus(day)
                    })).show()
                }


            }
        }

        binding.tvDay.setSafeOnClickListener {
            xpopup(this@ProModeStartActivity) {
                dismissOnTouchOutside(true)
                isDestroyOnDismiss(false)
                adapter.data.firstOrNull { it.isSelect }?.apply {
                    val week = stepDay.div(7)
                    var day = stepDay.rem(7)
                    asCustom(BaseStringPickPop(this@ProModeStartActivity, title = "Day", selectIndex = day, listString = daysList.toMutableList(), confirmAction = {
                        day = it
                        binding.tvDay.text = getString(R.string.home_dayss, it.toString())
                        // 如果更换了周，那么还需要计算总天数，week * 7 + day
                        binding.tvTotalDay.text = getString(R.string.home_total_days_variable, (week.times(7).plus(day)).toString())

                        // 也需要同步修改adapter里面的总天数。
                        stepDay = week.times(7).plus(day)
                    })).show()
                }
            }
        }

        binding.btnSuccess.setSafeOnClickListener {
            // 保存和修改周期
            mViewMode.updateCycle(PeriodListSaveReq(templateId = templateId.toString(), list = adapter.data.map {
                com.cl.modules_home.request.Req(
                    periodId = it.periodId,
                    step = it.step,
                    stepDay = it.stepDay,
                    stepShow = it.stepShow,
                )
            }.toMutableList()))
        }
    }

    companion object {
        // templateId
        const val TEMPLATE_ID = "templateId"

        // STEP
        const val STEP = "step"
    }
}