package com.cl.modules_planting_log.ui

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.trackPipAnimationHintView
import androidx.core.content.FileProvider
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.ImageUrl
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.ChooserOptionPop
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_planting_log.adapter.CustomViewGroupAdapter
import com.cl.modules_planting_log.adapter.EditTextValueChangeListener
import com.cl.modules_planting_log.databinding.PlantingTrainActivityBinding
import com.cl.modules_planting_log.request.CardInfo
import com.cl.modules_planting_log.request.FieldAttributes
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import com.cl.modules_planting_log.request.PlantLogTypeBean
import com.cl.modules_planting_log.viewmodel.PlantingLogAcViewModel
import com.cl.modules_planting_log.widget.CustomViewGroup
import com.cl.modules_planting_log.widget.PlantChooseLogTypePop
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.lxj.xpopup.util.XPopupUtils
import dagger.hilt.android.AndroidEntryPoint
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class PlantingTrainActivity : BaseActivity<PlantingTrainActivityBinding>(), EditTextValueChangeListener {
    @Inject
    lateinit var viewModel: PlantingLogAcViewModel

    // 植物ID， 用于新增日志
    private val plantId by lazy {
        intent.getStringExtra("plantId")
    }

    private val plantInfoData by lazy {
        intent.getSerializableExtra("plantInfoData") as? PlantInfoByPlantIdData
    }

    // logId 用于修改和查询日志
    private val logId by lazy {
        intent.getStringExtra("logId")
    }

    // period 用户新增时传递的周期参数
    private val period by lazy {
        intent.getStringExtra("period")
    }

    // showType,用于请求日志类型列表
    private val showType by lazy {
        intent.getStringExtra("showType") ?: CardInfo.TYPE_TRAINING_CARD
    }

    // 是否是新增的
    private val isAdd by lazy {
        intent.getBooleanExtra("isAdd", true)
    }

    // 属性数组
    private val maps by lazy {
        mapOf(
            "logTime" to FieldAttributes("Date*", "", "", CustomViewGroup.TYPE_CLASS_TEXT),
            "logType" to FieldAttributes("Training Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT),
            "waterType" to FieldAttributes("Water Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "volume" to FieldAttributes("Volume", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, false, metricUnits = "L", imperialUnits = "Gal"),
            "feedingType" to FieldAttributes("Feeding Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "repellentType" to FieldAttributes("Repellent Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "declareDeathType" to FieldAttributes("DeclareDeath Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "driedWeight" to FieldAttributes("Yield (Dried weight)", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, false, metricUnits = "g", imperialUnits = "Oz"),
            "wetWeight" to FieldAttributes("Yield (Wet weight)", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, false, metricUnits = "g", imperialUnits = "Oz"),
        )
    }

    /**
     * 日志适配器
     */
    private val logAdapter by lazy {
        CustomViewGroupAdapter(
            this@PlantingTrainActivity,
            listOf(
                "logTime", "logType", "waterType", "volume", "feedingType", "repellentType", "declareDeathType", "driedWeight", "wetWeight"
            ),
            listOf(
                "logTime", "logType", "waterType", "feedingType", "repellentType", "declareDeathType"
            ),
            maps,
            this@PlantingTrainActivity
        )
    }


    override fun initView() {
        binding.title
            .setRightButtonTextBack(R.drawable.background_check_tags_r5)
            .setRightButtonText("Save")
            .setRightButtonTextSize(13f)
            .setRightButtonTextHeight(25f)
            .setRightButtonTextColor(Color.WHITE)
            .setRightClickListener { handleSaveOrUpdateLog() }

        // 请求日志类型列表
        viewModel.getLogTypeList(showType, logId)

        binding.rvLog.adapter = logAdapter
    }

    /**
     * 修改或者保存Action
     */
    private fun handleSaveOrUpdateLog() {
        val logSaveOrUpdateReq = logAdapter.getLogData()
        if (logSaveOrUpdateReq.logType.isNullOrEmpty()) {
            ToastUtil.shortShow("Please select the Training type")
            return
        }
        logSaveOrUpdateReq.plantId = plantId
        logSaveOrUpdateReq.period = period
        logSaveOrUpdateReq.logId = logId
        logSaveOrUpdateReq.notes = binding.etNote.text.toString()
        updatePhotos(logSaveOrUpdateReq)
        updateUnit(logSaveOrUpdateReq, viewModel.isMetric, true)
        if (logId.isNullOrEmpty()) {
            createNewLog(logSaveOrUpdateReq)
        } else {
            modifyExistingLog(logSaveOrUpdateReq)
        }
    }

    private fun updatePhotos(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logSaveOrUpdateReq.trainingBeforePhoto = extractPhotoPath(viewModel.beforePicAddress.value)
        logSaveOrUpdateReq.trainingAfterPhoto = extractPhotoPath(viewModel.afterPicAddress.value)
    }

    // todo 截取有问题
    private fun extractPhotoPath(inputUrl: String?): String? {
        inputUrl?.let { url ->
            val urlArray = url.split("--------")
            val targetUrl = if (urlArray.size > 1) urlArray[1] else urlArray[0]
            if (targetUrl.startsWith("http")) {
                val prefix = "https://heyabbytest.s3.us-west-1.amazonaws.com/"
                val startIndex = prefix.length
                val endIndex = targetUrl.indexOf('?', startIndex) // 找到问号的位置，如果没有问号可以使用url.length
                return targetUrl.substring(startIndex, if (endIndex == -1) url.length else endIndex)
            }
            return targetUrl
        }
        logI("extractPhotoPath: $inputUrl")
        return inputUrl
    }


    private fun modifyExistingLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logI("修改日志")
        // Insert code for modifying an existing log entry
        viewModel.saveOrUpdateLog(logSaveOrUpdateReq)
        logI("Log Details: $logSaveOrUpdateReq")
    }

    private fun createNewLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logI("新增日志")
        //  Insert code for creating a new log entry
        viewModel.saveOrUpdateLog(logSaveOrUpdateReq)
        logI("Log Details: $logSaveOrUpdateReq")
    }

    private fun updateUnit(logSaveOrUpdateReq: LogSaveOrUpdateReq, isMetric: Boolean, isUpload: Boolean) {
        logSaveOrUpdateReq.logTime = if (isUpload) logSaveOrUpdateReq.logTime else DateHelper.formatTime(logSaveOrUpdateReq.logTime?.toLongOrNull() ?: System.currentTimeMillis(), CustomViewGroupAdapter.KEY_FORMAT_TIME)
        logSaveOrUpdateReq.logType =
            if (isUpload) viewModel.getLogTypeList.value?.data?.toList()?.firstOrNull { it.showUiText == logSaveOrUpdateReq.logType }?.logType ?: "" else viewModel.getLogTypeList.value?.data?.toList()?.firstOrNull { it.logType == logSaveOrUpdateReq.logType }?.showUiText ?: ""
    }

    override fun observe() {
        viewModel.apply {
            // 上传图片回调
            uploadImg.observe(this@PlantingTrainActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    data?.forEach {
                        val oneArray = it.split("com/")
                        if (oneArray.isNotEmpty()) {
                            if (oneArray.isNotEmpty()) {
                                val result = oneArray[1].split("?")
                                if (result.isNotEmpty()) {
                                    logI(result[0])
                                    // 更新用户信息
                                    // 更新集合
                                    // setPicAddress(ImageUrl(imageUrl = result[0]))
                                    if (chooserTips.value == true) setBeforeAddress((viewModel.beforePicAddress.value ?: "").plus("--------${result[0]}")) else setAfterAddress((viewModel.afterPicAddress.value ?: "").plus("--------${result[0]}"))
                                }
                            }
                        }
                    }
                }
            })

            logSaveOrUpdate.observe(this@PlantingTrainActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 提交成功 or 修改成功
                    finish()
                }
            })

            getLogById.observe(this@PlantingTrainActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 获取日志详情信息
                    if (null == data) return@success
                    data?.let {
                        updateUnit(it, viewModel.isMetric, false)
                        // 展示和隐藏条目
                        maps.forEach { (field, value) ->
                            // 只针对默认显示为False的条目进行判断，为true的都是必须显示的。
                            val declaredFiled = it::class.java.getDeclaredField(field)
                            declaredFiled.isAccessible = true
                            val values = declaredFiled.get(it)?.toString()
                            if (!value.isVisible) {
                                val mapValue = logAdapter.logTypeMap[it.logType]
                                if (mapValue.isNullOrEmpty()) {
                                    logAdapter.fieldsAttributes[field]?.isVisible = !values.isNullOrEmpty()
                                } else {
                                    // 找到相对应的mapValue，判断是否相等
                                    logAdapter.fieldsAttributes[field]?.isVisible = mapValue.contains(field)
                                }
                            }
                            if (logAdapter.fieldsAttributes[field]?.unit?.isEmpty() == true) {
                                // 转换公英制
                                if (logAdapter.fieldsAttributes[field]?.unit?.isEmpty() == true) {
                                    logAdapter.fieldsAttributes[field]?.unit = if (data?.inchMetricMode == "inch") logAdapter.fieldsAttributes[field]?.imperialUnits.toString() else logAdapter.fieldsAttributes[field]?.metricUnits.toString()
                                }
                            }
                        }
                        logAdapter.setData(it)
                        // 添加备注
                        binding.etNote.setText(it.notes)
                        // 添加两张图片、
                        it.trainingBeforePhoto?.let { setBeforeAddress(it) }
                        it.trainingAfterPhoto?.let { setAfterAddress(it) }
                    }
                }
            })

            getLogTypeList.observe(this@PlantingTrainActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 请求日志详情
                    viewModel.getLogById(logId)
                }
            })
        }
    }

    override fun initData() {
        clickViewAction()
    }

    override fun PlantingTrainActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@PlantingTrainActivity
            viewModel = this@PlantingTrainActivity.viewModel
            plantInfoData = this@PlantingTrainActivity.plantInfoData
            executePendingBindings()
        }
    }

    private fun showChooserTips() {
        // 添加图片
        XPopup.Builder(this@PlantingTrainActivity)
            .hasStatusBar(true)
            .isDestroyOnDismiss(false)
            .asCustom(
                ChooserOptionPop(
                    context = this@PlantingTrainActivity,
                    onPhotoAction = {
                        PermissionHelp().applyPermissionHelp(
                            this@PlantingTrainActivity,
                            getString(R.string.profile_request_camera),
                            object : PermissionHelp.OnCheckResultListener {
                                override fun onResult(result: Boolean) {
                                    if (!result) return
                                    //跳转到调用系统相机
                                    gotoCamera()
                                }
                            },
                            Manifest.permission.CAMERA
                        )
                    },
                    onLibraryAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            PermissionHelp().applyPermissionHelp(
                                this@PlantingTrainActivity,
                                getString(R.string.profile_request_photo),
                                object : PermissionHelp.OnCheckResultListener {
                                    override fun onResult(result: Boolean) {
                                        if (!result) return
                                        // 选择照片
                                        // 选择照片，不显示角标
                                        val style = PictureSelectorStyle()
                                        val ss = BottomNavBarStyle()
                                        ss.isCompleteCountTips = false
                                        style.bottomBarStyle = ss
                                        PictureSelector.create(this@PlantingTrainActivity)
                                            .openGallery(SelectMimeType.ofImage())
                                            .setImageEngine(GlideEngine.createGlideEngine())
                                            .setCompressEngine(CompressFileEngine { context, source, call ->
                                                Luban.with(context).load(source).ignoreBy(100)
                                                    .setCompressListener(object : OnNewCompressListener {
                                                        override fun onSuccess(source: String?, compressFile: File?) {
                                                            call?.onCallback(source, compressFile?.absolutePath)
                                                        }

                                                        override fun onError(source: String?, e: Throwable?) {
                                                            call?.onCallback(source, null)
                                                        }

                                                        override fun onStart() {

                                                        }
                                                    }).launch();
                                            })
                                            .setSandboxFileEngine(MeSandboxFileEngine()) // Android10 沙盒文件
                                            .isOriginalControl(false) // 原图功能
                                            .isDisplayTimeAxis(true) // 资源轴
                                            .setEditMediaInterceptListener(null) // 是否开启图片编辑功能
                                            .isMaxSelectEnabledMask(true) // 是否显示蒙层
                                            .isDisplayCamera(false) //是否显示摄像
                                            .setLanguage(LanguageConfig.ENGLISH) //显示英语
                                            .setMaxSelectNum(1)
                                            .setSelectorUIStyle(style)
                                            .forResult(PictureConfig.CHOOSE_REQUEST)
                                    }
                                },
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.READ_MEDIA_AUDIO,
                            )
                        } else {
                            PermissionHelp().applyPermissionHelp(
                                this@PlantingTrainActivity,
                                getString(R.string.profile_request_photo),
                                object : PermissionHelp.OnCheckResultListener {
                                    override fun onResult(result: Boolean) {
                                        if (!result) return
                                        // 选择照片
                                        // 选择照片，不显示角标
                                        val style = PictureSelectorStyle()
                                        val ss = BottomNavBarStyle()
                                        ss.isCompleteCountTips = false
                                        style.bottomBarStyle = ss
                                        PictureSelector.create(this@PlantingTrainActivity)
                                            .openGallery(SelectMimeType.ofImage())
                                            .setImageEngine(GlideEngine.createGlideEngine())
                                            .setCompressEngine(CompressFileEngine { context, source, call ->
                                                Luban.with(context).load(source).ignoreBy(100)
                                                    .setCompressListener(object : OnNewCompressListener {
                                                        override fun onSuccess(source: String?, compressFile: File?) {
                                                            call?.onCallback(source, compressFile?.absolutePath)
                                                        }

                                                        override fun onError(source: String?, e: Throwable?) {
                                                            call?.onCallback(source, null)
                                                        }

                                                        override fun onStart() {

                                                        }
                                                    }).launch();
                                            })
                                            .setSandboxFileEngine(MeSandboxFileEngine()) // Android10 沙盒文件
                                            .isOriginalControl(false) // 原图功能
                                            .isDisplayTimeAxis(true) // 资源轴
                                            .setEditMediaInterceptListener(null) // 是否开启图片编辑功能
                                            .isMaxSelectEnabledMask(true) // 是否显示蒙层
                                            .isDisplayCamera(false) //是否显示摄像
                                            .setLanguage(LanguageConfig.ENGLISH) //显示英语
                                            .setMaxSelectNum(1)
                                            .setSelectorUIStyle(style)
                                            .forResult(PictureConfig.CHOOSE_REQUEST)
                                    }
                                },
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                            )
                        }
                    })
            ).show()
    }

    /**
     * 跳转相机
     */
    private var imageUri: Uri? = null
    private fun gotoCamera() {
        imageUri = createImageUri()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_CAPTURE)
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private fun createImageUri(): Uri? {
        //Android 10以上
        val photoUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val status = Environment.getExternalStorageState()
            // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            if (status == Environment.MEDIA_MOUNTED) {
                contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                )
            } else {
                contentResolver.insert(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                    ContentValues()
                )
            }
        } else {
            val tempFile: File = FileUtil.createFileIfNotExists(
                SDCard.getContextPictureDir(this@PlantingTrainActivity)
                    .toString() + File.separator + System.currentTimeMillis() + ".jpg"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider", tempFile
                )
            } else {
                Uri.fromFile(tempFile)
            }
        }
        return photoUri
    }

    /**
     * 根据uri获取文件路径
     */
    private fun getRealFilePathFromUri(context: Context, uri: Uri?): String? {
        if (null == uri) {
            return null
        }
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) {
            data = uri.path
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }

    /**
     * 选择照片返回结果
     */
    private fun analyticalSelectResults(result: ArrayList<LocalMedia>) {
        /* if (result.size + chooserAdapter.data.size == 10) {
             chooserAdapter.data.removeAt(9)
             picList.removeAt(9)
         }*/

        for (media in result) {
            if (media.width == 0 || media.height == 0) {
                // 如果是图片
                if (PictureMimeType.isHasImage(media.mimeType)) {
                    val imageExtraInfo = MediaUtils.getImageSize(this, media.path)
                    media.width = imageExtraInfo.width
                    media.height = imageExtraInfo.height
                } else if (PictureMimeType.isHasVideo(media.mimeType)) {
                    val videoExtraInfo = MediaUtils.getVideoSize(this, media.path)
                    media.width = videoExtraInfo.width
                    media.height = videoExtraInfo.height
                }
            }
            logI(
                """
                "文件名: " + ${media.fileName}
                "是否压缩:" + ${media.isCompressed}
                "压缩:" + ${media.compressPath}
                "初始路径:" + ${media.path}
                "绝对路径:" + ${media.realPath}
                "是否裁剪:" + ${media.isCut}
                "裁剪路径:" + ${media.cutPath}
                "是否开启原图:" + ${media.isOriginal}
                "原图路径:" + ${media.originalPath}
                "沙盒路径:" + ${media.sandboxPath}
                "水印路径:" + ${media.watermarkPath}
                "视频缩略图:" + ${media.videoThumbnailPath}
                "原始宽高: " + ${media.width} + "x" + ${media.height}
                "裁剪宽高: " + ${media.cropImageWidth} + "x" + ${media.cropImageHeight}
                "文件大小: " + ${PictureFileUtils.formatAccurateUnitFileSize(media.size)}
                "文件时长: " + ${media.duration}
                "有效路径: " + ${media.availablePath}
            """.trimIndent()
            )

            runOnUiThread {
                // 展示图片
                val path = media.availablePath
                // 判断当前是点击before还是after
                path?.let {
                    if (viewModel.chooserTips.value == true) {
                        viewModel.setBeforeAddress(it)
                    } else {
                        viewModel.setAfterAddress(it)
                    }
                    // 直接上传
                    it.let { viewModel.submitTheForm(it) }.let { url -> viewModel.uploadImg(url) }
                }
            }
        }

    }

    /**
     * 结果返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // camera
            REQUEST_CAPTURE -> {
                if (resultCode == RESULT_OK && imageUri != null) {
                    val cropImagePath = getRealFilePathFromUri(applicationContext, imageUri)
                    cropImagePath?.let {
                        if (viewModel.chooserTips.value == true) {
                            viewModel.setBeforeAddress(it)
                        } else {
                            viewModel.setAfterAddress(it)
                        }
                        // 直接上传
                        it.let { viewModel.submitTheForm(it) }.let { url -> viewModel.uploadImg(url) }
                    }
                }
            }

            // 选择照片
            PictureConfig.CHOOSE_REQUEST -> {
                val result = PictureSelector.obtainSelectorList(data)
                if (result.isNullOrEmpty()) return
                analyticalSelectResults(result)
            }
        }
    }

    /**
     * 点击方法
     */
    private fun clickViewAction() {
        binding.beforeImage.setOnClickListener { view ->
            viewModel.setChooserTips(true)
            val beforeAddress = viewModel.beforePicAddress.value
            if (beforeAddress.isNullOrEmpty()) {
                // 表示没有添加照片
                // 去添加照片
                showChooserTips()
            } else {
                // 查看照片
                // 图片浏览
                val urlArray = viewModel.beforePicAddress.value?.split("--------")
                kotlin.runCatching {
                    extracted(view, listOf(urlArray?.get(0)))
                }.onFailure {
                    extracted(view, listOf(viewModel.beforePicAddress.value))
                }
            }
        }

        binding.beforeClose.setOnClickListener {
            val beforeAddress = viewModel.beforePicAddress.value
            beforeAddress?.let {
                // 移除照片
                viewModel.setClearBeforeAddress()
            }
        }

        binding.afterImage.setOnClickListener { view ->
            viewModel.setChooserTips(false)
            val afterAddress = viewModel.afterPicAddress.value
            if (afterAddress.isNullOrEmpty()) {
                // 表示没有添加照片
                // 去添加照片
                showChooserTips()
            } else {
                // 查看照片
                val urlArray = viewModel.afterPicAddress.value?.split("--------")
                kotlin.runCatching {
                    extracted(view, listOf(urlArray?.get(0)))
                }.onFailure {
                    extracted(view, listOf(viewModel.afterPicAddress.value))
                }
            }
        }

        binding.afterClose.setOnClickListener {
            val afterAddress = viewModel.afterPicAddress.value
            afterAddress?.let {
                // 移除照片
                viewModel.setClearAfterAddress()
            }
        }
    }

    private fun extracted(it: View?, list: List<String?>) {
        XPopup.Builder(this@PlantingTrainActivity)
            .asImageViewer(
                (it as? ImageView),
                0,
                list,
                OnSrcViewUpdateListener { _, _ -> },
                SmartGlideImageLoader()
            )
            .show()
    }

    override fun onValueChanged(position: Int, newValue: String) {

    }

    override fun onEditTextClick(position: Int, editText: EditText, customViewGroup: CustomViewGroup) {
        // 转换成日志
        val typeList = viewModel.getLogTypeList.value?.data?.map { PlantLogTypeBean(it.showUiText, false) }?.toMutableList()
        // 弹出相对应的日志列表弹窗
        /*XPopup.Builder(this@PlantingTrainActivity).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
            .hasShadowBg(true) // 去掉半透明背景
            //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
            .atView(editText).isCenterHorizontal(false).asCustom(this@PlantingTrainActivity.let {
                PlantChooseLogTypePop(it,
                    list = typeList,
                    onConfirmAction = { txt ->
                        logAdapter.setData(position, editText, txt)
                    }).setBubbleBgColor(Color.WHITE) //气泡背景
                    .setArrowWidth(XPopupUtils.dp2px(this@PlantingTrainActivity, 6f)).setArrowHeight(
                        XPopupUtils.dp2px(
                            this@PlantingTrainActivity, 6f
                        )
                    ) //.setBubbleRadius(100)
                    .setArrowRadius(
                        XPopupUtils.dp2px(
                            this@PlantingTrainActivity, 3f
                        )
                    )
            }).show()*/
        val selectedLogType = editText.text.toString()
        val logTypeListDataItems = viewModel.getLogTypeList.value?.data?.map { item ->
            item.copy(isSelected = item.showUiText == selectedLogType)
        } ?: mutableListOf()

        (logTypeListDataItems as? MutableList<LogTypeListDataItem>)?.let {
            customViewGroup.setRvListData(
                it, true
            )
        }
    }

    companion object {
        // 请求相机
        private const val REQUEST_CAPTURE = 100

        //请求相册
        private const val REQUEST_PICK = 101

        // 裁剪之后返回
        private const val REQUEST_CROP_PHOTO = 102

        // 传递nickName
        const val KEY_NICK_NAME = "key_nick_name"

        private const val MAX_PHOTOS = 3
    }
}