package com.cl.modules_contact.ui

import android.content.Context
import android.graphics.Color
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.widget.edittext.bean.MentionUser
import com.cl.modules_contact.databinding.ContactReelPostActivityBinding
import com.cl.modules_contact.pop.ContactListPop
import com.cl.modules_contact.viewmodel.PostViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 生成Gif发布界面
 */
@AndroidEntryPoint
class ReelPostActivity : BaseActivity<ContactReelPostActivityBinding>() {

    @Inject
    lateinit var viewModel: PostViewModel

    override fun initView() {
    }

    override fun observe() {
    }

    override fun initData() {
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