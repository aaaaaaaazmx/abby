package com.cl.modules_home.activity

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.equalsIgnoreCase
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.databinding.HomeGrowModeActivityBinding
import com.cl.modules_home.request.PeriodListBody
import com.cl.modules_home.viewmodel.ProModeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 植物种植模式选择
 */
@Route(path = RouterPath.Home.PAGE_GROW_MODE)
@AndroidEntryPoint
class GrowModeActivity : BaseActivity<HomeGrowModeActivityBinding>() {


    @Inject
    lateinit var mViewMode: ProModeViewModel

    // 是否是自家的帐篷， 只要是不为空，那么就是 tent_kit
    private val plantType by lazy {
        intent.getStringExtra(Constants.Global.KEY_PLANT_TYPE)
    }

    // 带过的category
    private val category by lazy {
        intent.getIntExtra(CATEGORY, -1)
    }

    // PLANT_ID
    private val plantId by lazy {
        intent.getStringExtra(PLANT_ID)
    }

    private val plantIdForBasePop by lazy {
        intent.getStringExtra(Constants.Global.KEY_PLANT_ID)
    }

    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    override fun initView() {
    }

    override fun observe() {
        mViewMode.apply {
            updateDeviceInfo.observe(this@GrowModeActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    // 点击跳转下一步。
                    // 如果是常规的跳转，
                    if (binding.curingBox.isChecked) {
                        // 这是tent——kit机器
                        if (!plantType.isNullOrEmpty()) {
                            // 跳转富文本界面
                            val intent = Intent(this@GrowModeActivity, KnowMoreActivity::class.java)
                            when(plantType) {
                                Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED_LABEL -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED)
                                    intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED)
                                }
                                Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE_LABEL -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE)
                                    intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE)
                                }
                                Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED_LABEL -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED)
                                    intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED)
                                }
                                Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING_LABEL -> {
                                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING)
                                    intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING)
                                }
                            }
                            intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                            intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                            intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, getString(com.cl.common_base.R.string.string_1393))
                            startActivity(intent)
                            return@success
                        }
                        // abby or 其他机器
                        // 看是否是选了clone、那么直接进入移植
                        if (category == 100003 || category == 100004) {
                            // 土培的种植之前的检查。
                            if (userinfoBean?.deviceType?.equalsIgnoreCase(Constants.Device.KEY_DEVICE_TYPE_O1_SOIL) == true) {
                                val intent = Intent(this@GrowModeActivity, BasePopActivity::class.java)
                                intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_SOIL_TRANSPLANT_CLONE_CHECK)
                                intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_SOIL_TRANSPLANT_CLONE_CHECK)
                                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                                intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, getString(com.cl.common_base.R.string.string_1368))
                                intent.putExtra(BasePopActivity.KEY_CATEGORYCODE, "$category")
                                intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                                startActivity(intent)
                                return@success
                            }
                            val intent = Intent(this@GrowModeActivity, BasePopActivity::class.java)
                            intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_CLONE_CHECK)
                            intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_CLONE_CHECK)
                            intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                            intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                            intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, getString(com.cl.common_base.R.string.string_1368))
                            intent.putExtra(BasePopActivity.KEY_CATEGORYCODE, "$category")
                            intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                            startActivity(intent)
                            return@success
                        }

                        // 跳转富文本界面
                        val intent = Intent(this@GrowModeActivity, KnowMoreActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, getString(com.cl.common_base.R.string.string_1393))
                        startActivity(intent)
                    } else {
                        //3. 点击Next，调用【创建日历模板】接口，拿到模板ID，templateId
                        createCalendar(PeriodListBody(plantId = if (plantId.isNullOrEmpty()) plantIdForBasePop else plantId))
                    }

                }
            })

            calendarTemp.observe(this@GrowModeActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    data?.templateId.let {
                        startActivity(Intent(this@GrowModeActivity, ProModeStartActivity::class.java).apply {
                            putExtra(Constants.Global.KEY_PLANT_TYPE, plantType)// 主要功能就是判断收拾tent_kit设备，后续流程对这个设备有些些微不同。
                            putExtra(ProModeStartActivity.TEMPLATE_ID, it)
                        })
                    }
                }
            })
        }
    }

    override fun initData() {
        binding.btnSuccess.setSafeOnClickListener {
            // 设置proMode模式
            mViewMode.updateDeviceInfo(UpDeviceInfoReq(
                deviceId = userinfoBean?.deviceId ?: "",
                proMode = if (binding.curingBox.isChecked) Constants.Global.KEY_CLOSE_PRO_MODE else Constants.Global.KEY_NEW_PRO_MODE
            ))
        }

        binding.curingBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBoxPhoto.isChecked = !isChecked
            /*showProgressLoading()
            mViewModel.cameraSetting(UpdateInfoReq(binding = true, deviceId = deviceId, storageModel = 0))*/
        }
        binding.curingBoxPhoto.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBox.isChecked = !isChecked
            /*showProgressLoading()
            mViewModel.cameraSetting(UpdateInfoReq(binding = true, deviceId = deviceId, storageModel = 1))*/
        }
    }

    companion object {
        // category
        const val CATEGORY = "category"

        // plantId
        const val PLANT_ID = "plantId"
    }
}