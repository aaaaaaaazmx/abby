package com.cl.modules_home.ui

import android.content.Intent
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.cl.modules_home.adapter.TentSpaceSetUpAdapter
import com.cl.modules_home.databinding.HomeTentPlantSetBinding
import com.cl.modules_home.request.MultiPlantListData
import com.cl.modules_home.viewmodel.TentKitViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@Route(path = RouterPath.Home.PAGE_TENT_KIT_PLANT_SETUP)
@AndroidEntryPoint
class TentKitPlanSetActivity: BaseActivity<HomeTentPlantSetBinding>() {

    @Inject
    lateinit var MultiPlantListData: TentKitViewModel

    // 是什么类型的植物跳转过来的
    private val plantType by lazy {
        intent.getStringExtra(Constants.Global.KEY_PLANT_TYPE)
    }

    // 获取植物ID
    private val plantId by lazy {
        Prefs.getString(Constants.Global.KEY_PLANT_ID)
    }

    private val adapter by lazy {
        TentSpaceSetUpAdapter(mutableListOf())
    }

    override fun initView() {
        binding.rvPlantSet.layoutManager = LinearLayoutManager(this@TentKitPlanSetActivity)
        binding.rvPlantSet.adapter = adapter


    }

    override fun observe() {
        MultiPlantListData.apply {
            plantList.observe(this@TentKitPlanSetActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    adapter.setList(data)
                }
            })

            updateMultiPlant.observe(this@TentKitPlanSetActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()

                    val intent = Intent(this@TentKitPlanSetActivity, BasePopActivity::class.java)
                    when(plantType) {
                        Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE ->  {
                            // 跳转富文本之后返回首页
                            intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE_LABEL)
                            intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE_LABEL)
                        }
                        Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING ->  {
                            intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING_LABEL)
                            intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING_LABEL)
                        }

                        Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED ->  {
                            intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED_LABEL)
                            intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED_LABEL)
                        }

                        Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED ->  {
                            intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED_LABEL)
                            intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED_LABEL)
                        }
                    }
                    intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, true)
                    startActivity(intent)
                }
            })
        }
    }

    override fun initData() {
        MultiPlantListData.getPlantList(plantId)
        binding.btnSuccess.setSafeOnClickListener {
            if (adapter.data.isEmpty()) {
                return@setSafeOnClickListener
            }
            MultiPlantListData.updateMultiPlant(adapter.data)
        }

        binding.ivAdd.setSafeOnClickListener {
            if (adapter.data.size == 6) {
                return@setSafeOnClickListener
            }
            if (adapter.data.isEmpty()) {
                adapter.addData(MultiPlantListData())
                return@setSafeOnClickListener
            }
            val first = adapter.data.first()
            if (first.isSyncStrainCheck == true) {
                adapter.addData(MultiPlantListData(strainName = first.strainName))
            } else {
                adapter.addData(MultiPlantListData())
            }
        }

        adapter.addChildClickViewIds(R.id.iv_close, R.id.rl_sync_strain)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when(view.id) {
                R.id.iv_close -> {
                    if (adapter.data.size == 1) {
                        return@setOnItemChildClickListener
                    }
                    // 删除当前。
                    adapter.removeAt(position)
                }
                R.id.rl_sync_strain -> {
                    // Find the CheckBox for syncing strain names
                    val syncStrainCheckbox =
                        view.findViewById<CheckBox>(R.id.cb_sync)
                    // Toggle the checkbox state
                    val isCheckboxChecked = !syncStrainCheckbox.isChecked
                    syncStrainCheckbox.isChecked = isCheckboxChecked
                    // If the checkbox is checked, proceed with syncing
                    // Return early if the adapter data is empty
                    if (adapter.data.isEmpty()) return@setOnItemChildClickListener
                    // Safely cast and update the strain names if the checkbox is checked
                    (adapter.data as? MutableList<MultiPlantListData>)?.let { list ->
                        if (list.isEmpty()) return@setOnItemChildClickListener
                        list.forEachIndexed { index, MultiPlantListData ->
                            if (isCheckboxChecked) {
                                MultiPlantListData.strainName = list[0].strainName
                            }
                            MultiPlantListData.isSyncStrainCheck = isCheckboxChecked
                            // 这里不需要重新设置适配器数据，因为我们已经直接修改了原有列表中的元素
                            this@TentKitPlanSetActivity.adapter.notifyItemChanged(index)
                        }
                    }
                }
            }
        }

    }

}