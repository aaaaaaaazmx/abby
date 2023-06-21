package com.cl.modules_home.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSONObject
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeCameraBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.ext.logI
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.adapter.MyAdapter
import com.cl.modules_home.viewmodel.HomeCameraViewModel
import com.cl.modules_home.widget.CenterLayoutManager
import com.cl.modules_home.widget.HomeTimeLapseDestroyPop
import com.cl.modules_home.widget.HomeTimeLapsePop
import com.luck.picture.lib.utils.MediaStoreUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.AbsP2pCameraListener
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.OperationDelegateCallBack
import com.thingclips.smart.camera.ipccamerasdk.p2p.ICameraP2P
import com.thingclips.smart.camera.middleware.p2p.IThingSmartCameraP2P
import com.thingclips.smart.camera.middleware.widget.AbsVideoViewCallback
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IDevListener
import com.thingclips.smart.sdk.api.IResultCallback
import com.tuya.smart.android.demo.camera.CameraSettingActivity
import com.tuya.smart.android.demo.camera.databinding.ActivityCameraPanelBinding
import com.tuya.smart.android.demo.camera.utils.CameraPTZHelper
import com.tuya.smart.android.demo.camera.utils.DPConstants
import com.tuya.smart.android.demo.camera.utils.MessageUtil
import com.tuya.smart.android.demo.camera.utils.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


/**
 * 摄像头功能界面
 */
@AndroidEntryPoint
@Route(path = RouterPath.Home.PAGE_CAMERA)
class CameraActivity : BaseActivity<HomeCameraBinding>(), View.OnClickListener {

    @Inject
    lateinit var mViewModel: HomeCameraViewModel

    companion object {
        private const val ASPECT_RATIO_WIDTH = 9
        private const val ASPECT_RATIO_HEIGHT = 16
        private const val TAG = "CameraPanelActivity"
    }

    private var isSpeaking = false
    private var isPlay = false
    private var previewMute = ICameraP2P.MUTE
    private var videoClarity = ICameraP2P.HD
    private var currVideoClarity: String? = null
    private lateinit var viewBinding: ActivityCameraPanelBinding
    private var mCameraP2P: IThingSmartCameraP2P<Any>? = null

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT -> handleConnect(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_SET_CLARITY -> handleClarity(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_MUTE -> handleMute(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_SCREENSHOT -> handlesnapshot(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_BEGIN -> ToastUtil.shortToast(
                    this@CameraActivity,
                    getString(com.tuya.smart.android.demo.camera.R.string.operation_suc)
                )

                com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_FAIL -> ToastUtil.shortToast(
                    this@CameraActivity,
                    getString(com.tuya.smart.android.demo.camera.R.string.operation_failed)
                )

                com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_OVER -> handleVideoRecordOver(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_BEGIN -> handleStartTalk(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_OVER -> handleStopTalk(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_GET_VIDEO_CLARITY -> handleGetVideoClarity(msg)
            }
            super.handleMessage(msg)
        }
    }
    var cameraPTZHelper: CameraPTZHelper? = null

    private fun handleStopTalk(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(
                this@CameraActivity,
                getString(com.tuya.smart.android.demo.camera.R.string.ipc_stop_talk) + getString(com.tuya.smart.android.demo.camera.R.string.operation_suc)
            )
        } else {
            ToastUtil.shortToast(
                this@CameraActivity,
                getString(com.tuya.smart.android.demo.camera.R.string.ipc_stop_talk) + getString(com.tuya.smart.android.demo.camera.R.string.operation_failed)
            )
        }
    }

    private fun handleStartTalk(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(
                this@CameraActivity,
                getString(com.tuya.smart.android.demo.camera.R.string.ipc_start_talk) + getString(com.tuya.smart.android.demo.camera.R.string.operation_suc)
            )
        } else {
            ToastUtil.shortToast(
                this@CameraActivity,
                getString(com.tuya.smart.android.demo.camera.R.string.ipc_start_talk) + getString(com.tuya.smart.android.demo.camera.R.string.operation_failed)
            )
        }
    }

    private fun handleVideoRecordOver(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.operation_suc))
        } else {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.operation_failed))
        }
    }

    private fun handlesnapshot(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.operation_suc))
        } else {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.operation_failed))
        }
    }

    private fun handleMute(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            binding.cameraMute.isSelected = (previewMute == ICameraP2P.MUTE)
        } else {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.operation_failed))
        }
    }

    private fun handleClarity(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            binding.cameraQuality.text =
                if (videoClarity == ICameraP2P.HD) getString(com.tuya.smart.android.demo.camera.R.string.hd) else getString(com.tuya.smart.android.demo.camera.R.string.sd)
        } else {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.operation_failed))
        }
    }

    private fun handleConnect(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            preview();
        } else {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.connect_failed))
        }
    }

    private fun handleGetVideoClarity(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS && !TextUtils.isEmpty(currVideoClarity)) {
            var info = getString(com.tuya.smart.android.demo.camera.R.string.other)
            if (currVideoClarity == ICameraP2P.HD.toString()) {
                info = getString(com.tuya.smart.android.demo.camera.R.string.hd)
            } else if (currVideoClarity == ICameraP2P.STANDEND.toString()) {
                info = getString(com.tuya.smart.android.demo.camera.R.string.sd)
            }
            ToastUtil.shortToast(
                this@CameraActivity,
                getString(com.tuya.smart.android.demo.camera.R.string.get_current_clarity) + info
            )
        } else {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.operation_failed))
        }
    }


    var letters = arrayOf("TIME-LAPSE", "VIDEO", "PHOTO", "MIC", "PLAYBACK")

    private val devId by lazy {
        intent.getStringExtra(Constants.Global.INTENT_DEV_ID)
    }

    private val layoutMangers by lazy {
        CenterLayoutManager(this@CameraActivity, LinearLayoutManager.HORIZONTAL, false)
    }

    internal interface OnScrollListener {
        fun onScrolled(position: Int, adapters: MyAdapter)
    }

    private val scrollListener = object : OnScrollListener {
        override fun onScrolled(position: Int, adapters: MyAdapter) {
            adapters.focusedPosition = position
        }
    }

    private var adapters: MyAdapter? = null
    override fun initView() {
        // Check if the device version >= 23 because
        // from Android 6.0 (Marshmallow) you can set the status bar color.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Set status bar color to black
            window.statusBarColor = Color.BLACK;
            // Set status bar icons color to white
            window.decorView.systemUiVisibility = 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // For versions >= 21 and < 23, we can just set the status bar color
            window.statusBarColor = Color.BLACK;
        }

        val windowManager = this.getSystemService(WINDOW_SERVICE) as WindowManager
        val displayMetrics = resources.displayMetrics

        val width = windowManager.defaultDisplay.width
        val height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT

        // Convert dp to px
        val marginTopPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            165f,
            displayMetrics
        ).toInt()

        val layoutParams = RelativeLayout.LayoutParams(width, height).apply {
            addRule(RelativeLayout.BELOW, R.id.iv_back)
            topMargin = marginTopPx
        }

        binding.cameraVideoViewRl.layoutParams = layoutParams


        binding.ivBack.setOnClickListener { finish() }

        binding.recyclerView.apply {
            layoutManager = layoutMangers

            var recyclerViewWidth: Int = layoutParams.width
            if (recyclerViewWidth == -1) {
                val displayMetrics = resources.displayMetrics
                recyclerViewWidth = displayMetrics.widthPixels
            }

            val targetPosition = 1
            adapters = MyAdapter(letters, this@CameraActivity, recyclerViewWidth, this)
            val snapHelper: SnapHelper = LinearSnapHelper()
            adapter = adapters
            adapters?.focusedPosition = targetPosition
            snapHelper.attachToRecyclerView(this)

            // 滑动监听
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout) {
                        val centerView = snapHelper.findSnapView(layoutManager)
                        val pos = layoutManager!!.getPosition(centerView!!)

                        if (pos == lastPosition) {  //如果位置没有发生改变，则返回
                            return
                        }

                        lastPosition = pos  //更新lastPosition

                        adapters?.let { scrollListener.onScrolled(pos, it) }
                        logI("onScrollStateChanged: pos = $pos")
                        logI("onScrollStateChanged: text = ${adapters?.getLetter(pos)}")

                        // 首先默认设置为false 重置他们的选中状态
                        binding.ivCameraButton.isChecked = false
                        when (adapters?.getLetter(pos)) {
                            "TIME-LAPSE" -> {
                                // 设置iv_camera_button的背景为time_lapse
                                binding.ivCameraButton.setBackgroundResource(com.cl.common_base.R.drawable.create_camera_time_line)
                                // 需要判断当前是否是开启了time_lapse模式， 目前就存在本地吧
                                val isOpen = Prefs.getBoolean(Constants.Global.KEY_TIME_LAPSE, false)
                                binding.ivCameraButton.isChecked = isOpen
                            }

                            "VIDEO" -> {
                                binding.ivCameraButton.setBackgroundResource(com.cl.common_base.R.drawable.create_camera_video)
                            }

                            "PHOTO" -> {
                                binding.ivCameraButton.setBackgroundResource(com.cl.common_base.R.drawable.create_camera_photo)
                            }

                            "MIC" -> {
                                binding.ivCameraButton.setBackgroundResource(com.cl.common_base.R.drawable.create_camera_mic)
                            }

                            "PLAYBACK" -> {
                                binding.ivCameraButton.setBackgroundResource(com.cl.common_base.R.drawable.create_camera_record)
                            }
                        }
                    }
                }
            })
        }
    }

    // 上一个滚动的视图。
    var lastPosition = -1
    override fun observe() {
    }

    override fun initData() {
        ThingIPCSdk.getCameraInstance()?.let {
            mCameraP2P = it.createCameraP2P(devId)
        }
        binding.cameraVideoView.setViewCallback(object : AbsVideoViewCallback() {
            override fun onCreated(o: Any) {
                super.onCreated(o)
                mCameraP2P?.generateCameraView(o)
            }
        })
        //        binding.cameraVideoView.createVideoView(p2pType)
        binding.cameraVideoView.createVideoView(devId)
        if (mCameraP2P == null) showNotSupportToast()
        devId?.let {
            cameraPTZHelper = CameraPTZHelper(it)
        }

        // 设置监听
        initListener()
    }

    private fun initListener() {
        mCameraP2P?.let {
            binding.cameraMute.setOnClickListener(this)
            binding.cameraQuality.setOnClickListener(this)
            binding.ivThumbnail.setOnClickListener(this)
            binding.ivGetImage.setOnClickListener(this)
        }

        binding.ivCameraButton.setOnCheckedChangeListener { buttonView, isChecked ->
            val currentLetter = adapters?.focusedPosition?.let { adapters?.getLetter(it) }
            val isTimeLapse = currentLetter == "TIME-LAPSE" || currentLetter == "PHOTO"
            val timeLapseShow = Prefs.getBoolean(Constants.Contact.KEY_TIMELAPSE_TIP_IS_SHOW, false)

            when (adapters?.focusedPosition?.let { adapters?.getLetter(it) }) {
                "TIME-LAPSE" -> {
                    val timeLapseKey = Constants.Global.KEY_TIME_LAPSE
                    val currentStatus = Prefs.getBoolean(timeLapseKey, false)
                    val isShowAlready = Prefs.getBoolean(Constants.Contact.KEY_TIMELAPSE_TIP_IS_SHOW, false)

                    if (timeLapseShow || isChecked) {
                        if (currentStatus == isChecked) {
                            // If the status remains unchanged, no need to do anything
                            return@setOnCheckedChangeListener
                        }
                        // Toggle the status and store it
                        Prefs.putBooleanAsync(timeLapseKey, !currentStatus)
                    }

                    val dialogBuilder = XPopup.Builder(this@CameraActivity)
                        .dismissOnTouchOutside(false)
                        .isDestroyOnDismiss(false)
                        .dismissOnBackPressed(false)

                    if (isChecked) {
                        // 展示过一次就不需要在弹窗了。，直接开启
                        if (!isShowAlready) {
                            dialogBuilder.asCustom(
                                HomeTimeLapsePop(this@CameraActivity,
                                    onConfirmAction = {
                                        binding.ivCameraButton.isChecked = true
                                        Prefs.putBoolean(Constants.Contact.KEY_TIMELAPSE_TIP_IS_SHOW, true)
                                        // Toggle the status and store it
                                        Prefs.putBooleanAsync(timeLapseKey, !currentStatus)
                                    },
                                    onCancelAction = {
                                        Prefs.putBooleanAsync(timeLapseKey, false)
                                        binding.ivCameraButton.isChecked = false
                                    })
                            ).show()
                        } else {
                            // Toggle the status and store it
                            Prefs.putBooleanAsync(timeLapseKey, !currentStatus)
                        }

                    } else {
                        dialogBuilder
                            .popupPosition(PopupPosition.Top)
                            .dismissOnTouchOutside(true)
                            .isClickThrough(false)
                            .hasShadowBg(true)
                            .atView(binding.ivGetImage)
                            .isCenterHorizontal(false)
                            .asCustom(
                                HomeTimeLapseDestroyPop(this@CameraActivity)
                                    .setBubbleBgColor(Color.WHITE)
                                    .setArrowWidth(XPopupUtils.dp2px(this@CameraActivity, 3f))
                                    .setArrowHeight(XPopupUtils.dp2px(this@CameraActivity, 6f))
                                    .setArrowRadius(XPopupUtils.dp2px(this@CameraActivity, 3f))
                            ).show()
                    }
                }


                "VIDEO" -> {
                    recordClick(!isChecked)
                }

                "PHOTO" -> {
                    snapShotClick()
                }

                "MIC" -> {
                    // onPause的时候，会自动关闭
                    isSpeaking = !isChecked
                    speakClick()
                }

                "PLAYBACK" -> {
                    // todo 回放
                }
            }

            // 选中时候的判断
            if (isChecked) {
                layoutMangers.isScrollEnabled = isTimeLapse
                adapters?.isShouldDisableClick = isTimeLapse
            } else {
                layoutMangers.isScrollEnabled = true
                adapters?.isShouldDisableClick = true
            }
        }
    }

    private fun showNotSupportToast() {
        com.cl.common_base.widget.toast.ToastUtil.shortShow(getString(com.tuya.smart.android.demo.camera.R.string.not_support_device))
    }


    private fun preview() {
        mCameraP2P?.startPreview(videoClarity, object : OperationDelegateCallBack {
            override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                Log.d(TAG, "start preview onSuccess")
                isPlay = true
            }

            override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                Log.d(TAG, "start preview onFailure, errCode: $errCode")
                isPlay = false
            }
        })
    }

    // 录制
    private fun recordClick(isRecording: Boolean) {
        applyForAuthority {
            if (!it) return@applyForAuthority
            if (!isRecording) {
                // 创建文件夹
                FileUtil.createDirIfNotExists(SDCard.getCacheDir(this@CameraActivity) + File.separator + "camera" + File.separator + mViewModel.sn.value)
                // 文件路径
                val picPath = SDCard.getCacheDir(this@CameraActivity) + File.separator + "camera" + File.separator + mViewModel.sn.value
                // 文件名字
                val fileName = System.currentTimeMillis().toString() + ".mp4"
                mCameraP2P?.startRecordLocalMp4(
                    picPath,
                    fileName,
                    this@CameraActivity,
                    object : OperationDelegateCallBack {
                        override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                            /*mHandler.sendEmptyMessage(com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_BEGIN)*/
                            // 开始录制，上面需要显示计时器
                            ViewUtils.setVisible(binding.timer)
                            startTimer()
                            //returns the recorded thumbnail path （.jpg）
                            Log.i(TAG, "record :$data")

                        }

                        override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                            ViewUtils.setGone(binding.timer)
                            timer.cancel()
                            mHandler.sendEmptyMessage(com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_FAIL)
                        }
                    })
            } else {
                mCameraP2P?.stopRecordLocalMp4(object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        ViewUtils.setGone(binding.timer)
                        timer.cancel()
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_OVER,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                            )
                        )
                        logI("1231231@: $data")
                        Glide.with(this@CameraActivity)
                            .load(data)
                            .into(binding.ivThumbnail)

                        if (!isExistInSdCard()) {
                            // 保存到相册
                            saveFileToGallery(this@CameraActivity, filePath = data, title = System.currentTimeMillis().toString() + ".mp4", mimeType = "video/mp4", albumName = mViewModel.sn.value.toString())
                        }
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        ViewUtils.setGone(binding.timer)
                        timer.cancel()
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_OVER,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                            )
                        )
                    }
                })
            }
        }
    }

    // 截屏
    private fun snapShotClick() {
        applyForAuthority {
            if (!it) return@applyForAuthority
            // 创建文件夹
            FileUtil.createDirIfNotExists(SDCard.getCacheDir(this@CameraActivity) + File.separator + "camera" + File.separator + mViewModel.sn.value)
            // 文件路径
            val picPath = SDCard.getCacheDir(this@CameraActivity) + File.separator + "camera" + File.separator + mViewModel.sn.value
            // 文件名字
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            mCameraP2P?.snapshot(
                picPath,
                fileName,
                this@CameraActivity,
                object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_SCREENSHOT,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                            )
                        )
                        // 加载图片
                        Glide.with(this@CameraActivity)
                            .load(data)
                            .into(binding.ivThumbnail)
                        Log.i(TAG, "snapshot :$data")

                        if (!isExistInSdCard()) {
                            // 保存到相册
                            saveFileToGallery(this@CameraActivity, filePath = data, title = System.currentTimeMillis().toString() + ".jpg", mimeType = "image/jpeg", albumName = mViewModel.sn.value.toString())
                        }
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_SCREENSHOT,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                            )
                        )
                    }
                })
        }
    }

    // 声音点击
    private fun muteClick() {
        val mute = if (previewMute == ICameraP2P.MUTE) ICameraP2P.UNMUTE else ICameraP2P.MUTE
        mCameraP2P?.setMute(mute, object : OperationDelegateCallBack {
            override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                previewMute = Integer.valueOf(data)
                mHandler.sendMessage(
                    MessageUtil.getMessage(
                        com.tuya.smart.android.demo.camera.utils.Constants.MSG_MUTE,
                        com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                    )
                )
            }

            override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                mHandler.sendMessage(
                    MessageUtil.getMessage(
                        com.tuya.smart.android.demo.camera.utils.Constants.MSG_MUTE,
                        com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                    )
                )
            }
        })
    }

    // 对讲点击
    private fun speakClick() {
        if (isSpeaking) {
            mCameraP2P?.stopAudioTalk(object : OperationDelegateCallBack {
                override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                    isSpeaking = false
                    mHandler.sendMessage(
                        MessageUtil.getMessage(
                            com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_OVER,
                            com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                        )
                    )
                }

                override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                    isSpeaking = false
                    mHandler.sendMessage(
                        MessageUtil.getMessage(
                            com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_OVER,
                            com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                        )
                    )
                }
            })
        } else {
            if (com.tuya.smart.android.demo.camera.utils.Constants.hasRecordPermission()) {
                mCameraP2P?.startAudioTalk(object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        isSpeaking = true
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_BEGIN,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                            )
                        )
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        isSpeaking = false
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_BEGIN,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                            )
                        )
                    }
                })
            } else {
                com.tuya.smart.android.demo.camera.utils.Constants.requestPermission(
                    this@CameraActivity,
                    Manifest.permission.RECORD_AUDIO,
                    com.tuya.smart.android.demo.camera.utils.Constants.EXTERNAL_AUDIO_REQ_CODE,
                    "open_recording"
                )
            }
        }
    }

    /**
     * Set video quality, HD or SD
     * 设置清晰度
     */
    private fun setVideoClarity() {
        mCameraP2P?.setVideoClarity(
            if (videoClarity == ICameraP2P.HD) ICameraP2P.STANDEND else ICameraP2P.HD,
            object : OperationDelegateCallBack {
                override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                    videoClarity = Integer.valueOf(data)
                    mHandler.sendMessage(
                        MessageUtil.getMessage(
                            com.tuya.smart.android.demo.camera.utils.Constants.MSG_SET_CLARITY,
                            com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                        )
                    )
                }

                override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                    mHandler.sendMessage(
                        MessageUtil.getMessage(
                            com.tuya.smart.android.demo.camera.utils.Constants.MSG_SET_CLARITY,
                            com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                        )
                    )
                }
            })
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getSn()
        binding.cameraVideoView.onResume()
        //must register again,or can't callback
        mCameraP2P?.let {
            it.registerP2PCameraListener(p2pCameraListener)
            it.generateCameraView(binding.cameraVideoView.createdView())
            if (it.isConnecting) {
                it.startPreview(object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        isPlay = true
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        Log.d(TAG, "start preview onFailure, errCode: $errCode")
                    }
                })
            } else {
                if (ThingIPCSdk.getCameraInstance()?.isLowPowerDevice(devId) == true) {
                    ThingIPCSdk.getDoorbell()?.wirelessWake(devId)
                }
                //Establishing a p2p channel
                it.connect(devId, object : OperationDelegateCallBack {
                    override fun onSuccess(i: Int, i1: Int, s: String) {
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                            )
                        )
                    }

                    override fun onFailure(i: Int, i1: Int, i2: Int) {
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                            )
                        )
                    }
                })
            }
        }
    }

    var reConnect = false
    private val p2pCameraListener: AbsP2pCameraListener = object : AbsP2pCameraListener() {
        override fun onReceiveSpeakerEchoData(pcm: ByteBuffer, sampleRate: Int) {
            mCameraP2P?.let {
                val length = pcm.capacity()
                Log.d(TAG, "receiveSpeakerEchoData pcmlength $length sampleRate $sampleRate")
                val pcmData = ByteArray(length)
                pcm[pcmData, 0, length]
                it.sendAudioTalkData(pcmData, length)
            }
        }

        override fun onSessionStatusChanged(camera: Any?, sessionId: Int, sessionStatus: Int) {
            super.onSessionStatusChanged(camera, sessionId, sessionStatus)
            if (sessionStatus == -3 || sessionStatus == -105) {
                // 遇到超时/鉴权失败，建议重连一次，避免循环调用
                if (!reConnect) {
                    reConnect = true
                    mCameraP2P?.connect(devId, object : OperationDelegateCallBack {
                        override fun onSuccess(i: Int, i1: Int, s: String) {
                            mHandler.sendMessage(
                                MessageUtil.getMessage(
                                    com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT,
                                    com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                                )
                            )
                        }

                        override fun onFailure(i: Int, i1: Int, i2: Int) {
                            mHandler.sendMessage(
                                MessageUtil.getMessage(
                                    com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT,
                                    com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                                )
                            )
                        }
                    })
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        binding.cameraVideoView.onPause()
        timer.cancel()
        mCameraP2P?.let {
            if (isSpeaking) it.stopAudioTalk(null)
            if (isPlay) {
                it.stopPreview(object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {}
                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {}
                })
                isPlay = false
            }
            it.removeOnP2PCameraListener()
            it.disconnect(object : OperationDelegateCallBack {
                override fun onSuccess(i: Int, i1: Int, s: String) {}
                override fun onFailure(i: Int, i1: Int, i2: Int) {}
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
        mCameraP2P?.destroyP2P()
        timer.cancel()
    }

    // 点击事件
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.camera_mute -> muteClick()
            R.id.camera_quality -> setVideoClarity()
            R.id.iv_thumbnail -> {
                applyForAuthority {
                    if (!it) return@applyForAuthority
                    // todo 跳转到相册
                }
            }

            R.id.iv_get_image -> {
                // todo 跳转到生成界面
            }
        }
    }

    private val timer by lazy {
        Timer()
    }
    private var seconds = 0
    private fun startTimer() {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val timeView = findViewById<TextView>(R.id.timer)
                    val hours: Int = seconds / 3600
                    val minutes: Int = seconds % 3600 / 60
                    val secs: Int = seconds % 60
                    val time = String.format("%02d:%02d:%02d", hours, minutes, secs)
                    timeView.text = time
                    seconds++
                }
            }
        }, 0, 1000)
    }

    @SuppressLint("CheckResult")
    override fun onTuYaToAppDataChange(status: String) {
        super.onTuYaToAppDataChange(status)
        val map = GSON.parseObject(status, Map::class.java)
        map?.forEach { (key, value) ->
            when (key) {
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_REST_STATUS_INSTRUCTION -> {
                    if (value.toString().isEmpty()) return@forEach
                    logI(
                        """
                        KEY_DEVICE_REPAIR_REST_STATUS: 
                        value: ${value.toString()}
                    """.trimIndent()
                    )
                    //mcu:Abby-1.1.01-220519-T-B#abbyAAYA2021730021#1.4.0#flash:Abby-1.1.01-220519-T-B#1.4.0
                    // 截取, 并且需要置灰
                    kotlin.runCatching {
                        mViewModel.saveSn(value.toString().split("#")[1])

                        // 查找当前sd卡路径，是否展示图片还是灰色的颜色，不管相册还是sdcard的，都是和当前的sn相关的，但是相册里面的可能被删除。
                        // todo 需要从相册里面读取图片，如果没有图片，就展示灰色的图片
                        FileUtil.createDirIfNotExists(SDCard.getCacheDir(this@CameraActivity) + File.separator + "camera" + File.separator + value.toString().split("#")[1])
                        val picPath = findFirstImageInDir(SDCard.getCacheDir(this@CameraActivity) + File.separator + "camera" + File.separator + value.toString().split("#")[1])
                        val requestOptions = RequestOptions().apply {
                            error(R.drawable.home_gray_place_holder)
                            placeholder(R.drawable.home_gray_place_holder)
                        }
                        Glide.with(this@CameraActivity)
                            .load(picPath)
                            .apply(requestOptions)
                            .into(binding.ivThumbnail)
                    }
                }

                // 是否关闭门
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_DOOR -> {
                    // 主要用户删除当前的door的气泡消息
                    // true 开门、 fasle 关门
                    if (value.toString() == "true") {
                        // 开门，打开隐私模式
                        publishDps(DPConstants.PRIVATE_MODE, "true")
                    } else {
                        // todo 关门，查看接口返回的是不是隐私模式，如果是，那么就不关闭，反之关闭
                        publishDps(DPConstants.PRIVATE_MODE, "false")
                    }
                    listenDPUpdate(DPConstants.PRIVATE_MODE, object : CameraSettingActivity.DPCallback {
                        override fun callback(obj: Any) {
                            ViewUtils.setVisible(obj.toString() == "true", binding.tvPrivacyMode)
                        }
                    })
                }
            }
        }
    }

    /**
     * 遍历图片文件夹，找到第一张图片
     */
    private fun findFirstImageInDir(directory: String): String? {
        val dir = File(directory)
        if (dir.exists()) {
            val files = dir.listFiles { _, name ->
                name.endsWith(".jpeg", ignoreCase = true) || name.endsWith(".jpg", ignoreCase = true)
                        || name.endsWith(".png", ignoreCase = true) || name.endsWith(".bmp", ignoreCase = true)
                        || name.endsWith(".gif", ignoreCase = true)
            }
            if (!files.isNullOrEmpty()) {
                return files[0].absolutePath
            }
        }
        return null
    }

    /**
     *申请的权限不一致
     */
    private fun applyForAuthority(resultAction: (result: Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionHelp().applyPermissionHelp(
                this@CameraActivity,
                getString(com.cl.common_base.R.string.profile_request_photo),
                object : PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        resultAction.invoke(result)
                    }
                },
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            )
        } else {
            PermissionHelp().applyPermissionHelp(
                this@CameraActivity,
                getString(com.cl.common_base.R.string.profile_request_photo),
                object : PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        resultAction.invoke(result)
                    }
                },
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }

    /**
     * 根据后台返回的参数来判断，当前存储的路径是否在sd卡中或者相册中
     */
    private fun isExistInSdCard(): Boolean {
        // todo 根据后台来返回的参数来判断是否保存在相册中还是sd卡中
        return false
    }

    fun saveFileToGallery(context: Context, filePath: String, title: String, mimeType: String, albumName: String): Uri? {
        val file = File(filePath)
        if (!file.exists()) return null

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, title)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$albumName")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = file.inputStream()
            outputStream = context.contentResolver.openOutputStream(uri!!)
            outputStream?.let { inputStream.copyTo(it) }
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri!!, contentValues, null, null)
        }

        return uri
    }


    private fun queryValueByDPID(dpId: String): Any? {
        ThingHomeSdk.getDataInstance().getDeviceBean(devId)?.also {
            return it.getDps()?.get(dpId)
        }
        return null
    }

    private val iTuyaDevice by lazy {
        ThingHomeSdk.newDeviceInstance(devId)
    }

    private fun publishDps(dpId: String, value: Any) {
        val jsonObject = JSONObject()
        jsonObject[dpId] = value
        val dps = jsonObject.toString()
        iTuyaDevice!!.publishDps(dps, object : IResultCallback {
            override fun onError(code: String, error: String) {
                Log.e(TAG, "publishDps err $dps")
            }

            override fun onSuccess() {
                Log.i(TAG, "publishDps suc $dps")
            }
        })
    }

    private fun listenDPUpdate(dpId: String, callback: CameraSettingActivity.DPCallback?) {
        ThingHomeSdk.newDeviceInstance(devId).registerDevListener(object : IDevListener {
            override fun onDpUpdate(devId: String, dpStr: String) {
                callback?.let {
                    val dps: Map<String, Any> =
                        JSONObject.parseObject<Map<String, Any>>(dpStr, MutableMap::class.java)
                    if (dps.containsKey(dpId)) {
                        dps[dpId]?.let { it1 -> callback.callback(it1) }
                    }
                }
            }

            override fun onRemoved(devId: String) {}
            override fun onStatusChanged(devId: String, online: Boolean) {}
            override fun onNetworkStatusChanged(devId: String, status: Boolean) {}
            override fun onDevInfoUpdate(devId: String) {}
        })
    }
}