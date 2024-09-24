package com.cl.modules_home.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.STEP
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.STEP_NOW
import com.cl.modules_home.activity.ProModeEnvActivity.Companion.TEMPLATE_ID
import com.cl.modules_home.adapter.ProModeTaskListAdapter
import com.cl.modules_home.databinding.HomeProModeTaskActivityBinding
import com.cl.modules_home.request.DeleteTaskReq
import com.cl.modules_home.request.EnvSaveReq
import com.cl.modules_home.request.SaveTaskReq
import com.cl.modules_home.request.Task
import com.cl.modules_home.viewmodel.ProModeViewModel
import com.cl.modules_home.widget.CalendarPop
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class ProModeTaskActivity : BaseActivity<HomeProModeTaskActivityBinding>() {

    @Inject
    lateinit var viewModel: ProModeViewModel

    private val step by lazy {
        intent.getStringExtra(STEP)
    }

    private val templateId by lazy {
        intent.getStringExtra(TEMPLATE_ID)
    }

    // stepShow
    private val stepShow by lazy {
        intent.getStringExtra(STEP_NOW)
    }

    // 结束时间 Long 类型 没有x1000
    private val endTime by lazy {
        intent.getLongExtra(ProModeEnvActivity.END_TIME, 0L)
    }

    private val startTime by lazy {
        intent.getLongExtra(ProModeEnvActivity.START_TIME, 0L)
    }

    private val adapter by lazy {
        ProModeTaskListAdapter(mutableListOf())
    }

    // 0 or else , 日历、主页
    private var intentflag = 0

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.tvPeriod.text = "[${stepShow}]"
        binding.recyclerView.layoutManager = LinearLayoutManager(this@ProModeTaskActivity)
        binding.recyclerView.adapter = adapter
    }

    override fun observe() {
        viewModel.apply {
            taskList.observe(this@ProModeTaskActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    adapter.setList(data)
                }
            })

            deleteTask.observe(this@ProModeTaskActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                }
            })

            saveTask.observe(this@ProModeTaskActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 根据状态跳转到哪个地方。
                    when (intentflag) {
                        0 -> {
                            // 跳转到日历界面
                            ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).withString(Constants.Global.KEY_IS_TEMPLATE_ID, templateId).navigation(this@ProModeTaskActivity)
                        }

                        else -> {
                            // setUpdater\Use Official Calendar
                            // startRunning
                            startRunning("", false, templateId)
                        }
                    }

                }
            })


            startRunning.observe(this@ProModeTaskActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    checkPlant()
                }
            })

            checkPlant.observe(this@ProModeTaskActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    data?.let { PlantCheckHelp().plantStatusCheck(this@ProModeTaskActivity, it, true) }
                }
            })
        }
    }

    override fun initData() {
        // 点击事件
        adapter.addChildClickViewIds(R.id.iv_clear_code, R.id.tv_send_code)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.iv_clear_code -> {
                    // 删除
                    viewModel.deleteTask(DeleteTaskReq(taskId = (adapter.data[position] as? Task)?.taskId.toString(), templateId = templateId))
                    adapter.removeAt(position)
                }

                R.id.tv_send_code -> {
                    // 跳转任务设置界面
                    startSetActivity(adapter, position)
                }
            }
        }

        binding.tvRecommend.setSafeOnClickListener {
            xpopup(this@ProModeTaskActivity) {
                dismissOnTouchOutside(false)
                isDestroyOnDismiss(false)
                asCustom(
                    CalendarPop(
                        this@ProModeTaskActivity,
                        content = "By clicking 'Setup Later,' you will start your growing journey without a calendar. You can edit your growing calendar anytime later by clicking the calendar icon.",
                        onConfirmAction = {
                            intentflag = 1
                            // 确认。
                            // 跳转到日历界面，显示空白日历。
                            viewModel.saveTask(SaveTaskReq(step = step, taskContent = adapter.data, templateId = templateId, setupLater = true))
                        })
                ).show()
            }
        }

        binding.btnSuccess.setSafeOnClickListener {
            intentflag = 0
            // 显示自己配置的任务的日历
            // 保存多个任务，然后跳转到日历界面
            viewModel.saveTask(SaveTaskReq(step = step, taskContent = adapter.data, templateId = templateId, useOfficialCalendar = false))
        }
        binding.tvCalendar.setSafeOnClickListener {
            xpopup(this@ProModeTaskActivity) {
                dismissOnTouchOutside(false)
                isDestroyOnDismiss(false)
                asCustom(
                    CalendarPop(
                        this@ProModeTaskActivity,
                        content = "By clicking “Use official Calendar”, you will start your growing journey with hey abby recommended calendar. You can edit your growing calendar anytime later by clicking the calendar icon.",
                        onConfirmAction = {
                            intentflag = 1
                            // 确认。
                            // 使用官方日历
                            // 跳转到日历界面
                            viewModel.saveTask(SaveTaskReq(step = step, taskContent = adapter.data, templateId = templateId, useOfficialCalendar = true))
                        })
                ).show()
            }
        }
        binding.ivAdd.setSafeOnClickListener {
            // 跳转任务设置界面
            startSetActivity()
        }
    }

    private fun startSetActivity(adapter: BaseQuickAdapter<*, *>? = null, position: Int? = null) {
        startActivity(Intent(this@ProModeTaskActivity, ProModeTaskSetActivity::class.java).apply {
            putExtra(STEP_NOW, stepShow)
            putExtra(STEP, step)
            putExtra(TEMPLATE_ID, templateId)
            putExtra(ProModeEnvActivity.END_TIME, endTime)
            putExtra(ProModeEnvActivity.START_TIME, startTime)
            // 带参数就是编辑，不带参数就是添加。
            if (null != adapter && null != position) {
                putExtra(ProModeEnvActivity.TASK_DATA, (adapter.data[position] as? Serializable))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // 接口请求
        viewModel.getTaskList(EnvSaveReq(step = step, templateId = templateId))
    }
}