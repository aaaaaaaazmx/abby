package com.cl.modules_home.activity

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeCameraBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.modules_home.adapter.MyAdapter
import com.cl.modules_home.widget.CenterLayoutManager
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.AbsP2pCameraListener
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.OperationDelegateCallBack
import com.thingclips.smart.camera.ipccamerasdk.p2p.ICameraP2P
import com.thingclips.smart.camera.middleware.p2p.IThingSmartCameraP2P
import com.thingclips.smart.camera.middleware.widget.AbsVideoViewCallback
import com.tuya.smart.android.demo.camera.databinding.ActivityCameraPanelBinding
import com.tuya.smart.android.demo.camera.utils.CameraPTZHelper
import com.tuya.smart.android.demo.camera.utils.MessageUtil
import com.tuya.smart.android.demo.camera.utils.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.nio.ByteBuffer

/**
 * 摄像头功能界面
 */
@AndroidEntryPoint
@Route(path = RouterPath.Home.PAGE_CAMERA)
class CameraActivity: BaseActivity<HomeCameraBinding>(), View.OnClickListener {
    companion object {
        private const val ASPECT_RATIO_WIDTH = 9
        private const val ASPECT_RATIO_HEIGHT = 16
        private const val TAG = "CameraPanelActivity"
    }

    private var isSpeaking = false
    private var isRecording = false
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
    
    
    var letters = arrayOf("TIME-LAPSE", "VIDEO", "PHOTO", "MIC")

    private val devId by lazy {
        intent.getStringExtra(Constants.Global.INTENT_DEV_ID)
    }

    internal interface OnScrollListener {
        fun onScrolled(position: Int, adapters: MyAdapter)
    }
    private val scrollListener = object : OnScrollListener {
        override fun onScrolled(position: Int, adapters: MyAdapter) {
            adapters.setFocusedPosition(position)
        }
    }

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
            layoutManager = CenterLayoutManager(this@CameraActivity, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = layoutManager


            var recyclerViewWidth: Int = layoutParams.width
            if (recyclerViewWidth == -1) {
                val displayMetrics = resources.displayMetrics
                recyclerViewWidth = displayMetrics.widthPixels
            }

            val targetPosition = 1
            val adapters = MyAdapter(letters, this@CameraActivity, recyclerViewWidth, this)
            val snapHelper: SnapHelper = LinearSnapHelper()
            adapter = adapters
            adapters.setFocusedPosition(targetPosition)
            snapHelper.attachToRecyclerView(this)

            // 滑动监听
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout) {
                        val centerView = snapHelper.findSnapView(layoutManager)
                        val pos = layoutManager!!.getPosition(centerView!!)
                        scrollListener.onScrolled(pos, adapters)
                        logI("onScrollStateChanged: pos = $pos")
                    }
                }
            })
        }
      
    }

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
    private fun recordClick() {
        if (!isRecording) {
            val picPath = getExternalFilesDir(null)!!.path + "/" + devId
            val file = File(picPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".mp4"
            mCameraP2P?.startRecordLocalMp4(
                picPath,
                fileName,
                this@CameraActivity,
                object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        isRecording = true
                        mHandler.sendEmptyMessage(com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_BEGIN)
                        //returns the recorded thumbnail path （.jpg）
                        Log.i(TAG, "record :$data")
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        mHandler.sendEmptyMessage(com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_FAIL)
                    }
                })
            recordStatue(true)
        } else {
            mCameraP2P?.stopRecordLocalMp4(object : OperationDelegateCallBack {
                override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                    isRecording = false
                    mHandler.sendMessage(
                        MessageUtil.getMessage(
                            com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_OVER,
                            com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                        )
                    )
                }

                override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                    isRecording = false
                    mHandler.sendMessage(
                        MessageUtil.getMessage(
                            com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_OVER,
                            com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                        )
                    )
                }
            })
            recordStatue(false)
        }
    }

    // 截屏
    private fun snapShotClick() {
        val picPath = getExternalFilesDir(null)!!.path + "/" + devId
        val file = File(picPath)
        if (!file.exists()) file.mkdirs()
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
                    Log.i(TAG, "snapshot :$data")
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


    // todo 录制时的状态
    private fun recordStatue(isRecording: Boolean) {
        /*binding.cameraControlBoard.speakTxt.isEnabled = !isRecording
        binding.cameraControlBoard.photoTxt.isEnabled = !isRecording
        binding.cameraControlBoard.replayTxt.isEnabled = !isRecording
        binding.cameraControlBoard.recordTxt.isEnabled = true
        binding.cameraControlBoard.recordTxt.isSelected = isRecording*/
    }

    override fun onResume() {
        super.onResume()
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
                            mHandler.sendMessage(MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS))
                        }

                        override fun onFailure(i: Int, i1: Int, i2: Int) {
                            mHandler.sendMessage(MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL))
                        }
                    })
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        binding.cameraVideoView.onPause()
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
    }

    // 点击事件
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.camera_mute -> muteClick()
            R.id.camera_quality -> setVideoClarity()
        }
    }
}