package com.cl.modules_home.ui

import com.bbgo.module_home.databinding.HomeChangTheSeedBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.viewmodel.HomePlantProfileViewModel
import com.thingclips.smart.android.user.bean.User
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangTheSeedActivity : BaseActivity<HomeChangTheSeedBinding>() {

    @Inject
    lateinit var mViewModel: HomePlantProfileViewModel

    private val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    override fun initView() {

    }

    override fun observe() {
        mViewModel.apply {
            plantDelete.observe(this@ChangTheSeedActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    mViewModel.checkPlant()
                }
            })

            // 检查植物
            checkPlant.observe(this@ChangTheSeedActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }

                success {
                    // 是否种植过
                    data?.let { PlantCheckHelp().plantStatusCheck(this@ChangTheSeedActivity, it, true) }
                }
            })
        }
    }

    override fun initData() {
        binding.slideToConfirm.slideListener = object : ISlideListener {
            override fun onSlideStart() {
            }

            override fun onSlideMove(percent: Float) {
            }

            override fun onSlideCancel() {
            }

            override fun onSlideDone() {
                binding.slideToConfirm.postDelayed(Runnable { binding.slideToConfirm.reset() }, 500)
                // 重新种植
                tuYaUser?.uid?.let { uid -> mViewModel.plantDelete(uid) }
            }
        }
    }
}