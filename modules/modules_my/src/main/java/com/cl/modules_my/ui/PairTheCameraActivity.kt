package com.cl.modules_my.ui

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreViewModel
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.util.ipc.CameraUtils
import com.cl.common_base.util.ipc.QRCodeUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyPairTheCameraBinding
import com.google.zxing.WriterException
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.builder.ThingCameraActivatorBuilder
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartCameraActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.tuya.smart.android.demo.camera.utils.DPConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 配对摄像头界面、生成二维码
 */
@AndroidEntryPoint
@Route(path = RouterPath.My.PAGE_CAMERA_QR_CODE)
class PairTheCameraActivity : BaseActivity<MyPairTheCameraBinding>() {

    @Inject
    lateinit var mViewMode: KnowMoreViewModel

    private val qrcodeUrl by lazy {
        intent.getStringExtra("qrcodeUrl")
    }

    private val token by lazy {
        intent.getStringExtra("token")
    }

    private val wifiName by lazy {
        intent.getStringExtra("wifiName")
    }

    private val wifiPsd by lazy {
        intent.getStringExtra("wifiPsd")
    }

    private val accessoryId by lazy {
        intent.getStringExtra("accessoryId")
    }

    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    private val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    override fun initView() {
        try {
            val bitmap = QRCodeUtil.createQRCode(qrcodeUrl, 330)
            runOnUiThread(Runnable {
                binding.ivImg.setImageBitmap(bitmap)
            })
        } catch (e: WriterException) {
            e.printStackTrace()
        }


        binding.unbindCamera.setOnClickListener {
            InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.Article, Constants.InterCome.KEY_INTER_COME_BIND_CAMERA)
        }
    }

    override fun observe() {
        mViewMode.apply {
            addAccessory.observe(this@PairTheCameraActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    val relationId = data?.relationId
                    /*CameraUtils.ipcProcess(this@PairTheCameraActivity, cameraId)*/
                    startActivity(Intent(this@PairTheCameraActivity, StoraceOptioneActivity::class.java).apply {
                        putExtra("accessoryDeviceId", cameraId)
                        putExtra("deviceId", deviceId)
                        putExtra("relationId", relationId)
                    })
                }
            })
        }
    }

    var cameraId = "cameraId"
    override fun initData() {
        // 监听是否成功还是失败
        // 首先获取配网token
        val builder = ThingCameraActivatorBuilder()
            .setToken(token)
            .setPassword(wifiPsd)
            .setTimeOut(100)
            .setContext(this@PairTheCameraActivity)
            .setSsid(wifiName)
            .setListener(object : IThingSmartCameraActivatorListener {
                override fun onQRCodeSuccess(qrcodeUrl: String?) {
                }

                override fun onError(errorCode: String?, errorMsg: String?) {
                    hideProgressLoading()
                    if (!errorCode.isNullOrEmpty() && !errorMsg.isNullOrEmpty()) {
                        ToastUtil.shortShow("errorCode: $errorCode errorMsg: $errorMsg")
                    }
                    // logI("123123: errorCode: $errorCode errorMsg: $errorMsg")
                }

                override fun onActiveSuccess(devResp: DeviceBean?) {
                    // 绑定成功、 跳转到视频界面
                    hideProgressLoading()
                    cameraId = devResp?.devId ?: ""
                    // logI("123123: cameraId: $cameraId  deviceId: $deviceId")

                    // 设置摄像头为连续摄像模式
                    // 设备设置页面-存储卡设置 SD卡录像模式选择，1为事件录像（检测到移动再录像到SD卡），2为连续录像
                    TuyaCameraUtils().publishDps(cameraId, DPConstants.SD_CARD_RECORD_MODE, "2")
                    letMultiple(accessoryId, deviceId) { a, b ->
                        // 新增配件接口
                        mViewMode.addAccessory(a, b, cameraId)
                    }
                }
            })

        // 开始配对
        ThingHomeSdk.getActivatorInstance().newCameraDevActivator(builder)?.apply {
            start()
        }
    }
}