package com.cl.modules_home.activity

import android.content.Intent
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
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
@AndroidEntryPoint
class GrowModeActivity : BaseActivity<HomeGrowModeActivityBinding>() {


    @Inject
    lateinit var mViewMode: ProModeViewModel

    // 带过的category
    private val category by lazy {
        intent.getIntExtra(CATEGORY, -1)
    }

    // PLANT_ID
    private val plantId by lazy {
        intent.getStringExtra(PLANT_ID)
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
                        // 看是否是选了clone、那么直接进入移植
                        if (category == 100003 || category == 100004) {
                            // 土培的种植之前的检查。
                            if (userinfoBean?.deviceType?.equalsIgnoreCase(Constants.Device.KEY_DEVICE_TYPE_O1_SOIL) == true) {
                                val intent = Intent(this@GrowModeActivity, BasePopActivity::class.java)
                                intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_SOIL_TRANSPLANT_CLONE_CHECK)
                                intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_SOIL_TRANSPLANT_CLONE_CHECK)
                                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                                intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                                intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "I am ready")
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
                            intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "I am ready")
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
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "Done")
                        startActivity(intent)
                    } else {
                        //3. 点击Next，调用【创建日历模板】接口，拿到模板ID，templateId
                        createCalendar(PeriodListBody(plantId = plantId))
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