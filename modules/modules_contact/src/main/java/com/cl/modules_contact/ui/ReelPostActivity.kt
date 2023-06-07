package com.cl.modules_contact.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.cl.common_base.widget.edittext.bean.MentionUser
import com.cl.modules_contact.ItemTouchHelp
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ChooserAdapter
import com.cl.modules_contact.databinding.ContactReelPostActivityBinding
import com.cl.modules_contact.decoraion.FullyGridLayoutManager
import com.cl.modules_contact.decoraion.GridSpaceItemDecoration
import com.cl.modules_contact.pop.ContactListPop
import com.cl.modules_contact.response.ChoosePicBean
import com.cl.modules_contact.ui.pic.ChoosePicActivity
import com.cl.modules_contact.viewmodel.PostViewModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.utils.DensityUtil
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
            if (holder.itemViewType == ChoosePicBean.KEY_TYPE_PIC) {
                mItemTouchHelper?.startDrag(holder)
            }
        })
    }

    private var mItemTouchHelper: ItemTouchHelper? = null

    override fun initView() {
        binding.rvPic.apply {
                layoutManager = FullyGridLayoutManager(
                    this@ReelPostActivity,
                    4, GridLayoutManager.VERTICAL, false
                )
                addItemDecoration(
                    GridSpaceItemDecoration(
                        4,
                        DensityUtil.dip2px(this@ReelPostActivity, 4f), DensityUtil.dip2px(this@ReelPostActivity, 1f)
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
                    // todo 跳转到选中图片界面
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionHelp().applyPermissionHelp(
                            this@ReelPostActivity,
                            getString(com.cl.common_base.R.string.profile_request_photo),
                            object : PermissionHelp.OnCheckResultListener {
                                override fun onResult(result: Boolean) {
                                    if (!result) return
                                    startActivity(Intent(this@ReelPostActivity, ChoosePicActivity::class.java))
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
                                    startActivity(Intent(this@ReelPostActivity, ChoosePicActivity::class.java))
                                }
                            },
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_MEDIA_IMAGES,
                        )
                    }
                }

                R.id.img_contact_pic_delete -> {
                    this@ReelPostActivity.chooserAdapter.removeAt(position)
                    viewModel.deletePicAddress(position)
                    picList.removeAt(position)
                    // 在最后面添加到ADD
                    if (this@ReelPostActivity.chooserAdapter.data.filter { it.type == ChoosePicBean.KEY_TYPE_ADD }.size == 1) {
                        return@setOnItemChildClickListener
                    } else {
                        this@ReelPostActivity.chooserAdapter.addData(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
                        picList.add(ChoosePicBean(type = ChoosePicBean.KEY_TYPE_ADD, picAddress = ""))
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
}