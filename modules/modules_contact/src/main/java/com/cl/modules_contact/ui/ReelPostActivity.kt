package com.cl.modules_contact.ui

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
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
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.Gif
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.ImageUtil
import com.cl.common_base.widget.edittext.bean.MentionUser
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ChooserAdapter
import com.cl.modules_contact.databinding.ContactReelPostActivityBinding
import com.cl.modules_contact.decoraion.FullyGridLayoutManager
import com.cl.modules_contact.decoraion.GridSpaceItemDecoration
import com.cl.modules_contact.pop.ContactListPop
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
import java.io.File
import java.io.Serializable
import java.lang.Float.max
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

    private fun initClick() {
        // 合成gif
        binding.tvPreview.setOnClickListener {
            FileUtil.deleteDirectory(DeviceConstants.getDialPhotoPath(this@ReelPostActivity))
            val sources: MutableList<Bitmap> = ArrayList()
            picList.forEachIndexed { index, choosePicBean ->
                if (choosePicBean.type == ChoosePicBean.KEY_TYPE_PIC) {
                    var imageBitmap: Bitmap? = null
                    // 需要区分是否是网络图片
                    if (choosePicBean.picAddress?.contains("https") == true || choosePicBean.picAddress?.contains("http") == true) {
                        // 表示是网络图片，
                        val cacheKey = ObjectKey(choosePicBean.picAddress ?: "")
                        val cacheFile = DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(this@ReelPostActivity), DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong())
                            .get(cacheKey)
                        // 通过缓存转换成bitmap
                        if (cacheFile != null && cacheFile.exists()) {
                            imageBitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                        } else {
                            // todo 如果没找到缓存，那么就下载图片
                        }
                    } else {
                        // 本地照片
                        imageBitmap = BitmapFactory.decodeFile(choosePicBean.picAddress, BitmapFactory.Options())
                    }
                    // val targetBitmap = ImageUtil.getTargetBitmap(imageBitmap, 450f, 600f)

                    // 读取图片文件
                    /*val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(choosePicBean.picAddress, options)
                    val imageWidth = options.outWidth
                    val imageHeight = options.outHeight
                    val scaleFactor = min(1f, min(450f / imageWidth, 600f / imageHeight))
                    options.inJustDecodeBounds = false
                    options.inSampleSize = scaleFactor.toInt()
                    val bitmap = BitmapFactory.decodeFile(choosePicBean.picAddress, options)

                    // 缩放图片
                    val matrix = Matrix()
                    matrix.postScale(scaleFactor, scaleFactor)
                    val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true)*/


                    // 判断图片是否需要放大
                    // imageWidth < 540 || imageHeight < 720
                    if (imageBitmap?.width!! < 540 || imageBitmap?.height!! < 720) {
                        // 需要放大，按照原来的方式进行放大
                         imageBitmap = ImageUtil.getTargetBitmap(imageBitmap, 450f, 600f)
                    } else {
                        // 需要缩小的
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(choosePicBean.picAddress, options)
                        val imageWidth = options.outWidth
                        val imageHeight = options.outHeight
                        val scaleFactor = min(1f, min(450f / imageWidth, 600f / imageHeight))
                        options.inJustDecodeBounds = false
                        options.inSampleSize = scaleFactor.toInt()
                        val bitmap = BitmapFactory.decodeFile(choosePicBean.picAddress, options)

                        // 缩放图片
                        val matrix = Matrix()
                        matrix.postScale(scaleFactor, scaleFactor)
                        imageBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true)
                    }

                    val imagePath = DeviceConstants.getDialPhotoPath(this@ReelPostActivity) + File.separator + "image" + index + ".png"
                    ImageUtil.saveBitmap(imageBitmap, imagePath)
                    imageBitmap?.let { it1 -> sources.add(it1) }
                }
            }

            FileUtil.deleteDirectory(DeviceConstants.getDialCustomGif(this@ReelPostActivity))
            FileUtil.createDirIfNotExists(DeviceConstants.getDialCustomGif(this@ReelPostActivity))
            // 保存gif路径
            val dialCustomGif = DeviceConstants.getDialCustomGif(this@ReelPostActivity) + System.currentTimeMillis() + ".gif"

            Gif.Builder().setSources(sources).setDestPath(dialCustomGif).setDelay(200).setRepeat(1).start(object : Gif.ResultCallback {
                override fun onSuccess(destPath: String?) {
                    logI("pngToGif >> onSuccess")
                }

                override fun onError(msg: String?) {
                    logI("pngToGif >> onFailure")
                }
            })
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
        }
        binding.cbTwo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.cbOne.isChecked = false
                binding.cbThree.isChecked = false
                binding.cbTwo.setTextColor(Color.WHITE)
                binding.cbOne.setTextColor(Color.BLACK)
                binding.cbThree.setTextColor(Color.BLACK)
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
     * 回调刷新页面
     */
    private val startActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            // 传递回来的照片集合
            val list = it.data?.getSerializableExtra(KEY_PIC_LIST) as? MutableList<*> ?: mutableListOf<String>()
            // 直接清空，然后在添加数据
            if (list.isEmpty()) return@registerForActivityResult
            lifecycleScope.launch {
                picList.clear()
                val listPic = withContext(Dispatchers.IO) {
                    list.map { data ->
                        ChoosePicBean(type = ChoosePicBean.KEY_TYPE_PIC, picAddress = data.toString())
                    } + ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD)
                }.take(20)
                picList.addAll(listPic)
                chooserAdapter.setList(picList)
            }

            //  todo 获取Glide的缓存文件
            /* val cacheKey = ObjectKey((list[0] ?: "") as String)
             val cacheFile = DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(this@ReelPostActivity), DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong())
                 .get(cacheKey)
             logI("123123123: ${cacheFile?.absolutePath}")
             if (cacheFile != null && cacheFile.exists()) {
                 val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                 imageView.setImageBitmap(bitmap)
             }*/
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