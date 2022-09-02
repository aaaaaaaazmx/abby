package com.cl.modules_my.ui

import androidx.core.widget.doAfterTextChanged
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyEditNickNameBinding
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditNickNameActivity : BaseActivity<MyEditNickNameBinding>() {
    @Inject
    lateinit var mViewModel: ProfileViewModel

    private val nickName by lazy {
        intent.getStringExtra(ProfileActivity.KEY_NICK_NAME)
    }

    private val modifyUserDetailReq by lazy {
        ModifyUserDetailReq()
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        if (!nickName.isNullOrEmpty()) {
            binding.etName.setText(nickName)
        }
    }

    override fun observe() {
        mViewModel.apply {
            modifyUserDetail.observe(this@EditNickNameActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    finish()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
                loading {
                    hideProgressLoading()
                }
            })
        }
    }

    override fun initData() {
        binding.tvCancel.setOnClickListener { finish() }

        binding.etName.doAfterTextChanged {
            val name = it.toString()
        }

        binding.tvSave.setOnClickListener {
            val name = binding.etName.text.toString()
            if (name.isNullOrEmpty()) return@setOnClickListener
            modifyUserDetailReq.nickName = name
            mViewModel.modifyUserDetail(modifyUserDetailReq)
        }

        binding.flClose.setOnClickListener {
            binding.etName.setText("")
        }
    }
}
