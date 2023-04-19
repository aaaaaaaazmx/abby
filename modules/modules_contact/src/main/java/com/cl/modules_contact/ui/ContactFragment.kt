package com.cl.modules_contact.ui

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.databinding.FragmentContactBinding
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.viewmodel.ContactViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 联系人首页
 */
@Route(path = RouterPath.Contact.PAGE_CONTACT)
@AndroidEntryPoint
class ContactFragment: BaseFragment<FragmentContactBinding>() {
    @Inject
    lateinit var mViewMode: ContactViewModel


    override fun initView(view: View) {
        mViewMode.getNewPage(NewPageReq(current = 1, size = 10))
    }

    override fun lazyLoad() {
    }

    override fun observe() {
        mViewMode.apply {
            newPageData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                }
            })
        }
    }

    override fun FragmentContactBinding.initBinding() {

    }

    override fun onResume() {
        super.onResume()
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { v, insets ->
            binding.clRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }
}