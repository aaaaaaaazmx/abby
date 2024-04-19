package com.cl.modules_my.ui

import android.view.View
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.util.chat.EnhancedChartUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyTestActivityBinding
import com.cl.modules_my.repository.UsbSwitchReq
import com.cl.modules_my.viewmodel.TestModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TestActivity : BaseActivity<MyTestActivityBinding>() {
    @Inject
    lateinit var mViewModel: TestModel

    override fun initView() {
        mViewModel.getDp()
        // 获取植物种植数据
        mViewModel.getPlantData("6cd6bbadef7c7cff23paxi")
    }

    override fun observe() {
        mViewModel.apply {
            // 切换USB
            usbSwitch.observe(this@TestActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.show(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()

                }
            })

            getPlantData.observe(this@TestActivity, resourceObserver {
                success {
                    // EnhancedChartUtil().setupEnhancedLineChart(binding.chart1, data?.termpertureList, "humidity" , "Grow Chamber Humidity")
                }

            })

            getDp.observe(this@TestActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()

                    data?.apply {
                        binding.cbUsbOne.isChecked = usb1
                        binding.cbUsbTwo.isChecked = usb2
                        binding.cbUsbThree.isChecked = usb3

                        binding.fitsLight.isItemChecked = tapeLights
                        binding.fitsTemp.isItemChecked = 0 == corF
                    }
                }
            })
        }
    }

    override fun initData() {
        binding.cbUsbOne.setOnCheckedChangeListener { buttonView, isChecked ->
            mViewModel.usbSwitch(UsbSwitchReq(mViewModel.deviceInfo?.deviceId, isChecked, binding.cbUsbTwo.isChecked, binding.cbUsbThree.isChecked, if (binding.fitsTemp.isItemChecked) 0 else 1, binding.fitsLight.isItemChecked))
        }
        binding.cbUsbTwo.setOnCheckedChangeListener { buttonView, isChecked ->
            mViewModel.usbSwitch(UsbSwitchReq(mViewModel.deviceInfo?.deviceId, binding.cbUsbOne.isChecked, isChecked, binding.cbUsbThree.isChecked, if (binding.fitsTemp.isItemChecked) 0 else 1, binding.fitsLight.isItemChecked))
        }
        binding.cbUsbThree.setOnCheckedChangeListener { buttonView, isChecked ->
            mViewModel.usbSwitch(UsbSwitchReq(mViewModel.deviceInfo?.deviceId, binding.cbUsbOne.isChecked, binding.cbUsbTwo.isChecked, isChecked, if (binding.fitsTemp.isItemChecked) 0 else 1, binding.fitsLight.isItemChecked))
        }

        binding.fitsLight.setSwitchCheckedChangeListener { buttonView, isChecked ->
            mViewModel.usbSwitch(UsbSwitchReq(mViewModel.deviceInfo?.deviceId, binding.cbUsbOne.isChecked, binding.cbUsbTwo.isChecked, binding.cbUsbThree.isChecked, if (binding.fitsTemp.isItemChecked) 0 else 1, isChecked))
        }
        binding.fitsTemp.setSwitchCheckedChangeListener { buttonView, isChecked ->
            mViewModel.usbSwitch(UsbSwitchReq(mViewModel.deviceInfo?.deviceId, binding.cbUsbOne.isChecked, binding.cbUsbTwo.isChecked, binding.cbUsbThree.isChecked, if (isChecked) 0 else 1, binding.fitsLight.isItemChecked))
        }
    }
}