package com.cl.modules_contact.ui

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.web.AgentWebFragment
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.FragmentShopBinding
import dagger.hilt.android.AndroidEntryPoint


/**
 * 商城首页
 */
@Route(path = RouterPath.Contact.PAGE_SHOP)
@AndroidEntryPoint
class ShopFragment : BaseFragment<FragmentShopBinding>() {
    var mAgentWebFragment: AgentWebFragment? = null

    override fun FragmentShopBinding.initBinding() {
        lifecycleOwner = this@ShopFragment
        executePendingBindings()
    }

    override fun initView(view: View) {
        childFragmentManager.beginTransaction().apply {
            mAgentWebFragment =  AgentWebFragment.getInstance(Bundle().also { it.putString(AgentWebFragment.URL_KEY, "https://heyabby.com/pages/app-store"); })
            mAgentWebFragment?.let {
                replace(R.id.container_framelayout, it, AgentWebFragment::class.java.getName())
                commit()
            }
        }
    }


    override fun lazyLoad() {
    }

}