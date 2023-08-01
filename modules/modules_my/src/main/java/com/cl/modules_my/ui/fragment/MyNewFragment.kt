package com.cl.modules_my.ui.fragment

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import cn.mtjsoft.barcodescanning.extentions.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollListener
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollView
import com.cl.common_base.widget.scroll.behavior.BottomSheetLayout
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyNewFragmentBinding
import com.cl.modules_my.ui.FeedbackActivity
import com.cl.modules_my.ui.OxygenListActivity
import com.cl.modules_my.ui.ProfileActivity
import com.cl.modules_my.ui.SettingActivity
import com.cl.modules_my.viewmodel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@Route(path = RouterPath.My.PAGE_MY)
@AndroidEntryPoint
class MyNewFragment : BaseFragment<MyNewFragmentBinding>() {
    @Inject
    lateinit var mViewModel: MyViewModel

    override fun MyNewFragmentBinding.initBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = mViewModel
            executePendingBindings()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            mViewModel.userDetail()
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.userDetail()
    }

    override fun lazyLoad() {
        // 只加载一次
        mViewModel.userDetail()

        initCllick()
    }

    private fun initCllick() {
        // 跳转到个人设置界面
        binding.rlEdit.setOnClickListener {
            startActivity(Intent(context, ProfileActivity::class.java))
        }
        // 跳转到个人设置界面
        binding.ivHead.setOnClickListener { startActivity(Intent(context, ProfileActivity::class.java)) }

        // 跳准到反馈界面
        binding.ftFeedback.setOnClickListener {
            startActivity(Intent(context, FeedbackActivity::class.java))
        }

        binding.rlVip.setOnClickListener {
            // 跳准到购买链接网址
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/subscription")
            startActivity(intent)
        }

        binding.ftMessage.setOnClickListener {
            InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.HelpCenter)
        }

        binding.ftSetting.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }
        binding.ftOxy.setOnClickListener {
            // 跳转到氧气币种
            context?.let {
                startActivity(Intent(it, OxygenListActivity::class.java))
            }
        }
    }

    override fun observe() {
        mViewModel.apply {
            wallpaperList.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (data.isNullOrEmpty()) return@success

                    // 替换背景
                    val wallId = userDetail.value?.data?.wallId
                    data?.firstOrNull { it.id == wallId }?.let { bean ->
                        context?.let {
                            Glide.with(it).load(bean.address)
                                .placeholder(com.cl.common_base.R.mipmap.my_bg)
                                .into(binding.rvLinkageTop)
                        }
                    }
                }
            })
            userDetail.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    data?.let {
                        // 缓存信息
                        GSON.toJson(it)?.let { it1 -> Prefs.putStringAsync(Constants.Login.KEY_USER_INFO, it1) }

                        // 壁纸
                        when (data?.wallAddress) {
                            "banner01" -> {
                                binding.rvLinkageTop.background = context?.let { cc ->
                                    ContextCompat.getDrawable(
                                        cc, com.cl.common_base.R.mipmap.banner01
                                    )
                                }
                            }

                            "banner02" -> {
                                binding.rvLinkageTop.background = context?.let { cc ->
                                    ContextCompat.getDrawable(
                                        cc, com.cl.common_base.R.mipmap.banner02
                                    )
                                }
                            }

                            "banner03" -> {
                                binding.rvLinkageTop.background = context?.let { cc ->
                                    ContextCompat.getDrawable(
                                        cc, com.cl.common_base.R.mipmap.banner03
                                    )
                                }
                            }

                            else -> mViewModel.wallpaperList()
                        }
                    }
                }
            })
        }
    }

    override fun initView(view: View) {
        binding.linkageScroll.topScrollTarget = { binding.rvLinkageTop }
        binding.linkageScroll.listeners.add(object : BehavioralScrollListener {
            override fun onScrollChanged(v: BehavioralScrollView, from: Int, to: Int) {
                updateFloatState()
            }
        })

        binding.bottomSheet.setup(BottomSheetLayout.POSITION_MAX, 400.dp, 400.dp)
        updateFloatState()
    }

    private val floatingHeight = 200.dp
    private fun updateFloatState() {
        if (binding.bottomSheet.indexOfChild(binding.rvLinkageBottom) >= 0) {
            if (binding.linkageScroll.scrollY >= floatingHeight) {
                binding.bottomSheet.visibility = View.GONE
                binding.bottomSheet.removeView(binding.rvLinkageBottom)
                if (binding.layoutBottom.indexOfChild(binding.rvLinkageBottom) < 0) {
                    binding.layoutBottom.addView(binding.rvLinkageBottom)
                }
                binding.linkageScroll.bottomScrollTarget = { binding.rvLinkageBottom }
            }
        } else {
            if (binding.linkageScroll.scrollY < floatingHeight) {
                binding.linkageScroll.bottomScrollTarget = null
                if (binding.layoutBottom.indexOfChild(binding.rvLinkageBottom) >= 0) {
                    binding.layoutBottom.removeView(binding.rvLinkageBottom)
                }
                if (binding.bottomSheet.indexOfChild(binding.rvLinkageBottom) < 0) {
                    binding.bottomSheet.addView(binding.rvLinkageBottom)
                }
                binding.bottomSheet.visibility = View.VISIBLE
            }
        }
    }
}