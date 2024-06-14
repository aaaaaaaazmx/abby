package com.cl.modules_my.ui.fragment

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import cn.mtjsoft.barcodescanning.extentions.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.BaseThreeTextPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollListener
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollView
import com.cl.common_base.widget.scroll.behavior.BottomSheetLayout
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyNewFragmentBinding
import com.cl.modules_my.pop.MyDiscordPop
import com.cl.modules_my.ui.DigitalActivity
import com.cl.modules_my.ui.FeedbackActivity
import com.cl.modules_my.ui.OxygenListActivity
import com.cl.modules_my.ui.ProfileActivity
import com.cl.modules_my.ui.ReDeemActivity
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
        /*if (discordPop?.isShow == true) {
            // XPopup当前是显示状态，执行你想要的操作
            discordPop?.setQueryBind()
        }*/
    }

    override fun lazyLoad() {
        // 只加载一次
        mViewModel.userDetail()

        initCllick()
    }


    private val discordPop by lazy {
        context?.let {
            activity?.let { it1 ->
                MyDiscordPop(it, it1) { email, code ->
                    logI("email: $email, code: $code")
                }
            }
        }
    }

    private fun initCllick() {
        // 跳转到个人设置界面
        binding.rlEdit.setOnClickListener {
            startActivity(Intent(context, ProfileActivity::class.java))
        }
        // 跳转到个人设置界面
        binding.ivHead.setOnClickListener {
            startActivity(
                Intent(
                    context,
                    ProfileActivity::class.java
                )
            )
        }

        // 跳转到vip续订洁面
        binding.tvVipStill.setSafeOnClickListener {
            context?.let {
                startActivity(Intent(it, ReDeemActivity::class.java))
            }
        }

        // 跳准到反馈界面
        binding.ftFeedback.setOnClickListener {
            startActivity(Intent(context, FeedbackActivity::class.java))
        }

        // 跳转到资产界面
        binding.ftDigital.setOnClickListener {
            startActivity(Intent(context, DigitalActivity::class.java))
        }

        binding.rlVip.setOnClickListener {
            // 跳准到购买链接网址
           /*val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/subscription")
            startActivity(intent)*/
            context?.let {
                startActivity(Intent(it, ReDeemActivity::class.java))
            }
        }

        binding.ftMessage.setOnClickListener {
            InterComeHelp.INSTANCE.openInterComeSpace(InterComeHelp.InterComeSpace.HelpCenter)
        }

        binding.ftDiscord.setOnClickListener {
            if (!mViewModel.userInfo()?.discordGlobalName.isNullOrEmpty()) {
                context?.let {
                    xpopup(it) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(false)
                        asCustom(BaseCenterPop(it, isShowCancelButton = false, confirmText = "OK", content = "Connected with Discord ID ${mViewModel.userInfo()?.discordGlobalName}")).show()
                    }
                }
                return@setOnClickListener
            }
            context?.let {
                xpopup(it) {
                    isDestroyOnDismiss(false)
                    dismissOnTouchOutside(false)
                    asCustom(discordPop).show()
                }
            }
        }

        binding.ftSetting.setOnClickListener {
            // 改为缓存
            if (mViewModel.userInfo()?.spaceType != ListDeviceBean.KEY_SPACE_TYPE_BOX) {
                context?.let { ct ->
                    xpopup(ct) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(true)
                        asCustom(
                            BaseThreeTextPop(
                                ct,
                                content = "The settings are currently not available for the grow tent space. Please switch to the hey abby grow box to access the settings",
                                oneLineText = "Switch",
                                twoLineText = "OK",
                                oneLineCLickEventAction = {
                                    // 跳转到设别列表界面
                                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                                        .navigation(ct)
                                },
                            )
                        ).show()
                    }
                }
                return@setOnClickListener
            }

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
            userDetail.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    // 动态更改宽高 iv_head_bg
                    val layoutParams = binding.ivHeadBg.layoutParams
                    layoutParams.height = dp2px(if (data?.basicInfo?.framesHeads.isNullOrEmpty()) 84f else 110f)
                    layoutParams.width = dp2px(if (data?.basicInfo?.framesHeads.isNullOrEmpty()) 84f else 110f)
                    binding.ivHeadBg.layoutParams = layoutParams

                    // ll_head 动态设备margin top
                    val layoutParams1 = binding.llHead.layoutParams as ConstraintLayout.LayoutParams
                    layoutParams1.topMargin = dp2px(if (data?.basicInfo?.framesHeads.isNullOrEmpty()) 42f else 62f)
                    binding.llHead.layoutParams = layoutParams1


                    data?.let {
                        // 缓存信息
                        GSON.toJsonInBackground(it) { it1 ->
                            Prefs.putString(
                                Constants.Login.KEY_LOGIN_DATA,
                                it1
                            )
                        }

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

                            else -> {
                                context?.let { cc ->
                                    Glide.with(cc).asDrawable().load(data?.wallAddress ?: com.cl.common_base.R.mipmap.my_bg)
                                        .apply(RequestOptions().override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL))
                                        .into(object : CustomTarget<Drawable>() {
                                            override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
                                                binding.rvLinkageTop.background = resource
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {
                                            }
                                        })
                                }
                            }
                        }
                        // 设置vip显示日期。
                        binding.tvVip.text = isVipSHowText()
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