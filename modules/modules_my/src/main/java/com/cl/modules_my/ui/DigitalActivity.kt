package com.cl.modules_my.ui

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import cn.mtjsoft.barcodescanning.extentions.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollListener
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollView
import com.cl.common_base.widget.scroll.behavior.BottomSheetLayout
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.common_base.adapter.MedialAdapter
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.FollowAndFolloerPop
import com.cl.common_base.util.ViewUtils
import com.cl.modules_my.databinding.MyDigitalActivityBinding
import com.cl.modules_my.viewmodel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@Route(path = RouterPath.My.PAGE_DIGITAL)
@AndroidEntryPoint
class DigitalActivity : BaseActivity<MyDigitalActivityBinding>() {

    @Inject
    lateinit var mViewModel: MyViewModel

    private val adapter by lazy {
        MedialAdapter(mutableListOf())
    }

    override fun initView() {
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.flRoot) { v, insets ->
            binding.flRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }

        binding.linkageScroll.topScrollTarget = { binding.rvLinkageTop }
        binding.linkageScroll.listeners.add(object : BehavioralScrollListener {
            override fun onScrollChanged(v: BehavioralScrollView, from: Int, to: Int) {
                updateFloatState()
            }
        })

        binding.bottomSheet.setup(BottomSheetLayout.POSITION_MID, 450.dp, 550.dp)
        updateFloatState()

        // 成就列表
        binding.rvMedal.layoutManager = LinearLayoutManager(this@DigitalActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMedal.adapter = adapter

        // 关注者和被关注者列表
        mViewModel.followList()
        mViewModel.followingList()
    }

    override fun observe() {
        mViewModel.apply {
            wallpaperList.observe(this@DigitalActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (data.isNullOrEmpty()) return@success

                    // 替换背景
                    val wallId = userDetail.value?.data?.wallId
                    data?.firstOrNull { it.id == wallId }?.let { bean ->
                        Glide.with(this@DigitalActivity).load(bean.address)
                            .placeholder(R.mipmap.my_bg)
                            .into(binding.rvLinkageTop)
                    }
                }
            })

            userDetail.observe(this@DigitalActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    data?.let {
                        // 缓存信息
                        GSON.toJson(it)?.let { it1 ->
                            Prefs.putString(
                                Constants.Login.KEY_LOGIN_DATA,
                                it1
                            )
                        }

                        // 壁纸
                        when (data?.wallAddress) {
                            "banner01" -> {
                                binding.rvLinkageTop.background =
                                    ContextCompat.getDrawable(
                                        this@DigitalActivity, R.mipmap.banner01
                                    )
                            }

                            "banner02" -> {
                                binding.rvLinkageTop.background =
                                    ContextCompat.getDrawable(
                                        this@DigitalActivity, R.mipmap.banner02
                                    )
                            }

                            "banner03" -> {
                                binding.rvLinkageTop.background =
                                    ContextCompat.getDrawable(
                                        this@DigitalActivity, R.mipmap.banner03
                                    )
                            }

                            else -> mViewModel.wallpaperList()
                        }
                    }
                }
            })

            // 个人资产
            userAssets.observe(this@DigitalActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {

                    // 个人标签
                    adapter.setList(data?.userFlags)

                    // 动态更改宽高 iv_head_bg
                    // 相差26f
                    val layoutParams = binding.ivHeadBg.layoutParams
                    layoutParams.height = dp2px(if (data?.basicInfo?.framesHeads.isNullOrEmpty()) 84f else 110f)
                    layoutParams.width = dp2px(if (data?.basicInfo?.framesHeads.isNullOrEmpty()) 84f else 110f)
                    binding.ivHeadBg.layoutParams = layoutParams

                    // ll_head 动态设备margin top
                    val layoutParams1 = binding.llHead.layoutParams as ConstraintLayout.LayoutParams
                    layoutParams1.topMargin = dp2px(if (data?.basicInfo?.framesHeads.isNullOrEmpty()) 42f else 62f)
                    binding.llHead.layoutParams = layoutParams1

                    // 更新背景墙
                    val imageViews = listOf(binding.ivWallpaperOne, binding.ivWallpaperTwo, binding.ivWallpaperThree, binding.ivWallpaperFour)
                    data?.wallpapers?.forEachIndexed { index, wallpaper ->
                        if (index < imageViews.size) {
                            val drawableRes = when (wallpaper.picture) {
                                "banner01" -> R.mipmap.banner01
                                "banner02" -> R.mipmap.banner02
                                "banner03" -> R.mipmap.banner03
                                else -> null
                            }
                            drawableRes?.let {
                                imageViews[index].background = ContextCompat.getDrawable(this@DigitalActivity, it)
                            }
                        }
                    }
                }
            })
        }
    }

    override fun initData() {
        // 跳转到个人设置界面
        binding.rlEdit.setOnClickListener {
            startActivity(Intent(this@DigitalActivity, ProfileActivity::class.java))
        }
        // 跳转到个人设置界面
        binding.ivHead.setOnClickListener {
            startActivity(Intent(this@DigitalActivity, ProfileActivity::class.java))
        }
        binding.flBack.setOnClickListener {
            finish()
        }
        binding.llDigitalAchievement.setOnClickListener {
            // 跳转到成就页面
            startActivity(Intent(this@DigitalActivity, AchievementActivity::class.java).apply {
                putExtra(AchievementActivity.IS_ACHIEVEMENT, true)
            })
        }
        binding.llDigitalFrames.setOnClickListener {
            // 跳转到成就页面
            startActivity(Intent(this@DigitalActivity, AchievementActivity::class.java).apply {
                putExtra(AchievementActivity.IS_ACHIEVEMENT, false)
            })
        }

        binding.tvPots.setOnClickListener {
            // 跳转到
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_JOURNEY).navigation()
        }
        binding.tvPotsNumber.setOnClickListener {
            // 跳转到
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_JOURNEY).navigation()
        }

        // follow
        binding.tvFollow.setOnClickListener {
            xpopup(this@DigitalActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                maxHeight(dp2px(700f))
                asCustom(FollowAndFolloerPop(this@DigitalActivity, mViewModel.followList.value?.data, true)).show()
            }
        }
        binding.tvFollowNumber.setOnClickListener {
            xpopup(this@DigitalActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                maxHeight(dp2px(700f))
                asCustom(FollowAndFolloerPop(this@DigitalActivity, mViewModel.followList.value?.data, true)).show()
            }
        }

        // following
        binding.tvFollowing.setOnClickListener {
            xpopup(this@DigitalActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                maxHeight(dp2px(700f))
                asCustom(FollowAndFolloerPop(this@DigitalActivity, mViewModel.followingList.value?.data, false)).show()
            }
        }
        binding.tvFollowingNumber.setOnClickListener {
            xpopup(this@DigitalActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                maxHeight(dp2px(700f))
                asCustom(FollowAndFolloerPop(this@DigitalActivity, mViewModel.followingList.value?.data, false)).show()
            }
        }

        // wallpaper
        binding.llDigitalWallpaper.setOnClickListener {
            // 跳转壁纸界面
            startActivity(Intent(this@DigitalActivity, WallActivity::class.java))
        }

        // wallet
        binding.llDigitalWallet.setOnClickListener {
            // 跳转到氧气币种
            startActivity(Intent(this@DigitalActivity, OxygenListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.userDetail()
        mViewModel.userAssets()
    }

    override fun MyDigitalActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@DigitalActivity
            viewModel = mViewModel
            executePendingBindings()
        }
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