package com.cl.modules_contact.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.signature.EmptySignature
import com.bumptech.glide.signature.ObjectKey
import com.bumptech.glide.util.Util
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.Gif
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.ImageUtil
import com.cl.common_base.widget.edittext.bean.MentionUser
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ChooserAdapter
import com.cl.modules_contact.databinding.ContactReelPostActivityBinding
import com.cl.modules_contact.decoraion.FullyGridLayoutManager
import com.cl.modules_contact.decoraion.GridSpaceItemDecoration
import com.cl.modules_contact.pop.ContactListPop
import com.cl.modules_contact.request.AddTrendReq
import com.cl.modules_contact.request.ImageUrl
import com.cl.modules_contact.request.Mention
import com.cl.modules_contact.response.ChoosePicBean
import com.cl.modules_contact.ui.pic.ChoosePicActivity
import com.cl.modules_contact.util.DeviceConstants
import com.cl.modules_contact.viewmodel.PostViewModel
import com.luck.picture.lib.utils.DensityUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import top.zibin.luban.Luban
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.lang.Float.min
import java.security.MessageDigest
import java.util.Collections
import javax.inject.Inject


/**
 * 生成Gif发布界面
 */
@AndroidEntryPoint
class ReelPostActivity : BaseActivity<ContactReelPostActivityBinding>() {

    @Inject
    lateinit var viewModel: PostViewModel

    // 图片列表
    private val picList by lazy {
        val list = mutableListOf<ChoosePicBean>()
        val choosePicBean = ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = "")
        list.add(choosePicBean)
        list
    }

    // 图片压缩列表
    private val compressPicList by lazy {
        mutableListOf<String>()
    }

    private val chooserAdapter by lazy {
        ChooserAdapter(mutableListOf(), onItemLongClick = { holder, position, view ->
        })
    }

    override fun initView() {
        binding.rvPic.apply {
            layoutManager = FullyGridLayoutManager(
                this@ReelPostActivity,
                4, GridLayoutManager.VERTICAL, false
            )
            addItemDecoration(
                GridSpaceItemDecoration(
                    4,
                    DensityUtil.dip2px(this@ReelPostActivity, 1f), DensityUtil.dip2px(this@ReelPostActivity, 1f)
                )
            )
            // 绑定拖拽事件
            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                    val itemViewType = viewHolder.itemViewType
                    /*if (itemViewType != ChoosePicBean.KEY_TYPE_ADD) {
                        viewHolder.itemView.alpha = 0.7f
                    }*/
                    return makeMovementFlags(
                        ItemTouchHelper.DOWN or ItemTouchHelper.UP
                                or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
                    )
                }

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    try {
                        //得到item原来的position
                        val fromPosition = viewHolder.absoluteAdapterPosition
                        //得到目标position
                        val toPosition = target.absoluteAdapterPosition
                        val itemViewType = target.itemViewType
                        if (itemViewType != ChoosePicBean.KEY_TYPE_ADD) {
                            if (fromPosition < toPosition) {
                                for (i in fromPosition until toPosition) {
                                    Collections.swap(chooserAdapter.data, i, i + 1)
                                }
                            } else {
                                for (i in fromPosition downTo toPosition + 1) {
                                    Collections.swap(chooserAdapter.data, i, i - 1)
                                }
                            }
                            Collections.swap(picList, fromPosition, toPosition)
                            chooserAdapter.notifyItemMoved(fromPosition, toPosition)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // 不处理滑动事件
                }
            })
            itemTouchHelper.attachToRecyclerView(this)
            adapter = this@ReelPostActivity.chooserAdapter
            this@ReelPostActivity.chooserAdapter.setList(picList)

            // 拖拽
            /* isNestedScrollingEnabled = true
             val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(this@ReelPostActivity.chooserAdapter)
             val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
             itemTouchHelper.attachToRecyclerView(this)
             this@ReelPostActivity.chooserAdapter.enableDragItem(itemTouchHelper, R.id.iv_chooser_select, true)*/
        }

        binding.etConnect.doAfterTextChanged {
            binding.tvEms.text = "${it?.length}/140"
        }

    }

    override fun observe() {
        viewModel.apply {
            // 发帖回调
            addData.observe(this@ReelPostActivity, resourceObserver {
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


            uploadImg.observe(this@ReelPostActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
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
                                    logI("setPicAddress: ${picAddress.value?.size}}")
                                    // 如果上传标记为true， 表示立即发布
                                    if (uploadImageFlag.value == true) {
                                        newPost()
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    override fun initData() {
        initAdapter()
        initClick()
    }

    private fun initAdapter() {
        chooserAdapter.addChildClickViewIds(R.id.iv_pic_add, R.id.img_contact_pic_delete, R.id.iv_chooser_select)
        chooserAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? ChoosePicBean
            when (view.id) {
                R.id.iv_pic_add -> {
                    // 添加图片
                    //  跳转到选中图片界面
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionHelp().applyPermissionHelp(
                            this@ReelPostActivity,
                            getString(com.cl.common_base.R.string.profile_request_photo),
                            object : PermissionHelp.OnCheckResultListener {
                                override fun onResult(result: Boolean) {
                                    if (!result) return
                                    val intent = Intent(this@ReelPostActivity, ChoosePicActivity::class.java)
                                    intent.putExtra(KEY_PIC_LIST_RESULT, picList.filter { it.type == ChoosePicBean.KEY_TYPE_PIC } as? Serializable)
                                    startActivityLauncher.launch(intent)
                                }
                            },
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO,
                        )
                    } else {
                        PermissionHelp().applyPermissionHelp(
                            this@ReelPostActivity,
                            getString(com.cl.common_base.R.string.profile_request_photo),
                            object : PermissionHelp.OnCheckResultListener {
                                override fun onResult(result: Boolean) {
                                    if (!result) return
                                    val intent = Intent(this@ReelPostActivity, ChoosePicActivity::class.java)
                                    intent.putExtra(KEY_PIC_LIST_RESULT, picList.filter { it.type == ChoosePicBean.KEY_TYPE_PIC } as? Serializable)
                                    startActivityLauncher.launch(intent)
                                }
                            },
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                        )
                    }
                }

                R.id.img_contact_pic_delete -> {
                    // 有改动，就需要删除上传的gif
                    viewModel.clearPicAddress()
                    picList.find { it.picAddress == chooserAdapter.getItem(position).picAddress }?.let {
                        picList.remove(it)
                        chooserAdapter.removeAt(position)
                        if (chooserAdapter.data.none { data -> data.type == ChoosePicBean.KEY_TYPE_ADD }) {
                            chooserAdapter.addData(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                            picList.add(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                        }
                    }
                }

                R.id.iv_chooser_select -> {
                    val picList = mutableListOf<String?>()
                    chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.forEach {
                        picList.add(it.picAddress)
                    }
                    // 图片浏览
                    XPopup.Builder(this@ReelPostActivity)
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


    private fun initClick() {

        binding.btnPost.setOnClickListener {
            showProgressLoading()
            // 如果没有图片返回true， 找到了返回false
            // 没有2张图片不能发帖
            if (!picList.none { it.type == ChoosePicBean.KEY_TYPE_PIC }) {
                hideProgressLoading()
                if (picList.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size < 2) {
                    ToastUtil.shortShow("You need at least two pictures.")
                    return@setOnClickListener
                }
            }

            // 同时也需要判断，当前是否上传过gif,判断当前没有gif，但是选择的照片又不为空
            if ((viewModel.picAddress.value?.size ?: 0) <= 0 && !picList.none { it.type == ChoosePicBean.KEY_TYPE_PIC }) {
                // 1、先生成gif、并且上传gif, 2、发布
                viewModel.setUploadImageFlag(true)
                generateGifAndUploadGif()
                return@setOnClickListener
            }
            // 3、 其他情况直接发布
            newPost()
        }


        // 合成gif
        binding.tvPreview.setOnClickListener {
            // 没有2张图片不能发帖
            if (!picList.none { it.type == ChoosePicBean.KEY_TYPE_PIC }) {
                if (picList.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size < 2) {
                    ToastUtil.shortShow("You need at least two pictures.")
                    return@setOnClickListener
                }
            }
            showProgressLoading()
            // 有改动，就需要删除上传的gif
            viewModel.clearPicAddress()
            generateGifAndUploadGif()
        }

        binding.textView.setOnClickListener { finish() }

        binding.cbOne.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.cbTwo.isChecked = false
                binding.cbThree.isChecked = false
                binding.cbOne.setTextColor(Color.WHITE)
                binding.cbTwo.setTextColor(Color.BLACK)
                binding.cbThree.setTextColor(Color.BLACK)
            }
            // 如果有图
            if (!picList.none { it.type == ChoosePicBean.KEY_TYPE_PIC } && picList.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size >= 2) {
                generateGifAndUploadGif(delayCheck = true)
            }
        }
        binding.cbTwo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.cbOne.isChecked = false
                binding.cbThree.isChecked = false
                binding.cbTwo.setTextColor(Color.WHITE)
                binding.cbOne.setTextColor(Color.BLACK)
                binding.cbThree.setTextColor(Color.BLACK)
            }
            // 如果有图
            if (!picList.none { it.type == ChoosePicBean.KEY_TYPE_PIC } && picList.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size >= 2) {
                generateGifAndUploadGif(delayCheck = true)
            }
        }
        binding.cbThree.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.cbTwo.isChecked = false
                binding.cbOne.isChecked = false
                binding.cbThree.setTextColor(Color.WHITE)
                binding.cbTwo.setTextColor(Color.BLACK)
                binding.cbOne.setTextColor(Color.BLACK)
            }
            // 如果有图
            if (!picList.none { it.type == ChoosePicBean.KEY_TYPE_PIC } && picList.filter { it.type == ChoosePicBean.KEY_TYPE_PIC }.size >= 2) {
                generateGifAndUploadGif(delayCheck = true)
            }
        }

        binding.clType.setOnClickListener {
            binding.typeBox.isChecked = !binding.typeBox.isChecked
        }

        binding.peopleAt.setOnClickListener {
            // @别人弹窗
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
            XPopup.Builder(this@ReelPostActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .autoOpenSoftInput(false)
                .moveUpToKeyboard(false)
                .autoFocusEditText(false)
                .asCustom(
                    ContactListPop(this@ReelPostActivity,
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
     * 生成gif 并且上传gif
     */
    @SuppressLint("CheckResult")
    private fun generateGifAndUploadGif(delayCheck: Boolean? = false, parm: ((status: Boolean) -> Unit)? = null) {
        FileUtil.deleteDirectory(DeviceConstants.getDialPhotoPath(this@ReelPostActivity))
        val sources: MutableList<Bitmap> = ArrayList()
        picList.forEachIndexed { index, choosePicBean ->
            if (choosePicBean.type == ChoosePicBean.KEY_TYPE_PIC) {
                var imageBitmap: Bitmap? = null
                // 需要区分是否是网络图片
                if (choosePicBean.picAddress?.contains("https") == true || choosePicBean.picAddress?.contains("http") == true) {
                    // 表示是网络图片，
                    val cacheKey = ObjectKey(choosePicBean.picAddress)
                    val cacheFile = DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(this@ReelPostActivity), DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong())
                        .get(cacheKey)
                    // 通过缓存转换成bitmap
                    if (cacheFile != null && cacheFile.exists()) {
                        imageBitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                        imageBitmap = conversionBitmap(imageBitmap, cacheFile.absolutePath)
                    } else {
                        // todo 如果没找到缓存，那么就下载图片，并且需要压缩, 其实能看到网络图片那么就完全不需要下载了。
                    }
                } else {
                    // 表示是本地图片
                    val uri = (choosePicBean.compressPicAddress ?: choosePicBean.picAddress)?.let { Uri.fromFile(File(it))}
                    imageBitmap = ImageUtil.rotationZoomingDecodeBitmap(this@ReelPostActivity, uri)
                    //                    imageBitmap = BitmapFactory.decodeFile(choosePicBean.picAddress)
                    //                    imageBitmap = conversionBitmap(imageBitmap, choosePicBean.picAddress)
                }
                // 转化你之后转换图片
                val imagePath = DeviceConstants.getDialPhotoPath(this@ReelPostActivity) + File.separator + "image" + index + ".png"
                ImageUtil.saveBitmap(imageBitmap, imagePath)
                imageBitmap?.let { it1 -> sources.add(it1) }
            }
        }

        FileUtil.deleteDirectory(DeviceConstants.getDialCustomGif(this@ReelPostActivity))
        FileUtil.createDirIfNotExists(DeviceConstants.getDialCustomGif(this@ReelPostActivity))
        // 保存gif路径
        val dialCustomGif = DeviceConstants.getDialCustomGif(this@ReelPostActivity) + System.currentTimeMillis() + ".gif"

        // 根据选择的时长
        val delayTime = if (binding.cbOne.isChecked) {
            300
        } else if (binding.cbTwo.isChecked) {
            500
        } else if (binding.cbThree.isChecked) {
            800
        } else {
            300 // Default delay time
        }
        // .setRepeat(1)
        Gif.Builder().setSources(sources).setNickName(if (binding.typeBox.isChecked) viewModel.userinfoBean?.nickName else null).setDestPath(dialCustomGif).setDelay(delayTime).start(object : Gif.ResultCallback {
            override fun onSuccess(destPath: String?) {
                hideProgressLoading()
                logI("pngToGif >> onSuccess")
                if (delayCheck == true || viewModel.uploadImageFlag.value == true) {
                    // 上传gif
                    viewModel.uploadImg(upLoadImage(dialCustomGif))
                    return
                }
                viewModel.uploadImg(upLoadImage(dialCustomGif))
                // 并不是什么时候都展示展示Gif图
                XPopup.Builder(this@ReelPostActivity).asImageViewer(
                    binding.ivPreview, destPath, SmartGlideImageLoader()
                ).show()

            }

            override fun onError(msg: String?) {
                hideProgressLoading()
                logI("pngToGif >> onFailure")
                ToastUtil.shortShow("pngToGif >> onFailure")
            }
        })
    }

    /**
     * 获取旋转信息
     */
    private fun getImageRotation(filePath: String): Int {
        val exifInterface = ExifInterface(filePath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    /**
     * bitmap\转换
     * 判断图片是否需要放大
     * imageWidth < 450 || imageHeight < 1124
     */
    private fun conversionBitmap(imageBitmap: Bitmap?, path: String?): Bitmap? {
        var imageBitmap1 = imageBitmap
        if (imageBitmap1?.width!! < 828 || imageBitmap1.height < 1124) {

        } else {
            // 需要缩小的
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight
            val scaleFactor = min(1f, min(828f / imageWidth, 1124f / imageHeight))
            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor.toInt()
            val bitmap = BitmapFactory.decodeFile(path, options)

            // 缩放图片
            val matrix = Matrix()
            //0.20535715
            logI("1231232: scale: $scaleFactor")
            matrix.postScale(scaleFactor, scaleFactor)
            imageBitmap1 = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true)
        }
        return imageBitmap1
    }

    private fun getOrientation(path: String?): Int {
        val exif = ExifInterface(path!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    /**
     * 直接发布
     */
    private fun newPost() {
        // @的人
        val mentions: MutableList<Mention> = mutableListOf()
        binding.etConnect.formatResult?.userList?.forEach {
            mentions.add(Mention(it.abbyId, it.name, it.picture, it.id))
        }
        // 直接发帖
        viewModel.add(
            AddTrendReq(
                content = if (TextUtils.isEmpty(binding.etConnect.text.toString())) null else binding.etConnect.text.toString(),
                imageUrls = if (viewModel.picAddress.value?.isEmpty() == true) null else viewModel.picAddress.value,
                mentions = mentions,
                openData = 0,
                syncTrend = 1,
                taskId = null,
            )
        )
    }

    /**
     * 回调刷新页面
     */
    private val startActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            // 有改动，就需要删除上传的gif
            viewModel.clearPicAddress()
            // 传递回来的照片集合
            val list = it.data?.getSerializableExtra(KEY_PIC_LIST) as? MutableList<*> ?: mutableListOf<String>()
            // 直接清空，然后在添加数据
            if (list.isEmpty()) return@registerForActivityResult
            lifecycleScope.launch {
                try {
                    // 压缩图片
                    val compressedList = withContext(Dispatchers.Default) {
                        list.map { data ->
                            Luban.with(this@ReelPostActivity).load(data.toString()).ignoreBy(100).get()
                        }
                    }
                    logI("compressedList: ${compressedList.size}")
                    picList.clear()
                    val listPic = withContext(Dispatchers.IO) {
                        list.map { listData ->
                            val compressData = compressedList.getOrNull(list.indexOf(listData))
                            ChoosePicBean(
                                type = ChoosePicBean.KEY_TYPE_PIC,
                                picAddress = listData?.toString(),
                                compressPicAddress = compressData?.get(0).toString()
                            )
                        } + ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD)
                    }.take(20)
                    picList.addAll(listPic)
                    chooserAdapter.setList(picList)
                    // [/storage/emulated/0/Android/data/com.cl.abby/cache/luban_disk_cache/1686532998062109.jpeg]
                    // BitmapFactory.decodeFile((picList[0].compressPicAddress.toString()))
                    // logI("123123:/ ${getImageRotation(picList[0].compressPicAddress!!)}")
                    //logI("123123:/ ${getImageRotation("/storage/emulated/0/Android/data/com.cl.abby/cache/luban_disk_cache/1686492285069403.jpeg")}")
                } catch (e: Exception) {
                    logI("Error: ${e.message}")
                }
            }
        }
    }

    /**
     * Glide缓存存储路径：/data/data/your_packagexxxxxxx/cache/image_manager_disk_cache
     * Glide文件名生成规则函数 : 4.0+ 版本
     *
     * @param url 图片地址url
     * @return 返回图片在磁盘缓存的key值
     */
    private fun getGlide4SafeKey(url: String): String? {
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val signature = EmptySignature.obtain()
            signature.updateDiskCacheKey(messageDigest)
            GlideUrl(url).updateDiskCacheKey(messageDigest)
            val safeKey: String = Util.sha256BytesToHex(messageDigest.digest())
            return "$safeKey.0"
        } catch (e: java.lang.Exception) {
        }
        return null
    }

    companion object {
        // 传递过来的照片数组
        const val KEY_PIC_LIST = "key_pic_list"

        // 传递过去的照片数组
        const val KEY_PIC_LIST_RESULT = "key_pic_list_result"
    }

}