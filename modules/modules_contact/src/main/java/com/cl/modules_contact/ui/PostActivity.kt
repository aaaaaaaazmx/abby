package com.cl.modules_contact.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.ChooserOptionPop
import com.cl.common_base.util.SoftInputUtils
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.common_base.widget.edittext.bean.MentionUser
import com.cl.common_base.widget.edittext.listener.EditDataListener
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ChooserAdapter
import com.cl.modules_contact.databinding.ContactPostActivityBinding
import com.cl.modules_contact.decoraion.FullyGridLayoutManager
import com.cl.modules_contact.decoraion.GridSpaceItemDecoration
import com.cl.modules_contact.pop.ContactLinkPop
import com.cl.modules_contact.pop.ContactListPop
import com.cl.modules_contact.pop.ContactPhPop
import com.cl.modules_contact.pop.TdsPop
import com.cl.modules_contact.request.AddTrendReq
import com.cl.modules_contact.request.ImageUrl
import com.cl.modules_contact.request.Mention
import com.cl.modules_contact.response.ChoosePicBean
import com.cl.modules_contact.viewmodel.PostViewModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

/**
 * 发帖
 */
@AndroidEntryPoint
class PostActivity : BaseActivity<ContactPostActivityBinding>() {

    @Inject
    lateinit var viewModel: PostViewModel

    private val chooserAdapter by lazy {
        ChooserAdapter(mutableListOf())
    }

    // 图片列表
    private val picList by lazy {
        val list = mutableListOf<ChoosePicBean>()
        val choosePicBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = "")
        list.add(choosePicBean)
        list
    }


    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.rvPic.apply {
            layoutManager = FullyGridLayoutManager(
                this@PostActivity,
                4, GridLayoutManager.VERTICAL, false
            )
            addItemDecoration(
                GridSpaceItemDecoration(
                    4,
                    DensityUtil.dip2px(this@PostActivity, 4f), DensityUtil.dip2px(this@PostActivity, 1f)
                )
            )
            adapter = this@PostActivity.chooserAdapter
            this@PostActivity.chooserAdapter.setList(picList)

            // 拖拽
           /* isNestedScrollingEnabled = true
            val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(this@PostActivity.chooserAdapter)
            val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
            itemTouchHelper.attachToRecyclerView(this)
            this@PostActivity.chooserAdapter.enableDragItem(itemTouchHelper, R.id.iv_chooser_select, true)*/
        }

        binding.etConnect.doAfterTextChanged {
            binding.tvEms.text = "${it?.length}/140"
        }
    }

    override fun observe() {
        viewModel.apply {
            // 上传图片回调
            uploadImg.observe(this@PostActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    showProgressLoading()
                }
                success {
                    val imageUrlsList = mutableListOf<ImageUrl>()
                    data?.forEach {
                        val oneArray = it.split("com/")
                        if (oneArray.isNotEmpty()) {
                            if (oneArray.isNotEmpty()) {
                                val result = oneArray[1].split("?")
                                if (result.isNotEmpty()) {
                                    logI(result[0])
                                    // 更新用户信息
                                    imageUrlsList.add(ImageUrl(imageUrl = result[0]))
                                }
                            }
                        }
                    }

                    // @的人
                    val mentions: MutableList<Mention> = mutableListOf()
                    binding.etConnect.formatResult?.userList?.forEach {
                        mentions.add(Mention(it.abbyId, it.name, it.picture, it.id))
                    }

                    // 图片上传成功之后，就是发帖
                    add(
                        AddTrendReq(
                            content = if (TextUtils.isEmpty(binding.etConnect.text.toString())) null else binding.etConnect.text.toString(),
                            imageUrls = imageUrlsList,
                            link = if (binding.tvLink.text.toString() == "Add Link") null else binding.tvLink.text.toString(),
                            mentions = mentions,
                            openData = if (binding.plantToVisible.isItemChecked) 1 else 0,
                            ph = viewModel.phValue.value,
                            syncTrend = if (binding.shareToPublic.isItemChecked) 1 else 0,
                            taskId = null,
                            tds = binding.optionTds.itemValue
                        )
                    )
                }
            })

            // 发帖回调
            addData.observe(this@PostActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 这个需要回调给Fragment，通知刷新界面
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            })
        }
    }

    override fun initData() {

        initAdapter()
        initClick()
    }

    private fun initClick() {

        binding.etConnect.editDataListener = object : EditDataListener {
            override fun onEditAddAt(str: String?, start: Int, length: Int) {
            }

            override fun onEditAddHashtag(start: Int) {
            }

            override fun onCloseSearchView() {
            }
        }

        binding.optionPh.setOnClickListener {
            XPopup.Builder(this@PostActivity)
                .dismissOnTouchOutside(true)
                .isDestroyOnDismiss(false)
                .asCustom(
                    ContactPhPop(this@PostActivity,
                        txt = if (TextUtils.isEmpty(viewModel.phValue.value)) 7.0f else viewModel.phValue.value?.toFloat(),
                        onConfirmAction = { phValue ->
                            // tds`
                            binding.optionPh.itemValue = phValue
                            viewModel.setPhValue(phValue)
                        })
                ).show()
        }

        // tds
        binding.optionTds.setOnClickListener {
            XPopup.Builder(this@PostActivity)
                .dismissOnTouchOutside(true)
                .isDestroyOnDismiss(false)
                .moveUpToKeyboard(true)
                .asCustom(
                    TdsPop(this@PostActivity,
                        txt = binding.optionTds.itemValue,
                        onConfirmAction = { txt ->
                            // tds
                            binding.optionTds.itemValue = txt
                        })
                ).show()
        }

        // 输入超链接
        binding.tvLink.setOnClickListener {
            // 超链接
            XPopup.Builder(this@PostActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .moveUpToKeyboard(true)
                .asCustom(
                    ContactLinkPop(
                        this@PostActivity,
                        onConfirmAction = { txt ->
                            // 超链接
                            binding.tvLink.text = txt
                        })
                ).show()
        }

        binding.textView.setOnClickListener { finish() }

        binding.btnPost.setOnClickListener {
            if (isFastClick()) {
                // 所有内容都是空的，
                if (picList.size == 1 && TextUtils.isEmpty(binding.etConnect.text.toString())) {
                    ToastUtil.shortShow("Cannot post when empty")
                    return@setOnClickListener
                }
                // 图片是空的，但是有文字
                if (picList.size == 1) {
                    // @的人
                    val mentions: MutableList<Mention> = mutableListOf()
                    binding.etConnect.formatResult?.userList?.forEach {
                        mentions.add(Mention(it.abbyId, it.name, it.picture, it.id))
                    }

                    // 图片上传成功之后，就是发帖
                    viewModel.add(
                        AddTrendReq(
                            content = if (TextUtils.isEmpty(binding.etConnect.text.toString())) null else binding.etConnect.text.toString(),
                            imageUrls = null,
                            link = if (binding.tvLink.text.toString() == "Add Link") null else binding.tvLink.text.toString(),
                            mentions = mentions,
                            openData = if (binding.plantToVisible.isItemChecked) 1 else 0,
                            ph = viewModel.phValue.value,
                            syncTrend = if (binding.shareToPublic.isItemChecked) 1 else 0,
                            taskId = null,
                            tds = binding.optionTds.itemValue
                        )
                    )
                    return@setOnClickListener
                }

                // 上传图片 && 发帖
                val addType = picList.firstOrNull { it.type == ChoosePicBean.KEY_TYPE_ADD }
                if (addType == null) {
                    viewModel.uploadImg(upLoadImage(picList))
                } else {
                    picList.filter { it.type != ChoosePicBean.KEY_TYPE_ADD }.apply {
                        viewModel.uploadImg(upLoadImage(this.toMutableList()))
                    }
                }
            } else {
                logI("2312312313")
            }
        }

        binding.peopleAt.setOnClickListener {
            // @人 跳转到联系人列表
            XPopup.Builder(this@PostActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .autoOpenSoftInput(false)
                .moveUpToKeyboard(false)
                .autoFocusEditText(false)
                .asCustom(
                    ContactListPop(this@PostActivity,
                        onConfirmAction = {
                            //  插入@的人
                            it.forEach { mentionData ->
                                val index: Int = binding.etConnect.selectionStart
                                binding.etConnect.editableText.insert(index, "@")
                                binding.etConnect.insert(MentionUser(mentionData.userId ?: "", mentionData.nickName ?: "", mentionData.abbyId ?: "", mentionData.nickName ?: "", mentionData.picture ?: ""))
                            }
                        })
                ).show()
        }

    }

    /**
     * 表单提交
     * 多张表单提交
     */
    private fun upLoadImage(path: MutableList<ChoosePicBean>): List<MultipartBody.Part> {
        //1.创建MultipartBody.Builder对象
        val builder = MultipartBody.Builder()
            //表单类型
            .setType(MultipartBody.FORM)

        path.forEach { bean ->
            //2.获取图片，创建请求体
            bean.picAddress?.let {
                val file = File(it)
                //表单类x型
                //表单类型
                val body: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)

                //3.调用MultipartBody.Builder的addFormDataPart()方法添加表单数据
                /**
                 * ps:builder.addFormDataPart("code","123456");
                 * ps:builder.addFormDataPart("file",file.getName(),body);
                 */
                builder.addFormDataPart("imgType", "trend") //传入服务器需要的key，和相应value值
                builder.addFormDataPart("files", file.name, body) //添加图片数据，body创建的请求体
            }
        }
        //4.创建List<MultipartBody.Part> 集合，
        //  调用MultipartBody.Builder的build()方法会返回一个新创建的MultipartBody
        //  再调用MultipartBody的parts()方法返回MultipartBody.Part集合
        return builder.build().parts
    }


    private fun initAdapter() {
        chooserAdapter.addChildClickViewIds(R.id.iv_pic_add, R.id.img_contact_pic_delete, R.id.iv_chooser_select)
        chooserAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? ChoosePicBean
            when (view.id) {
                R.id.iv_pic_add -> {
                    // 添加图片
                    XPopup.Builder(this@PostActivity)
                        .hasStatusBar(true)
                        .isDestroyOnDismiss(false)
                        .asCustom(
                            ChooserOptionPop(
                                context = this@PostActivity,
                                onPhotoAction = {
                                    PermissionHelp().applyPermissionHelp(
                                        this@PostActivity,
                                        getString(com.cl.common_base.R.string.profile_request_camera),
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
                                            this@PostActivity,
                                            getString(com.cl.common_base.R.string.profile_request_photo),
                                            object : PermissionHelp.OnCheckResultListener {
                                                override fun onResult(result: Boolean) {
                                                    if (!result) return
                                                    // 选择照片
                                                    // 选择照片，不显示角标
                                                    val style = PictureSelectorStyle()
                                                    val ss = BottomNavBarStyle()
                                                    ss.isCompleteCountTips = false
                                                    style.bottomBarStyle = ss
                                                    PictureSelector.create(this@PostActivity)
                                                        .openGallery(SelectMimeType.ofImage())
                                                        .setImageEngine(GlideEngine.createGlideEngine())
                                                        //                            .setCompressEngine(ImageFileCompressEngine()) //是否压缩
                                                        .setSandboxFileEngine(MeSandboxFileEngine()) // Android10 沙盒文件
                                                        .isOriginalControl(false) // 原图功能
                                                        .isDisplayTimeAxis(true) // 资源轴
                                                        .setEditMediaInterceptListener(null) // 是否开启图片编辑功能
                                                        .isMaxSelectEnabledMask(true) // 是否显示蒙层
                                                        .isDisplayCamera(false) //是否显示摄像
                                                        .setLanguage(LanguageConfig.ENGLISH) //显示英语
                                                        .setMaxSelectNum(9 - chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size)
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
                                            this@PostActivity,
                                            getString(com.cl.common_base.R.string.profile_request_photo),
                                            object : PermissionHelp.OnCheckResultListener {
                                                override fun onResult(result: Boolean) {
                                                    if (!result) return
                                                    // 选择照片
                                                    // 选择照片，不显示角标
                                                    val style = PictureSelectorStyle()
                                                    val ss = BottomNavBarStyle()
                                                    ss.isCompleteCountTips = false
                                                    style.bottomBarStyle = ss
                                                    PictureSelector.create(this@PostActivity)
                                                        .openGallery(SelectMimeType.ofImage())
                                                        .setImageEngine(GlideEngine.createGlideEngine())
                                                        //                            .setCompressEngine(ImageFileCompressEngine()) //是否压缩
                                                        .setSandboxFileEngine(MeSandboxFileEngine()) // Android10 沙盒文件
                                                        .isOriginalControl(false) // 原图功能
                                                        .isDisplayTimeAxis(true) // 资源轴
                                                        .setEditMediaInterceptListener(null) // 是否开启图片编辑功能
                                                        .isMaxSelectEnabledMask(true) // 是否显示蒙层
                                                        .isDisplayCamera(false) //是否显示摄像
                                                        .setLanguage(LanguageConfig.ENGLISH) //显示英语
                                                        .setMaxSelectNum(9 - chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size)
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
                    this@PostActivity.chooserAdapter.removeAt(position)
                    picList.removeAt(position)
                    // 在最后面添加到ADD
                    if (this@PostActivity.chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_ADD }.size == 1) {
                        return@setOnItemChildClickListener
                    } else {
                        this@PostActivity.chooserAdapter.addData(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                        picList.add(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                    }
                }

                R.id.iv_chooser_select -> {
                    val picList = mutableListOf<String?>()
                    chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.forEach {
                        picList.add(it.picAddress)
                    }
                    // 图片浏览
                    XPopup.Builder(this@PostActivity)
                        .asImageViewer(
                            (view as? ImageView),
                            position,
                            picList.toList(),
                            OnSrcViewUpdateListener { _, _ ->  },
                            SmartGlideImageLoader()
                        )
                        .show()
                }
            }
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
                SDCard.getContextPictureDir(this@PostActivity)
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
                    val chooseBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = cropImagePath)
                    picList.add(0, chooseBean)
                    chooserAdapter.addData(0, chooseBean)

                    if (chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size >= 9) {
                        // 移除最后一张加号
                        chooserAdapter.data.removeAt(chooserAdapter.data.size - 1)
                        picList.removeAt(picList.size - 1)
                    } else {

                    }

                    /* if (picList.size + 1 > 10) {
                         chooserAdapter.removeAt(0)
                         picList.removeAt(0)
                         return
                     } else if (1 + picList.size == 10) {
                         // 隐藏加号。
                         chooserAdapter.removeAt(chooserAdapter.data.size - 1)
                         picList.removeAt(chooserAdapter.data.size - 1)
                     }*/
                }
            }

            // 选择照片
            PictureConfig.CHOOSE_REQUEST -> {
                val result = PictureSelector.obtainSelectorList(data)
                if (result.isNullOrEmpty()) return
                analyticalSelectResults(result)
                if (chooserAdapter.data.size == 10) {
                    chooserAdapter.removeAt(9)
                    picList.removeAt(9)
                }
            }
        }
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
                val chooseBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = path)
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
     * 快速点击
     */
    // 两次点击间隔不能少于1000ms
    private val FAST_CLICK_DELAY_TIME = 1000
    private var lastClickTime: Long = 0

    private fun isFastClick(): Boolean {
        var flag = true
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime <= FAST_CLICK_DELAY_TIME) {
            flag = false
        }
        lastClickTime = currentClickTime
        return flag
    }

    override fun onResume() {
        super.onResume()
        logI("onResume")
        SoftInputUtils.hideSoftInput(this@PostActivity)
    }

    override fun onStart() {
        super.onStart()
        logI("onStart")
        SoftInputUtils.hideSoftInput(this@PostActivity)
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
    }
}