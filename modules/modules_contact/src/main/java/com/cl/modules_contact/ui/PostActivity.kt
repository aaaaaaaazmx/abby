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
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.ChooserOptionPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.SoftInputUtils
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.common_base.widget.edittext.bean.MentionUser
import com.cl.common_base.widget.edittext.listener.EditDataListener
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.ItemTouchHelp
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ChooserAdapter
import com.cl.modules_contact.databinding.ContactPostActivityBinding
import com.cl.common_base.widget.decoraion.FullyGridLayoutManager
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.util.Collections
import javax.inject.Inject

/**
 * 发帖
 */
@AndroidEntryPoint
@Route(path = RouterPath.Contact.PAGE_TREND)
class PostActivity : BaseActivity<ContactPostActivityBinding>() {

    @Inject
    lateinit var viewModel: PostViewModel

    private val chooserAdapter by lazy {
        ChooserAdapter(mutableListOf(), onItemLongClick = { holder, position, view ->
            if (holder.itemViewType == ChoosePicBean.KEY_TYPE_PIC) {
                mItemTouchHelper?.startDrag(holder)
            }
        })
    }

    // 分享的类型，主要是针对种植完成的分享
    // 默认为空，表示是正常的分享，种植完成的分享就是"plant_complete"
    private val shareType by lazy {
        intent.getStringExtra(Constants.Global.KEY_SHARE_TYPE)
    }

    // 带过来的文字
    private val shareContent by lazy {
        intent.getStringExtra(Constants.Global.KEY_SHARE_TEXT)
    }

    // 需要分享的图片
    private val shareImg by lazy {
        intent.getStringExtra(Constants.Global.KEY_SHARE_CONTENT)
    }

    // 图片列表
    private val picList by lazy {
        val list = mutableListOf<ChoosePicBean>()
        val choosePicBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = "")
        list.add(choosePicBean)
        list
    }

    private var mItemTouchHelper: ItemTouchHelper? = null


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
            // 绑定拖拽事件
            val help = ItemTouchHelp(chooserAdapter)
            help.setOnItemSwapListener { fromPosition, toPosition ->
                Collections.swap(picList, fromPosition, toPosition)
                viewModel.picAddress.value?.let { Collections.swap(it, fromPosition, toPosition) }
            }
            mItemTouchHelper = ItemTouchHelper(help)
            mItemTouchHelper?.attachToRecyclerView(this)

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

        // 是否勾选数据
        binding.shareToPublic.isItemChecked = viewModel.shareToPublic
        binding.plantToVisible.isItemChecked = viewModel.plantDataIsVisible
        if (!viewModel.shareToPublic) {
            ViewUtils.setVisible(false, binding.plantToVisible, binding.peopleAt)
        }

        // 如果是种植完成跳转到这边来，需要直接添加一张图
        shareTypeOperation()
    }

    /**
     * 种植成功分昂操作
     */
    private fun shareTypeOperation() {
        shareType?.let {
            binding.etConnect.setText(shareContent ?: "")
            // 本地添加一个，网络地址也添加一个,适配器上也添加一个
            val chooseBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = shareImg, isUploading = true)
            picList.add(0, chooseBean)
            chooserAdapter.addData(0, chooseBean)
            // 直接上传
            picList[0].picAddress?.let { upLoadImage(it) }?.let { viewModel.uploadImg(it) }
        }
    }
    override fun observe() {
        viewModel.apply {
            // 上传图片回调
            uploadImg.observe(this@PostActivity, resourceObserver {
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

            // 发帖回调
            addData.observe(this@PostActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 放进去
                    Prefs.putBoolean(Constants.Contact.KEY_SHARE_TO_PUBLIC, binding.shareToPublic.isItemChecked)
                    Prefs.putBoolean(Constants.Contact.KEY_PLANT_DATA_IS_VISIBLE, binding.plantToVisible.isItemChecked)

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
        // 隐藏和显示
        binding.shareToPublic.setSwitchCheckedChangeListener { buttonView, isChecked ->
            ViewUtils.setVisible(isChecked, binding.plantToVisible, binding.peopleAt)
        }

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
                        },
                        onDeleteAction = {
                            // tds
                            binding.optionTds.itemValue = null
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
                        txt = if (binding.tvLink.text.toString() == "Add Link") "" else binding.tvLink.text.toString(),
                        onConfirmAction = { txt ->
                            // 超链接
                            binding.tvLink.text = txt
                        })
                ).show()
        }

        binding.textView.setOnClickListener { finish() }

        binding.btnPost.setOnClickListener {
            if (chooserAdapter.data.any { it.isUploading == true }) {
                ToastUtil.shortShow("Please wait for the picture to finish uploading")
                return@setOnClickListener
            }

            if (isFastClick()) {
                // 所有内容都是空的，
                if (picList.size == 1 && TextUtils.isEmpty(binding.etConnect.text.toString())) {
                    ToastUtil.shortShow("Cannot post when empty")
                    return@setOnClickListener
                }
                // @的人
                val mentions: MutableList<Mention> = mutableListOf()
                binding.etConnect.formatResult?.userList?.forEach {
                    mentions.add(Mention(it.abbyId, it.name, it.picture, it.id))
                }

                // 图片是空的，但是有文字
                if (picList.size == 1) {
                    // 没有图片，直接发帖
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
                } else {
                    // 直接发帖
                    viewModel.add(
                        AddTrendReq(
                            content = if (TextUtils.isEmpty(binding.etConnect.text.toString())) null else binding.etConnect.text.toString(),
                            imageUrls = viewModel.picAddress.value,
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
            } else {
                logI("2312312313")
            }
        }

        binding.peopleAt.setOnClickListener {
            // 需要删除之后取消勾选，取消勾选之后，需要删除@的人
            // 首先需要查看当前的@的人，是否和保存的是否一致，有可能用户已经删除了
            val userList = binding.etConnect.formatResult?.userList ?: mutableListOf()
            val alreadyList = viewModel.selectFriends.value ?: mutableListOf()
            if (userList.isEmpty()) {
                viewModel.setSelectFriendsClear()
            } else {
                // 判断他们的size 是否一致
                if (userList.size == alreadyList?.size) {
                    // 那么就不用管
                } else {
                    // 找出他们之间不同的，并且在alreadyList中删除他
                    viewModel.findDifferentItems(alreadyList, userList).forEach {
                        viewModel.serSelectFriendsRemove(it)
                    }
                }
            }

            // @人 跳转到联系人列表
            XPopup.Builder(this@PostActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .autoOpenSoftInput(false)
                .moveUpToKeyboard(false)
                .autoFocusEditText(false)
                .asCustom(
                    ContactListPop(this@PostActivity,
                        alreadyCheckedData = viewModel.selectFriends.value ?: mutableListOf(),
                        onConfirmAction = {
                            //  插入@的人
                            // 当保存的人没有，那么说明是第一次插入
                            if (viewModel.selectFriends.value?.isEmpty() == true) {
                                it.forEach { mentionData ->
                                    val index: Int = binding.etConnect.selectionStart
                                    binding.etConnect.editableText.insert(index, "@")
                                    binding.etConnect.insert(MentionUser(mentionData.userId ?: "", mentionData.nickName ?: "", mentionData.abbyId ?: "", mentionData.nickName ?: "", mentionData.picture ?: ""))
                                }
                            } else {
                                // 在第二次插入时，需要判断是插入还是删除
                                // 在勾选之后取消、需要删除相对应的人，那么userList.size > it.size
                                if ((binding.etConnect.formatResult?.userList?.size ?: 0) > it.size) {
                                    // 删除当前的length
                                    viewModel.findDifferentItemForuserList(it, binding.etConnect.formatResult?.userList).forEach { userList ->
                                        // 需要删除当前的userList
                                        binding.etConnect.remove(MentionUser(userList.id ?: "", userList.name ?: "", userList.abbyId ?: "", userList.name ?: "", userList.picture ?: ""))
                                    }
                                } else {
                                    // 插入用户贵
                                    viewModel.findDifferentItems(it, binding.etConnect.formatResult?.userList).forEach { mentionData ->
                                        val index: Int = binding.etConnect.selectionStart
                                        binding.etConnect.editableText.insert(index, "@")
                                        binding.etConnect.insert(MentionUser(mentionData.userId ?: "", mentionData.nickName ?: "", mentionData.abbyId ?: "", mentionData.nickName ?: "", mentionData.picture ?: ""))
                                    }
                                }
                            }
                            // 保存已经勾选的人
                            viewModel.setSelectFriends(it)
                        })
                ).show()
        }

    }


    /**
     * 表单提交
     * 需要循环上传
     */
    private fun upLoadImage(path: String): List<MultipartBody.Part> {
        //1.创建MultipartBody.Builder对象
        val builder = MultipartBody.Builder()
            //表单类型
            .setType(MultipartBody.FORM)

        //2.获取图片，创建请求体
        val file = File(path)
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
                    this@PostActivity.chooserAdapter.removeAt(position)
                    viewModel.clearPicAddress()
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
                            OnSrcViewUpdateListener { _, _ -> },
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
                    val chooseBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = cropImagePath, isUploading = true)
                    picList.add(0, chooseBean)
                    chooserAdapter.addData(0, chooseBean)
                    // 直接上传
                    picList[0].picAddress?.let { upLoadImage(it) }?.let { viewModel.uploadImg(it) }
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
                picList[0].picAddress?.let { upLoadImage(it) }?.let { viewModel.uploadImg(it) }
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