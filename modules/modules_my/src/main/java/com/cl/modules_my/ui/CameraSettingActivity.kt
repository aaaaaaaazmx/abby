package com.cl.modules_my.ui

import android.app.Activity
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyCameraSettingBinding
import com.cl.modules_my.viewmodel.CameraSettingViewModel
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.tuya.smart.android.demo.camera.utils.DPConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 照片存储选项界面
 */
@AndroidEntryPoint
class CameraSettingActivity : BaseActivity<MyCameraSettingBinding>() {

    @Inject
    lateinit var mViewModel: CameraSettingViewModel

    // 配件ID
    private val accessoryDeviceId by lazy {
        intent.getStringExtra("accessoryDeviceId")
    }

    // 设备ID abby的
    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    override fun initView() {
        //  需要先获取当前的存储模式，然后设置选中状态，请求接口才能知道
        deviceId?.let { mViewModel.getAccessoryInfo(it) }
    }

    override fun observe() {
        mViewModel.apply {
            getAccessoryInfo.observe(this@CameraSettingActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (this.data == null) return@success
                    binding.curingBox.isChecked = data?.storageModel == "0"
                    binding.curingBoxPhoto.isChecked = data?.storageModel == "1"
                    binding.ftPrivacyMode.isItemChecked = data?.privateModel == true
                }
            })

            saveCameraSetting.observe(this@CameraSettingActivity, resourceObserver {
                error { errorMsg, code ->
                    com.cl.common_base.widget.toast.ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }

                success {
                    // 保存成功
                    hideProgressLoading()
                }
            })
        }
    }

    override fun onBackPressed() {
        ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
            .navigation()
        finish()
    }

    override fun initData() {
        binding.title.setLeftClickListener {
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                .navigation()
            finish()
        }

        binding.curingBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBoxPhoto.isChecked = !isChecked
            showProgressLoading()
            mViewModel.cameraSetting(UpdateInfoReq(deviceId = deviceId, storageModel = "0"))
        }
        binding.curingBoxPhoto.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBox.isChecked = !isChecked
            showProgressLoading()
            mViewModel.cameraSetting(UpdateInfoReq(deviceId = deviceId, storageModel = "1"))
        }

        binding.unbindCamera.setOnClickListener {
            // 解绑相机
            accessoryDeviceId?.let { it1 ->
                tuyaUtils.unBindCamera(it1, onErrorAction = {
                    ToastUtil.shortShow(it)
                }) {
                    // 绑定成功，结束当前页面，刷新配件列表
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }

        binding.ftPrivacyMode.setSwitchCheckedChangeListener { _, isChecked ->
            showProgressLoading()
            // 隐私模式的开关
            if (isChecked) {
                // 开启隐私模式
                accessoryDeviceId?.let { tuyaUtils.publishDps(it, DPConstants.PRIVATE_MODE, true) }
            } else {
                // 关闭隐私模式
                accessoryDeviceId?.let { tuyaUtils.publishDps(it, DPConstants.PRIVATE_MODE, false) }
            }

            accessoryDeviceId?.let {
                tuyaUtils.listenDPUpdate(it, DPConstants.PRIVATE_MODE, object : TuyaCameraUtils.DPCallback {
                    override fun callback(obj: Any) {
                        // 隐私模式的开关
                        binding.ftPrivacyMode.isItemChecked = obj.toString() == "true"
                        // 上传到服务器
                        mViewModel.cameraSetting(UpdateInfoReq(deviceId = deviceId, privateModel = obj.toString() == "true"))
                    }
                })
            }
        }
    }

    /**
     * 涂鸦摄像头帮助类
     */
    private val tuyaUtils by lazy {
        TuyaCameraUtils()
    }

    // 移除设备
    private fun unBindDevice() {
        ThingHomeSdk.newDeviceInstance(accessoryDeviceId).removeDevice(object : IResultCallback {
            override fun onError(s: String, s1: String) {
            }

            override fun onSuccess() {

            }
        })
    }
}