package com.cl.modules_contact.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.ChooserOptionPop
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ChooserAdapter
import com.cl.modules_contact.databinding.ContactPostActivityBinding
import com.cl.modules_contact.decoraion.FullyGridLayoutManager
import com.cl.modules_contact.decoraion.GridSpaceItemDecoration
import com.cl.modules_contact.decoraion.GridSpacingItemDecoration
import com.cl.modules_contact.pop.ContactListPop
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
import com.luck.picture.lib.utils.ToastUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.XPopupUtils
import dagger.hilt.android.AndroidEntryPoint
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
        }

        binding.etConnect.doAfterTextChanged {
            binding.tvEms.text = "${it?.length}/140"
        }
    }

    override fun observe() {
    }

    override fun initData() {

        initAdapter()
        initClick()
    }

    private fun initClick() {

        binding.peopleAt.setOnClickListener {
            // @人 跳转到联系人列表
            XPopup.Builder(this@PostActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .moveUpToKeyboard(false)
                .autoOpenSoftInput(false)
                .asCustom(
                    ContactListPop(this@PostActivity,
                        onConfirmAction = {
                            // todo 插入话题
                        })
                ).show()
        }

    }

    private fun initAdapter() {

        chooserAdapter.addChildClickViewIds(R.id.iv_pic_add, R.id.img_contact_pic_delete)
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
                                                        .setMaxSelectNum(9)
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
                                                        .setMaxSelectNum(9)
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
                    if (this@PostActivity.chooserAdapter.data.size == 8) {
                        this@PostActivity.chooserAdapter.addData(adapter.data.size, ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                    }
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
            REQUEST_CAPTURE -> {
                if (resultCode == RESULT_OK && imageUri != null) {
                    if (picList.size + 1 > 10) {
                        chooserAdapter.removeAt(0)
                        return
                    } else if (1 + picList.size == 10) {
                        // 隐藏加号。
                        chooserAdapter.removeAt(chooserAdapter.data.size - 1)
                    }
                    // gotoClipActivity(imageUri)
                    val cropImagePath = getRealFilePathFromUri(applicationContext, imageUri)
                    val chooseBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = cropImagePath)
                    picList.add(0, chooseBean)
                    chooserAdapter.addData(0, chooseBean)
                }
            }

            REQUEST_CROP_PHOTO -> {
                // 根据返回的URi
                if (resultCode == RESULT_OK && data != null) {
                    val uri = data.data
                    uri?.let {
                        // 上传一张图片
                        // 图片类型（head-头像、trend-动态）
                        // mViewModel.uploadImg(upLoadImage(cropImagePath ?: ""))
                    }
                    // 获取到路径
                }
            }

            PictureConfig.CHOOSE_REQUEST -> {
                val result = PictureSelector.obtainSelectorList(data)
                if (result.isNullOrEmpty()) return
                analyticalSelectResults(result)
            }
        }
    }

    /**
     * 选择照片返回结果
     */
    private fun analyticalSelectResults(result: ArrayList<LocalMedia>) {
        if (result.size + picList.size > 10) {
            chooserAdapter.removeAt(0)
            return
        } else if (result.size + picList.size == 10) {
            // 隐藏加号。
            chooserAdapter.removeAt(chooserAdapter.data.size - 1)
        }
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