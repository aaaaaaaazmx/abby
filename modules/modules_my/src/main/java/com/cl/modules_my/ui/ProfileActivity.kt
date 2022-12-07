package com.cl.modules_my.ui

import android.Manifest
import android.app.PictureInPictureUiState
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyProfileActivityBinding
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.viewmodel.ProfileViewModel
import com.cl.modules_my.widget.ChooserOptionPop
import com.cl.modules_my.widget.LoginOutPop
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.lxj.xpopup.XPopup
import com.permissionx.guolindev.PermissionX
import com.tuya.smart.android.user.api.ILogoutCallback
import com.tuya.smart.home.sdk.TuyaHomeSdk
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject


/**
 * 个人信息界面
 */
@AndroidEntryPoint
class ProfileActivity : BaseActivity<MyProfileActivityBinding>() {

    @Inject
    lateinit var mViewModel: ProfileViewModel

    // 用户信息
    private val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    /**
     * 确认退出弹窗
     */
    private val confirm by lazy {
        XPopup.Builder(this@ProfileActivity)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
            .asCustom(LoginOutPop(this) {
                TuyaHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                    override fun onSuccess() {
                        // 清除缓存数据
                        Prefs.removeKey(Constants.Login.KEY_LOGIN_DATA_TOKEN)
                        // 清除上面所有的Activity
                        // 跳转到Login页面
                        ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            .navigation()
                    }

                    override fun onError(code: String?, error: String?) {
                        logE(
                            """
                           logout -> onError:
                            code: $code
                            error: $error
                        """.trimIndent()
                        )
                        ToastUtil.shortShow(error)
                        Reporter.reportTuYaError("getUserInstance", error, code)
                    }
                })
            })
    }

    private val modifyUserDetailReq by lazy {
        ModifyUserDetailReq()
    }

    /**
     * 选择照片还是拍照
     */
    private val chooserOptionPop by lazy {
        XPopup.Builder(this@ProfileActivity)
            .hasStatusBar(true)
            .isDestroyOnDismiss(false)
            .asCustom(
                ChooserOptionPop(
                    context = this@ProfileActivity,
                    onPhotoAction = {
                        PermissionHelp().applyPermissionHelp(
                            this@ProfileActivity,
                            getString(com.cl.common_base.R.string.profile_request_camera),
                            object : PermissionHelp.OnCheckResultListener{
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
                        // 选择照片
                        // 选择照片，不显示角标
                        val style = PictureSelectorStyle()
                        val ss = BottomNavBarStyle()
                        ss.isCompleteCountTips = false
                        style.bottomBarStyle = ss
                        PictureSelector.create(this@ProfileActivity)
                            .openGallery(SelectMimeType.ofImage())
                            .setImageEngine(GlideEngine.createGlideEngine())
//                            .setCompressEngine(ImageFileCompressEngine()) //是否压缩
                            .setSandboxFileEngine(MeSandboxFileEngine()) // Android10 沙盒文件
                            .isOriginalControl(false)// 原图功能
                            .isDisplayTimeAxis(true)// 资源轴
                            .setEditMediaInterceptListener(null)// 是否开启图片编辑功能
                            .isMaxSelectEnabledMask(true) // 是否显示蒙层
                            .isDisplayCamera(false)//是否显示摄像
                            .setLanguage(LanguageConfig.ENGLISH) //显示英语
                            .setMaxSelectNum(1)
                            .setSelectorUIStyle(style)
                            .forResult(PictureConfig.CHOOSE_REQUEST)
                    })
            )
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        // 设置nikeName
        binding.ftNickName.setItemValueWithColor(userInfo?.nickName, "#000000")
        // 设置邮箱
        binding.ftEmail.itemValue = userInfo?.email
        // 设置abbyID
        binding.ftId.itemValue = userInfo?.abbyId
        binding.ftId.setHideArrow(true)
        // 设置头像
        // 设置头像是否显示
        val headUrl = userInfo?.avatarPicture ?: userInfo?.userDetailData?.avatarPicture
        if (headUrl.isNullOrEmpty()) {
            binding.ftHead.setTvItemImage(userInfo?.nickName?.substring(0, 1))
        } else {
            binding.ftHead.setImageForUrl(headUrl, true)
        }
        // 文字加粗
        binding.ftHead.setItemTitle(getString(com.cl.common_base.R.string.profile_photo), true)
        binding.ftNickName.setItemTitle(getString(com.cl.common_base.R.string.profile_name), true)
        binding.ftId.setItemTitle(getString(com.cl.common_base.R.string.profile_abby_id), true)
        binding.ftEmail.setItemTitle(getString(com.cl.common_base.R.string.profile_email), true)
    }

    override fun observe() {
        mViewModel.apply {
            // 上传头像结果回调
            uploadImg.observe(this@ProfileActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    binding.ftHead.setImageForUrl(data, true)

                    // 更新用户信息
                    // 上传的头像地址需要截取
                    if (data.isNullOrEmpty()) return@success
                    data?.let {
                        val oneArray = it.split("com/")
                        if (oneArray.isNotEmpty()) {
                            if (oneArray.isNotEmpty()) {
                                val result = oneArray[1].split("?")
                                if (result.isNotEmpty()) {
                                    logI(result[0])
                                    // 更新用户信息
                                    modifyUserDetailReq.avatarPicture = result[0]
                                    mViewModel.modifyUserDetail(modifyUserDetailReq)
                                }
                            }
                        }
                    }
                }
                error { msg, code ->
                    hideProgressLoading()
                    msg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
                loading {
                    showProgressLoading()
                }
            })

            /**
             * 修改用户信息
             */
            modifyUserDetail.observe(this@ProfileActivity, resourceObserver {
                success {
                    hideProgressLoading()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
                loading {
                    hideProgressLoading()
                }
            })

            /**
             * 获取用户基本数据
             */
            userDetail.observe(this@ProfileActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    // 设置nikeName
                    binding.ftNickName.setItemValueWithColor(data?.nickName, "#000000")
                    // 设置头像
                    val headUrl = data?.avatarPicture ?: ""
                    if (headUrl.isNullOrEmpty()) {
                        binding.ftHead.setTvItemImage(userInfo?.nickName?.substring(0, 1))
                    } else {
                        binding.ftHead.setImageForUrl(headUrl, true)
                    }
                    // 设置abbyID
                    binding.ftId.itemValue = data?.abbyId

                    // 缓存赋值
                    userInfo?.userDetailData = data
                }
                loading { }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
            })

        }
    }

    override fun initData() {
        // 退出
        binding.tvLoginOut.setOnClickListener {
            confirm.show()
        }

        binding.ftHead.setOnClickListener {
            chooserOptionPop.show()
        }

        binding.ftNickName.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditNickNameActivity::class.java)
            intent.putExtra(KEY_NICK_NAME, binding.ftNickName.itemValue)
            startActivity(intent)
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
                SDCard.getContextPictureDir(this@ProfileActivity)
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
                    gotoClipActivity(imageUri)
                }
            }

            REQUEST_CROP_PHOTO -> {
                // 根据返回的URi
                if (resultCode == RESULT_OK && data != null) {
                    val uri = data.data
                    uri?.let {
                        val cropImagePath = getRealFilePathFromUri(applicationContext, uri)
                        // 上传一张图片
                        // 图片类型（head-头像、trend-动态）
                        mViewModel.uploadImg(upLoadImage(cropImagePath ?: ""))
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
        }
        runOnUiThread {
            // 展示图片
            val media = result[0]
            val path = media.availablePath
            if (PictureMimeType.isContent(path)) {
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

    private fun gotoClipActivity(uri: Uri?) {
        if (uri == null) {
            return
        }
        val intent = Intent(this, ClipImageActivity::class.java)
        val bundle = Bundle()
        bundle.putInt("type", 1)
        bundle.putString("uri", uri.toString())
        intent.putExtras(bundle)
        startActivityForResult(intent, REQUEST_CROP_PHOTO)
    }

    /**
     * 表单提交
     */
    private fun upLoadImage(path: String): List<MultipartBody.Part> {
        //1.创建MultipartBody.Builder对象
        val builder = MultipartBody.Builder()
            //表单类型
            .setType(MultipartBody.FORM)

        //2.获取图片，创建请求体
        val file: File = File(path)

        //表单类x型
        //表单类型
        val body: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)

        //3.调用MultipartBody.Builder的addFormDataPart()方法添加表单数据
        /**
         * ps:builder.addFormDataPart("code","123456");
         * ps:builder.addFormDataPart("file",file.getName(),body);
         */
        builder.addFormDataPart("imgType", "head") //传入服务器需要的key，和相应value值
        builder.addFormDataPart("file", file.name, body) //添加图片数据，body创建的请求体

        //4.创建List<MultipartBody.Part> 集合，
        //  调用MultipartBody.Builder的build()方法会返回一个新创建的MultipartBody
        //  再调用MultipartBody的parts()方法返回MultipartBody.Part集合
        return builder.build().parts
    }

    override fun onResume() {
        super.onResume()
        // 获取用户信息
        mViewModel.userDetail()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 刷新缓存
        GSON.toJson(userInfo)
            ?.let {
                logI("refreshData: $it")
                Prefs.putStringAsync(Constants.Login.KEY_LOGIN_DATA, it)
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
    }

}