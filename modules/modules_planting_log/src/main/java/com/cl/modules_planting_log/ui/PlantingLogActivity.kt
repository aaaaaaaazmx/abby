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
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.adapter.ChooserAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.ChoosePicBean
import com.cl.common_base.bean.ImageUrl
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.temperatureConversion
import com.cl.common_base.ext.unitsConversion
import com.cl.common_base.ext.weightConversion
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.ChooserOptionPop
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.common_base.widget.ItemTouchHelp
import com.cl.common_base.widget.decoraion.FullyGridLayoutManager
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_planting_log.adapter.CustomViewGroupAdapter
import com.cl.modules_planting_log.databinding.PlantingActionActivityBinding
import com.cl.modules_planting_log.databinding.PlantingLogActivityBinding
import com.cl.modules_planting_log.request.FieldAttributes
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import com.cl.modules_planting_log.viewmodel.PlantingLogAcViewModel
import com.cl.modules_planting_log.widget.CustomViewGroup
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import dagger.hilt.android.AndroidEntryPoint
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.util.Collections
import javax.inject.Inject


/**
 * 种植日志记录页面
 */
@AndroidEntryPoint
class PlantingLogActivity : BaseActivity<PlantingLogActivityBinding>() {

    @Inject
    lateinit var viewModel: PlantingLogAcViewModel

    private val maps by lazy {
        mapOf(
            "logTime" to FieldAttributes("Date*", "", "", CustomViewGroup.TYPE_CLASS_TEXT),
            "spaceTemp" to FieldAttributes("Space Temp(ST)", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, metricUnits = "°C", imperialUnits = "℉"),
            "waterTemp" to FieldAttributes("Water Temp (WT)", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, metricUnits = "°C", imperialUnits = "℉"),
            "humidity" to FieldAttributes("Humidity (RH)", "", "%", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL),
            "ph" to FieldAttributes("PH", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL),
            "tdsEc" to FieldAttributes("TDS", "", "PPM", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL),
            "plantHeight" to FieldAttributes("Height (HT)", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, metricUnits = "cm", imperialUnits = "In"),
            /*"vpd" to FieldAttributes("VPD", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL),*/
            /* "driedWeight" to FieldAttributes("Yield (Dried weight)", "", if (viewModel.isMetric) "g" else "Oz", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL),
             "wetWeight" to FieldAttributes("Yield (Wet weight)", "", if (viewModel.isMetric) "g" else "Oz", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL),*/
            /*"lightingSchedule" to FieldAttributes("Lighting Schedule", "", "", CustomViewGroup.TYPE_CLASS_TEXT),*/
            "lightingOn" to FieldAttributes("Lighting On", "", "", CustomViewGroup.TYPE_CLASS_TEXT),
            "lightingOff" to FieldAttributes("Lighting Off", "", "", CustomViewGroup.TYPE_CLASS_TEXT),
            "co2Concentration" to FieldAttributes("CO2 Concentration", "", "PPM", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL),
        )
    }

    /**
     * 日志适配器
     */
    private val logAdapter by lazy {
        CustomViewGroupAdapter(
            this@PlantingLogActivity,
            listOf(
                "logTime", "spaceTemp", "waterTemp", "humidity", "ph", "tdsEc",
                "plantHeight", "lightingOn", "lightingOff", "co2Concentration"
            ),
            listOf(
                "logTime", "lightingOn", "lightingOff"
            ),
            maps,
        )
    }

    private val plantInfoData by lazy {
        intent.getSerializableExtra("plantInfoData") as? PlantInfoByPlantIdData
    }

    // 植物ID， 用于新增日志
    private val plantId by lazy {
        intent.getStringExtra("plantId")
    }

    // logId 用于修改和查询日志
    private val logId by lazy {
        intent.getStringExtra("logId")
    }

    // period 用户新增时传递的周期参数
    private val period by lazy {
        intent.getStringExtra("period")
    }

    // 是否是新增的or编辑的。
    private val isAdd by lazy {
        intent.getBooleanExtra("isAdd", true)
    }

    /**
     * 本地记录，和后台返回的分开
     * 上传也是上传本地的，以本地的为准
     */
    private val picList by lazy {
        val list = mutableListOf<ChoosePicBean>()
        val choosePicBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = "")
        list.add(choosePicBean)
        list
    }

    // 交换
    private var mItemTouchHelper: ItemTouchHelper? = null
    private val chooserAdapter by lazy {
        ChooserAdapter(mutableListOf(), onItemLongClick = { holder, position, view ->
            if (holder.itemViewType == ChoosePicBean.KEY_TYPE_PIC) {
                mItemTouchHelper?.startDrag(holder)
            }
        })
    }

    private fun handleSaveOrUpdateLog() {
        if (chooserAdapter.data.any { it.isUploading == true }) {
            ToastUtil.shortShow("Please wait for the picture to finish uploading")
            return
        }
        if (binding.ftTrend.isItemChecked) {
            val notes = binding.etNote.text
            val picListUrl = viewModel.picAddress.value ?: mutableListOf()

            if (picListUrl.isEmpty() || notes.isNullOrEmpty()) {
                ToastUtil.show("To synchronize with the trend, you need to upload photos and fill in notes.")
                binding.ftTrend.isItemChecked = false
                return
            }
        }

        val logSaveOrUpdateReq = logAdapter.getLogData()
        logSaveOrUpdateReq.period = period
        logSaveOrUpdateReq.inchMetricMode = viewModel.getLogById.value?.data?.inchMetricMode
        logSaveOrUpdateReq.syncPost = binding.ftTrend.isItemChecked
        updateNotes(logSaveOrUpdateReq)
        updatePhotos(logSaveOrUpdateReq)
        updateUnit(logSaveOrUpdateReq, viewModel.isMetric, true)
        showProgressLoading()
        if (logId.isNullOrEmpty()) {
            createNewLog(logSaveOrUpdateReq)
        } else {
            modifyExistingLog(logSaveOrUpdateReq)
        }
    }

    private fun updateNotes(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logSaveOrUpdateReq.notes = binding.etNote.text.toString()
    }

    private fun updatePhotos(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        kotlin.runCatching {
            logSaveOrUpdateReq.plantPhoto = viewModel.picAddress.value?.map {
                if (it.imageUrl?.startsWith("http") == true) {
                    val url = it.imageUrl
                    val searchString = "user/trend/"
                    val startIndex = url?.indexOf(searchString)
                    if (startIndex != -1) {
                        val endIndex = startIndex?.let { it1 -> url.indexOf(".jpeg", it1) }?.plus(5) // 5 is the length of ".jpeg"
                        val result = endIndex?.let { it1 -> url.substring(startIndex, it1) }
                        result
                    } else url
                } else it.imageUrl
            }?.toMutableList()
        }
    }

    private fun updateUnit(logSaveOrUpdateReq: LogSaveOrUpdateReq, isMetric: Boolean, isUpload: Boolean) {
        logSaveOrUpdateReq.logTime = if (isUpload) logSaveOrUpdateReq.logTime else DateHelper.formatTime(logSaveOrUpdateReq.logTime?.toLongOrNull() ?: System.currentTimeMillis(), CustomViewGroupAdapter.KEY_FORMAT_TIME)
        /*logSaveOrUpdateReq.spaceTemp = temperatureConversion(logSaveOrUpdateReq.spaceTemp?.toFloatOrNull() ?: 0f, isMetric, isUpload)
        logSaveOrUpdateReq.waterTemp = temperatureConversion(logSaveOrUpdateReq.waterTemp?.toFloatOrNull() ?: 0f, isMetric, isUpload)
        logSaveOrUpdateReq.plantHeight = unitsConversion(logSaveOrUpdateReq.plantHeight?.toFloatOrNull() ?: 0f, isMetric, isUpload)
        logSaveOrUpdateReq.driedWeight = weightConversion((logSaveOrUpdateReq.driedWeight?.toFloatOrNull() ?: 0f), isMetric, isUpload)
        logSaveOrUpdateReq.wetWeight = weightConversion((logSaveOrUpdateReq.wetWeight?.toFloatOrNull() ?: 0f), isMetric, isUpload)*/
    }

    private fun createNewLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logI("新增日志")
        logSaveOrUpdateReq.plantId = plantId
        //  Insert code for creating a new log entry
        viewModel.saveOrUpdateLog(logSaveOrUpdateReq)
        logI("Log Details: $logSaveOrUpdateReq")
    }

    private fun modifyExistingLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logI("修改日志")
        logSaveOrUpdateReq.logId = logId
        // Insert code for modifying an existing log entry
        viewModel.saveOrUpdateLog(logSaveOrUpdateReq)
        logI("Log Details: $logSaveOrUpdateReq")
    }

    override fun PlantingLogActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@PlantingLogActivity
            plantInfoData = this@PlantingLogActivity.plantInfoData
            model = this@PlantingLogActivity.viewModel
            executePendingBindings()
        }
    }

    override fun initView() {
        binding.title
            .setRightButtonTextBack(R.drawable.background_check_tags_r5)
            .setRightButtonText("Save")
            .setRightButtonTextSize(13f)
            .setRightButtonTextHeight(25f)
            .setRightButtonTextColor(Color.WHITE)
            .setRightClickListener { handleSaveOrUpdateLog() }

        // 日志适配
        binding.rvLog.adapter = logAdapter

        // 图片适配
        binding.rvPic.layoutManager = LinearLayoutManager(this@PlantingLogActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPic.apply {
            layoutManager = FullyGridLayoutManager(
                this@PlantingLogActivity,
                4, GridLayoutManager.VERTICAL, false
            )
            addItemDecoration(
                GridSpaceItemDecoration(
                    4,
                    DensityUtil.dip2px(this@PlantingLogActivity, 4f), DensityUtil.dip2px(this@PlantingLogActivity, 1f)
                )
            )
            // 绑定拖拽事件
            val help = ItemTouchHelp(chooserAdapter)
            help.setOnItemSwapListener { fromPosition, toPosition ->
                Collections.swap(picList, fromPosition, toPosition)
                viewModel.picAddress.value?.let { Collections.swap(it, fromPosition, toPosition) }
            }
            mItemTouchHelper = ItemTouchHelper(help)
            mItemTouchHelper?.attachToRecyclerView(this)

            adapter = this@PlantingLogActivity.chooserAdapter
            this@PlantingLogActivity.chooserAdapter.setList(picList)

            // 拖拽
            /* isNestedScrollingEnabled = true
             val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(this@PlantingLogActivity.chooserAdapter)
             val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
             itemTouchHelper.attachToRecyclerView(this)
             this@PlantingLogActivity.chooserAdapter.enableDragItem(itemTouchHelper, R.id.iv_chooser_select, true)*/
        }

        // 新增的才显示
        ViewUtils.setVisible(isAdd, binding.ftTrend)

        // 获取日志详情
        viewModel.getLogById(logId)
    }

    override fun observe() {
        viewModel.apply {
            // 获取日志详情
            getLogById.observe(this@PlantingLogActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (null == data) return@success
                    data?.let {
                        // 后台返回的都是英制，那么转换就需要根据用户选中的来判断了。
                        updateUnit(it, viewModel.isMetric, false)
                        maps.forEach { (field, value) ->
                            // 只针对默认显示为False的条目进行判断，为true的都是必须显示的。
                            val declaredFiled = it::class.java.getDeclaredField(field)
                            declaredFiled.isAccessible = true
                            val values = declaredFiled.get(it)?.toString()
                            // 转换公英制,有些是默认填的，那么就不需要转换
                            if (logAdapter.fieldsAttributes[field]?.unit?.isEmpty() == true) {
                                logAdapter.fieldsAttributes[field]?.unit = if (data?.inchMetricMode == "inch") logAdapter.fieldsAttributes[field]?.imperialUnits.toString() else logAdapter.fieldsAttributes[field]?.metricUnits.toString()
                            }
                        }
                        logAdapter.setData(it)
                        // 添加备注、添加照片、
                        binding.etNote.setText(it.notes)
                        binding.ftTrend.isItemChecked = it.syncPost == true
                        it.plantPhoto?.let { photoList ->
                            photoList.forEach { url ->
                                viewModel.setPicAddress(ImageUrl(imageUrl = url))
                                picList.add(0, ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = url))
                            }
                            chooserAdapter.setList(picList)
                        }
                    }
                }
            })

            // 修改日志、以及上传日志
            logSaveOrUpdate.observe(this@PlantingLogActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg) }
                success {
                    hideProgressLoading()
                    // 提交成功 or 修改成功
                    finish()
                }
            })

            // 上传图片回调
            uploadImg.observe(this@PlantingLogActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 上传成功,隐藏上传进度条
                    chooserAdapter.data.firstOrNull { it.isUploading == true }?.let {
                        it.isUploading = false
                        chooserAdapter.notifyItemChanged(0)
                    }

                    data?.forEach {
                        val oneArray = it.split("com/")
                        if (oneArray.isNotEmpty()) {
                            if (oneArray.isNotEmpty()) {
                                val result = oneArray[1].split("?")
                                if (result.isNotEmpty()) {
                                    logI(result[0])
                                    // 更新用户信息
                                    // 更新集合
                                    setPicAddress(ImageUrl(imageUrl = result[0]))
                                }
                            }
                        }
                    }
                }
            })
        }
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
                // val cropImagePath = getRealFilePathFromUri(applicationContext, imageUri)
                val chooseBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = path, isUploading = true)
                picList.add(0, chooseBean)
                chooserAdapter.addData(0, chooseBean)

                /*if (PictureMimeType.isContent(path)) {
                    // 调准到裁剪页面
                    gotoClipActivity(
                        Uri.parse(
                            path
                        )
                    )
                } else {
                    // 跳转到裁剪页面
                    gotoClipActivity(
                        Uri.parse(
                            media.path
                        )
                    )
                }*/

            }
        }

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
                SDCard.getContextPictureDir(this@PlantingLogActivity)
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
     * 结果返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // camera
            REQUEST_CAPTURE -> {
                if (resultCode == RESULT_OK && imageUri != null) {
                    // gotoClipActivity(imageUri)
                    val cropImagePath = getRealFilePathFromUri(applicationContext, imageUri)
                    val chooseBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = cropImagePath, isUploading = true)
                    picList.add(0, chooseBean)
                    chooserAdapter.addData(0, chooseBean)
                    // 直接上传
                    picList[0].picAddress?.let { viewModel.submitTheForm(it) }?.let { viewModel.uploadImg(it) }
                    if (chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size >= 9) {
                        // 移除最后一张加号
                        chooserAdapter.data.removeAt(chooserAdapter.data.size - 1)
                        picList.removeAt(picList.size - 1)
                    } else {

                    }
                }
            }

            // 选择照片
            PictureConfig.CHOOSE_REQUEST -> {
                val result = PictureSelector.obtainSelectorList(data)
                if (result.isNullOrEmpty()) return
                analyticalSelectResults(result)
                // 直接上传
                picList[0].picAddress?.let { viewModel.submitTheForm(it) }?.let { viewModel.uploadImg(it) }
                if (chooserAdapter.data.size == 10) {
                    chooserAdapter.removeAt(9)
                    picList.removeAt(9)
                }
            }
        }
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

    override fun initData() {
        chooserAdapter.addChildClickViewIds(R.id.iv_pic_add, R.id.img_contact_pic_delete, R.id.iv_chooser_select)
        chooserAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? ChoosePicBean
            when (view.id) {
                R.id.iv_pic_add -> {
                    if (viewModel.picAddress.value?.size == MAX_PHOTOS) {
                        ToastUtil.show("add Max 3 Photos")
                        return@setOnItemChildClickListener
                    }
                    // 添加图片
                    XPopup.Builder(this@PlantingLogActivity)
                        .hasStatusBar(true)
                        .isDestroyOnDismiss(false)
                        .asCustom(
                            ChooserOptionPop(
                                context = this@PlantingLogActivity,
                                onPhotoAction = {
                                    PermissionHelp().applyPermissionHelp(
                                        this@PlantingLogActivity,
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
                                            this@PlantingLogActivity,
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
                                                    PictureSelector.create(this@PlantingLogActivity)
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
                                            this@PlantingLogActivity,
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
                                                    PictureSelector.create(this@PlantingLogActivity)
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

                R.id.img_contact_pic_delete -> {
                    this@PlantingLogActivity.chooserAdapter.removeAt(position)
                    viewModel.clearPicAddress()
                    picList.removeAt(position)
                    // 在最后面添加到ADD
                    if (this@PlantingLogActivity.chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_ADD }.size == 1) {
                        return@setOnItemChildClickListener
                    } else {
                        this@PlantingLogActivity.chooserAdapter.addData(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                        picList.add(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                    }
                }

                R.id.iv_chooser_select -> {
                    val picList = mutableListOf<String?>()
                    chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.forEach {
                        picList.add(it.picAddress)
                    }
                    // 图片浏览
                    XPopup.Builder(this@PlantingLogActivity)
                        .asImageViewer(
                            (view as? ImageView),
                            position,
                            picList.toList(),
                            OnSrcViewUpdateListener { _, _ -> },
                            SmartGlideImageLoader()
                        )
                        .show()
                }
            }
        }

        binding.ftTrend.setSwitchCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val notes = binding.etNote.text
                val picListUrl = viewModel.picAddress.value ?: mutableListOf()

                if (picListUrl.isEmpty() || notes.isNullOrEmpty()) {
                    ToastUtil.show("To synchronize with the trend, you need to upload photos and fill in notes.")
                    binding.ftTrend.isItemChecked = false
                }
            }
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