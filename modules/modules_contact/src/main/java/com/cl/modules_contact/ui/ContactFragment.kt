package com.cl.modules_contact.ui

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.modules_contact.databinding.FragmentContactBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 联系人首页
 */
@Route(path = RouterPath.Contact.PAGE_CONTACT)
@AndroidEntryPoint
class ContactFragment: BaseFragment<FragmentContactBinding>() {
    override fun initView(view: View) {

    }

    override fun lazyLoad() {
    }

    override fun FragmentContactBinding.initBinding() {

    }
}