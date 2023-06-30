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
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeCameraBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.modules_home.adapter.MyAdapter
import com.cl.modules_home.viewmodel.HomeCameraViewModel
import com.cl.modules_home.widget.CameraChooserGerPop
import com.cl.modules_home.widget.CenterLayoutManager
import com.cl.modules_home.widget.HomeTimeLapseDestroyPop
import com.cl.modules_home.widget.HomeTimeLapsePop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.thingclips.smart.android.camera.timeline.OnBarMoveListener
import com.thingclips.smart.android.camera.timeline.TimeBean
import com.thingclips.smart.android.common.utils.L
import com.thingclips.smart.camera.base.log.ThingCameraModule.playback
import com.thingclips.smart.camera.camerasdk.bean.ThingVideoFrameInfo
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.AbsP2pCameraListener
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.OperationDelegateCallBack
import com.thingclips.smart.camera.ipccamerasdk.bean.MonthDays
import com.thingclips.smart.camera.ipccamerasdk.p2p.ICameraP2P
import com.thingclips.smart.camera.middleware.p2p.IThingSmartCameraP2P
import com.thingclips.smart.camera.middleware.widget.AbsVideoViewCallback
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.tuya.smart.android.demo.camera.CameraPlaybackActivity
import com.tuya.smart.android.demo.camera.bean.RecordInfoBean
import com.tuya.smart.android.demo.camera.bean.TimePieceBean
import com.tuya.smart.android.demo.camera.databinding.ActivityCameraPanelBinding
import com.tuya.smart.android.demo.camera.utils.CameraPTZHelper
import com.tuya.smart.android.demo.camera.utils.DPConstants
import com.tuya.smart.android.demo.camera.utils.MessageUtil
import com.tuya.smart.android.demo.camera.utils.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.Calendar
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
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE -> handleDataDate(msg)
                com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE_BY_DAY_SUCC, com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE_BY_DAY_FAIL -> handleDataDay(
                    msg
                )
            }
            super.handleMessage(msg)
        }
    }
    var cameraPTZHelper: CameraPTZHelper? = null


    // 回放功能
    // 是否正在回放
    private var isPlayback = false

    // key 为查询的day
    var mBackDataMonthCache: MutableMap<String, MutableList<String>>? = HashMap()

    // 查询的日期的一级列表，需要根据这个查询当天的时间段列表 如2023/06/28/09 当前的9点开始
    private var dateList: ArrayList<String>? = arrayListOf()

    // 具体的时间节点列表 如 09:22:01,时分秒列表
    private var queryDateList: MutableList<TimePieceBean>? = mutableListOf()

    // 具体时间列表的缓存
    var mBackDataDayCache: MutableMap<String, MutableList<TimePieceBean>>? = HashMap()

    private fun showErrorToast() {
        runOnUiThread {
            com.cl.common_base.widget.toast.ToastUtil.shortShow(getString(com.tuya.smart.android.demo.camera.R.string.no_data))
        }
    }

    /**
     * 发送日期数据
     */
    private fun handleDataDate(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            dateList?.clear()
            queryDateList?.clear();
            val days = mBackDataMonthCache?.get(mCameraP2P?.monthKey)
            if (days.isNullOrEmpty()) {
                showErrorToast()
                return
            }

            // 保存的格式为 2023/06/28/09 2023/06/28/26
            if (currentDate.isNotEmpty() && currentDate.contains("/")) {
                for (s in days) {
                    dateList?.add("$currentDate/$s")
                }
            }

            // 根据上面的dateList来出查询具体的时间列表
            showTimePieceAtDay(dateList?.get(0))
        }
    }

    /**
     * 根据日期，来查询具体的时间列表
     */
    private fun showTimePieceAtDay(inputStr: String?) {
        if (!inputStr.isNullOrEmpty() && inputStr.contains("/")) {
            val substring = inputStr.split("/".toRegex()).toTypedArray()
            val year = substring[0].toInt()
            val mouth = substring[1].toInt()
            val day = substring[2].toInt()
            mCameraP2P?.queryRecordTimeSliceByDay(
                year,
                mouth,
                day,
                object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        logI("$inputStr --- $data")
                        parsePlaybackData(data)
                    }

                    override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                        mHandler.sendEmptyMessage(com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE_BY_DAY_FAIL)
                    }
                })
        }
    }

    /**
     * 解析具体的时间列表 如 09:22:01,时分秒列表
     */
    private fun parsePlaybackData(obj: Any) {
        val parseObject = JSON.parseObject(obj.toString(), RecordInfoBean::class.java)
        if (parseObject.count != 0) {
            if (parseObject.items.isNotEmpty()) {
                mBackDataDayCache?.put(mCameraP2P?.dayKey.toString(), parseObject.items)
            }
            mHandler.sendMessage(
                MessageUtil.getMessage(
                    com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE_BY_DAY_SUCC,
                    com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                )
            )
        } else {
            mHandler.sendMessage(
                MessageUtil.getMessage(
                    com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE_BY_DAY_FAIL,
                    com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                )
            )
        }
    }

    /**
     * 查询某一天的时间列表
     */
    private fun handleDataDay(msg: Message) {
        if (msg.arg1 == com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS) {
            queryDateList?.clear()
            //Timepieces with data for the query day
            val timePieceBeans = mBackDataDayCache!![mCameraP2P?.dayKey]
            if (!timePieceBeans.isNullOrEmpty()) {
                queryDateList?.addAll(timePieceBeans)
                val timelineData: MutableList<TimeBean> = arrayListOf()
                for ((startTime, endTime) in timePieceBeans) {
                    val b = TimeBean()
                    b.startTime = startTime
                    b.endTime = endTime
                    timelineData.add(b)
                }
                binding.timeline.setCurrentTimeConfig(timePieceBeans[0].endTime * 1000L)
                binding.timeline.setRecordDataExistTimeClipsList(timelineData)
            } else {
                showErrorToast()
            }

            // 查询到了，直接播放，
            if (!queryDateList.isNullOrEmpty()) {
                kotlin.runCatching {
                    val startTime = queryDateList?.get(0)
                    val endTime = queryDateList?.get(queryDateList?.size?.minus(1) ?: 0)
                    playback(startTime?.startTime!!, endTime?.endTime!!, startTime.startTime)
                }
            }
        }
    }


    private fun playback(startTime: Int, endTime: Int, playTime: Int) {
        mCameraP2P?.startPlayBack(startTime, endTime, playTime, object : OperationDelegateCallBack {
            override fun onSuccess(sessionId: Int, requestId: Int, data: String?) {
                logI("startPlayBack onSuccess")
                isPlayback = true
            }

            override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                isPlayback = false
                logI("startPlayBack onFailure")
            }
        }, object : OperationDelegateCallBack {
            override fun onSuccess(sessionId: Int, requestId: Int, data: String?) {
                isPlayback = false
                logI("onReceiveFirstFrame onSuccess")
            }

            override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                isPlayback = false
                logI("onReceiveFirstFrame onFailure")
            }
        })
    }

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
        // 注册监听
        devId?.let {
            tuYaUtils.listenDPUpdate(devId = it, dpId = DPConstants.PRIVATE_MODE, onStatusChangedAction = { online ->
                // 监听设备是否在线
                if (!online) {
                    binding.ivCameraButton.isClickable = false
                    binding.ivCameraButton.isEnabled = false
                    binding.ivCameraButton.isFocusable = false
                } else {
                    binding.ivCameraButton.isClickable = true
                    binding.ivCameraButton.isEnabled = true
                    binding.ivCameraButton.isFocusable = true
                }
            })
        }

        // 获取配件信息
        mViewModel.tuYaDeviceBean?.devId?.let { mViewModel.getAccessoryInfo(it) }

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
                        if (binding.ivCameraButton.isChecked) {
                            mViewModel.selectCallBack(true)
                            binding.ivCameraButton.isChecked = false
                        }

                        // 使用映射来查找对应的背景资源
                        val background = backgrounds[adapters?.getLetter(pos)]

                        // 如果找到了对应的背景资源，就更新按钮的背景和选中状态
                        if (background != null) {
                            binding.ivCameraButton.setBackgroundResource(background)

                            // 需要判断当前是否是开启了time_lapse模式，目前就存在本地吧
                            val isOpen = Prefs.getBoolean(Constants.Global.KEY_TIME_LAPSE, false)

                            if (adapters?.getLetter(pos) == "TIME-LAPSE") {
                                binding.ivCameraButton.isChecked = isOpen
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
        mViewModel.apply {
            getPartsInfo.observe(this@CameraActivity, resourceObserver {
                error { errorMsg, code ->
                    com.cl.common_base.widget.toast.ToastUtil.shortShow(errorMsg)
                }
                success {
                    if (null == data) return@success
                }
            })


            // 配件信息
            getAccessoryInfo.observe(this@CameraActivity, resourceObserver {
                error { errorMsg, code ->
                    com.cl.common_base.widget.toast.ToastUtil.shortShow(errorMsg)
                }

                success {

                    if (data == null) return@success
                    // 隐私模式不能显示
                    if (data?.privateModel == true) {
                        binding.ivCameraButton.isClickable = false
                        binding.ivCameraButton.isEnabled = false
                        binding.ivCameraButton.isFocusable = false

                        // 主动设置为隐私模式
                        devId?.let { tuYaUtils.publishDps(it, DPConstants.PRIVATE_MODE, true) }
                    } else {
                        binding.ivCameraButton.isClickable = true
                        binding.ivCameraButton.isEnabled = true
                        binding.ivCameraButton.isFocusable = true
                        // 主动设置为隐私模式
                        devId?.let { tuYaUtils.publishDps(it, DPConstants.PRIVATE_MODE, false) }
                    }

                    devId?.let {
                        tuYaUtils.listenDPUpdate(it, DPConstants.PRIVATE_MODE, object : TuyaCameraUtils.DPCallback {
                            override fun callback(obj: Any) {
                                logI("123123@: ${obj.toString()}")
                                ViewUtils.setVisible(obj.toString() == "true", binding.tvPrivacyMode)
                            }
                        })
                    }
                }
            })
        }
    }

    override fun initData() {
        // 获取配件信息
        mViewModel.getPartsInfo()

        ThingIPCSdk.getCameraInstance()?.let {
            mCameraP2P = it.createCameraP2P(devId)
        }
        binding.cameraVideoView.setViewCallback(object : AbsVideoViewCallback() {
            override fun onCreated(o: Any) {
                super.onCreated(o)
                mCameraP2P?.generateCameraView(o)
            }
        })
        /*binding.cameraVideoView.createVideoView(p2pType)*/
        binding.cameraVideoView.createVideoView(devId)
        if (mCameraP2P == null) showNotSupportToast()
        devId?.let {
            cameraPTZHelper = CameraPTZHelper(it)
        }

        // 设置监听
        initListener()
    }

    private fun initListener() {
        binding.cameraMute.setOnClickListener(this)
        binding.cameraQuality.setOnClickListener(this)
        binding.ivThumbnail.setOnClickListener(this)
        binding.ivGetImage.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)

        binding.ivCameraButton.setOnCheckedChangeListener { buttonView, isChecked ->
            val currentLetter = adapters?.focusedPosition?.let { adapters?.getLetter(it) }
            val isTimeLapse = currentLetter == "TIME-LAPSE" || currentLetter == "PHOTO"
            val timeLapseShow = Prefs.getBoolean(Constants.Contact.KEY_TIMELAPSE_TIP_IS_SHOW, false)

            // 主要用户在重置时产生的一系列问题
            if (mViewModel.selectCallBack.value == true) {
                mViewModel.selectCallBack(false)
                return@setOnCheckedChangeListener
            }

            // 隐私模式，不能做任何事情。
            if (mViewModel.getAccessoryInfo.value?.data?.privateModel == true) {
                com.cl.common_base.widget.toast.ToastUtil.shortShow("Currently in privacy mode")
                return@setOnCheckedChangeListener
            }

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

                                        // 判断是否第一次开启，判断是否需要截图
                                        isScreenshots()
                                    },
                                    onCancelAction = {
                                        Prefs.putBooleanAsync(timeLapseKey, false)
                                        binding.ivCameraButton.isChecked = false
                                    })
                            ).show()
                        } else {
                            // Toggle the status and store it
                            Prefs.putBooleanAsync(timeLapseKey, !currentStatus)

                            // 判断是否第一次开启，判断是否需要截图
                            isScreenshots()
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
                    ViewUtils.setVisible(isChecked, binding.timelineLayout)
                    binding.timeline.setOnBarMoveListener(object : OnBarMoveListener {
                        override fun onBarMove(l: Long, l1: Long, l2: Long) {}
                        override fun onBarMoveFinish(startTime: Long, endTime: Long, currentTime: Long) {
                            binding.timeline.setCanQueryData()
                            binding.timeline.setQueryNewVideoData(false)
                            if (startTime != -1L && endTime != -1L) {
                                playback(startTime.toInt(), endTime.toInt(), currentTime.toInt())
                            }
                        }

                        override fun onBarActionDown() {}
                    })
                    binding.timeline.setOnSelectedTimeListener { _, _ -> }
                    if (isChecked) {
                        // 开始播放
                        queryDayByMonthClick()
                    } else {
                        // 暂停播放
                        if (isPlayback) {
                            isPlayback = false
                            mCameraP2P?.stopPlayBack(null)
                            /*pauseOnCamera()*/
                            mCameraP2P?.let {
                                binding.cameraVideoView.onPause()
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
                                    override fun onSuccess(i: Int, i1: Int, s: String) {
                                        runOnUiThread {
                                            binding.cameraVideoView.onResume()
                                            //must register again,or can't callback
                                            it.registerP2PCameraListener(p2pCameraListener)
                                            it.generateCameraView(binding.cameraVideoView.createdView())
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

                                    override fun onFailure(i: Int, i1: Int, i2: Int) {}
                                })
                            }
                        }
                    }
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

    /**
     * 查询回放功能
     * 日期格式为 2023/06
     */
    private val currentDate by lazy {
        DateHelper.formatTime(System.currentTimeMillis(), "yyyy/MM/dd")
    }

    private fun queryDayByMonthClick() {
        // 默认加载为今天的。
        logI("queryDayByMonthClick currentDate = $currentDate")
        if (mCameraP2P?.isConnecting == false) {
            ToastUtil.shortToast(this@CameraActivity, getString(com.tuya.smart.android.demo.camera.R.string.connect_first))
            return
        }
        val substring = currentDate.split("/".toRegex()).toTypedArray()
        if (substring.size >= 2) {
            try {
                val year = substring[0].toInt()
                val mouth = substring[1].toInt()
                mCameraP2P?.queryRecordDaysByMonth(
                    year,
                    mouth,
                    object : OperationDelegateCallBack {
                        override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                            val monthDays = JSONObject.parseObject(data, MonthDays::class.java)
                            mBackDataMonthCache?.put(mCameraP2P?.monthKey.toString(), monthDays.dataDays)
                            logI("MonthDays --- $data")
                            mHandler.sendMessage(
                                MessageUtil.getMessage(
                                    com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE,
                                    com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                                )
                            )
                        }

                        override fun onFailure(sessionId: Int, requestId: Int, errCode: Int) {
                            mHandler.sendMessage(
                                MessageUtil.getMessage(
                                    com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE,
                                    com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL
                                )
                            )
                        }
                    })
            } catch (e: Exception) {
                com.cl.common_base.widget.toast.ToastUtil.shortShow(getString(com.tuya.smart.android.demo.camera.R.string.input_err))
            }
        }

    }

    /**
     * 是否需要截图
     */
    private fun isScreenshots() {
        // 隐私模式不截图，留着下一次截图
        if (mViewModel.getAccessoryInfo.value?.data?.privateModel == true) return
        val time = System.currentTimeMillis()
        val lastSnapshotTime = Prefs.getLong(Constants.Global.KEY_IS_LAST_OPERATION_DATE)

        // 初始化日历对象
        val currentCalendar = Calendar.getInstance().apply {
            timeInMillis = time
        }
        val lastSnapshotCalendar = Calendar.getInstance().apply {
            timeInMillis = lastSnapshotTime
        }

        // 判断今天是否已经截图
        if (lastSnapshotTime == 0L || currentCalendar.get(Calendar.YEAR) != lastSnapshotCalendar.get(Calendar.YEAR) || currentCalendar.get(Calendar.DAY_OF_YEAR) != lastSnapshotCalendar.get(Calendar.DAY_OF_YEAR)) {
            // 如果今天还没截图，就执行截图操作并更新截图时间
            snapShotClick()
            Prefs.putLong(Constants.Global.KEY_IS_LAST_OPERATION_DATE, time)
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
                val picPath = createFileDir()
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
                            timer?.cancel()
                            mHandler.sendEmptyMessage(com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_FAIL)
                        }
                    })
            } else {
                mCameraP2P?.stopRecordLocalMp4(object : OperationDelegateCallBack {
                    override fun onSuccess(sessionId: Int, requestId: Int, data: String) {
                        ViewUtils.setGone(binding.timer)
                        timer?.cancel()
                        mHandler.sendMessage(
                            MessageUtil.getMessage(
                                com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_OVER,
                                com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS
                            )
                        )
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
                        timer?.cancel()
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
            val picPath = createFileDir()
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
        goOnCameraPlay()
    }

    /**
     * 检查是否可以继续显示摄像数据
     */
    private fun goOnCameraPlay() {
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

        override fun onReceiveFrameYUVData(
            i: Int,
            byteBuffer: ByteBuffer,
            byteBuffer1: ByteBuffer,
            byteBuffer2: ByteBuffer,
            i1: Int,
            i2: Int,
            i3: Int,
            i4: Int,
            l: Long,
            l1: Long,
            l2: Long,
            o: Any,
        ) {
            super.onReceiveFrameYUVData(
                i,
                byteBuffer,
                byteBuffer1,
                byteBuffer2,
                i1,
                i2,
                i3,
                i4,
                l,
                l1,
                l2,
                o
            )
            if (adapters?.focusedPosition?.let { adapters?.getLetter(it) } == "PLAYBACK") {
                binding.timeline.setCurrentTimeInMillisecond(l * 1000L)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        binding.cameraVideoView.onPause()
        timer?.cancel()
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
                override fun onSuccess(i: Int, i1: Int, s: String) {
                }

                override fun onFailure(i: Int, i1: Int, i2: Int) {}
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
        mCameraP2P?.destroyP2P()
        timer?.cancel()
    }

    // 点击事件
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.camera_mute -> muteClick()
            R.id.camera_quality -> setVideoClarity()
            R.id.iv_thumbnail -> {
                applyForAuthority {
                    if (!it) return@applyForAuthority
                    // 跳转到相册
                    startActivity(Intent(this@CameraActivity, CameraChooserActivity::class.java).apply {
                        // 传递设备id
                        putExtra("devId", devId)
                        // 传递sdCard相册路径
                        putExtra("sdCardPath", createFileDir())
                        // 是否是保存在相册还是保存在本地
                        putExtra("isSaveAlbum", mViewModel.getAccessoryInfo.value?.data?.storageModel == 0)
                        // 设备的sn号
                        putExtra("sn", mViewModel.sn.value)
                    })
                }
            }

            R.id.iv_get_image -> {
                // 跳转到生成界面
                XPopup.Builder(this@CameraActivity)
                    .popupPosition(PopupPosition.Left)
                    .dismissOnTouchOutside(true)
                    .isClickThrough(false)  //点击透传
                    .hasShadowBg(true) // 去掉半透明背景
                    //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                    .atView(binding.ivGetImage)
                    .isCenterHorizontal(false)
                    .asCustom(
                        CameraChooserGerPop(
                            this@CameraActivity,
                            gifAction = {
                                // 跳转gif生成界面
                                // 是否存在相册，
                                // 相册地址，
                                // 内存地址
                                ARouter.getInstance().build(RouterPath.Contact.PAGE_GIF)
                                    .withBoolean("isAlbum", mViewModel.getAccessoryInfo.value?.data?.storageModel == 0)
                                    .withString("sdCardPath", createFileDir())
                                    .withString("devId", devId)
                                    .withString("albumPath", getAlbumDir())
                                    .withBoolean("isVideo", false)
                                    .withString("sn", mViewModel.sn.value)
                                    .navigation()
                            },
                            videoAction = {
                                ARouter.getInstance().build(RouterPath.Contact.PAGE_GIF)
                                    .withBoolean("isAlbum", mViewModel.getAccessoryInfo.value?.data?.storageModel == 0)
                                    .withString("sdCardPath", createFileDir())
                                    .withString("devId", devId)
                                    .withString("albumPath", getAlbumDir())
                                    .withBoolean("isVideo", true)
                                    .withString("sn", mViewModel.sn.value)
                                    .navigation()
                            }
                        ).setBubbleBgColor(Color.WHITE) //气泡背景
                            .setArrowWidth(XPopupUtils.dp2px(this@CameraActivity, 3f))
                            .setArrowHeight(
                                XPopupUtils.dp2px(
                                    this@CameraActivity,
                                    3f
                                )
                            )
                            //.setBubbleRadius(100)
                            .setArrowRadius(
                                XPopupUtils.dp2px(
                                    this@CameraActivity,
                                    3f
                                )
                            )
                    ).show()
            }

            R.id.iv_back -> {
                isExit()
            }
        }
    }

    private var timer: Timer? = null
    private var seconds = 0
    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
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

    @SuppressLint("CheckResult", "FileEndsWithExt")
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
                        val requestOptions = RequestOptions().apply {
                            error(R.drawable.home_gray_place_holder)
                            placeholder(R.drawable.home_gray_place_holder)
                        }
                        //  查找当前sd卡路径，是否展示图片还是灰色的颜色，不管相册还是sdcard的，都是和当前的sn相关的，但是相册里面的可能被删除。
                        //  需要从相册里面读取图片，如果没有图片，就展示灰色的图片
                        applyForAuthority {
                            if (!it) {
                                Glide.with(this@CameraActivity)
                                    .load("")
                                    .apply(requestOptions)
                                    .into(binding.ivThumbnail)
                                return@applyForAuthority
                            }
                            if (mViewModel.getPartsInfo.value?.data?.storageModel == 0) {
                                // 表示是从内存卡里面读取的
                                val picPath = findFirstImageInDir(createFileDir())

                                Glide.with(this@CameraActivity)
                                    .load(picPath)
                                    .apply(requestOptions)
                                    .into(binding.ivThumbnail)
                            } else {
                                val list = fetchImagesAndVideosFromSpecificFolder()
                                list.firstOrNull { path -> path.absolutePath.endsWith(".jpg") || path.absolutePath.endsWith(".jpeg") || path.absolutePath.endsWith(".png") }?.let { path ->
                                    Glide.with(this@CameraActivity)
                                        .load(path)
                                        .apply(requestOptions)
                                        .into(binding.ivThumbnail)
                                } ?: Glide.with(this@CameraActivity)
                                    .load("")
                                    .apply(requestOptions)
                                    .into(binding.ivThumbnail)
                            }
                        }

                    }
                }

                // 是否关闭门
                TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_DOOR -> {
                    val isOpen = value.toString() == "true"
                    val isPrivate = mViewModel.getAccessoryInfo.value?.data?.privateModel == true
                    if (isOpen) {
                        // 开门，打开隐私模式
                        devId?.let { tuYaUtils.publishDps(it, DPConstants.PRIVATE_MODE, true) }
                    } else if (!isPrivate) {
                        // 关门，如果不是隐私模式就关闭
                        devId?.let { tuYaUtils.publishDps(it, DPConstants.PRIVATE_MODE, false) }
                    }
                    devId?.let {
                        tuYaUtils.listenDPUpdate(it, DPConstants.PRIVATE_MODE, callback = object : TuyaCameraUtils.DPCallback {
                            override fun callback(obj: Any) {
                                logI("123123@: ${obj.toString()}")
                                ViewUtils.setVisible(obj.toString() == "true", binding.tvPrivacyMode)
                            }
                        })
                    }
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
        // 0 是手机， 1 是相册
        // 根据后台来返回的参数来判断是否保存在相册中还是sd卡中
        return mViewModel.getAccessoryInfo.value?.data?.storageModel == 0
    }

    /**
     * 存储到相册
     */
    private fun saveFileToGallery(context: Context, filePath: String, title: String, mimeType: String, albumName: String): Uri? {
        val file = File(filePath)
        if (!file.exists()) return null

        val isVideo = mimeType.startsWith("video")
        val contentUri = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val directory = Environment.DIRECTORY_PICTURES

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, title)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$directory/$albumName")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(contentUri, contentValues)

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


    /**
     * 从相册中读取图片或者视频
     */
    private fun fetchImagesAndVideosFromSpecificFolder(): List<File> {
        val file = File(getAlbumDir())
        val files = file.listFiles { dir, name ->
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".mp4")
        }
        return files?.toList() ?: emptyList()
    }

    // 创建文件夹，并且返回文件夹路径
    // 这是本地路径
    private fun createFileDir(): String {
        FileUtil.createDirIfNotExists(SDCard.getCacheDir(this@CameraActivity) + File.separator + mViewModel.sn.value)
        // 文件路径
        return SDCard.getCacheDir(this@CameraActivity) + File.separator + mViewModel.sn.value
    }

    // 返回相册的文件夹路径
    private fun getAlbumDir(): String {
        return Environment.getExternalStorageDirectory().toString() + "/Pictures/" + mViewModel.sn.value
    }

    override fun onBackPressed() {
        isExit()
    }

    private fun isExit() {
        adapters?.focusedPosition?.let { adapters?.getLetter(it) }?.apply {
            if (binding.ivCameraButton.isChecked) {
                if (this == "TIME-LAPSE" || this == "PHOTO") {
                    this@CameraActivity.finish()
                } else {
                    com.cl.common_base.widget.toast.ToastUtil.shortShow("Please stop the current mode first")
                }
            } else {
                this@CameraActivity.finish()
            }
        } ?: finish()
    }

    /**
     * 涂鸦摄像头帮助类
     */
    private val tuYaUtils by lazy {
        TuyaCameraUtils()
    }

    private val iTuyaDevice by lazy {
        ThingHomeSdk.newDeviceInstance(devId)
    }

    // 创建一个映射，将每个标签映射到对应的背景资源
    private val backgrounds = mapOf(
        "TIME-LAPSE" to com.cl.common_base.R.drawable.create_camera_time_line,
        "VIDEO" to com.cl.common_base.R.drawable.create_camera_video,
        "PHOTO" to com.cl.common_base.R.drawable.create_camera_photo,
        "MIC" to com.cl.common_base.R.drawable.create_camera_mic,
        "PLAYBACK" to com.cl.common_base.R.drawable.create_camera_record
    )
}