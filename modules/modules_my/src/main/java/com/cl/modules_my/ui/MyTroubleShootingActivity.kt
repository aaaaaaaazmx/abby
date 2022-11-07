package com.cl.modules_my.ui

import android.content.*
import android.graphics.Typeface
import android.net.Uri
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.easeui.EaseUiHelper
import com.cl.common_base.easeui.ui.EaseUiActivity
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.SendEmailTipsPop
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyTroubleShootingBinding
import com.cl.modules_my.ui.fragment.MyTroubleShootingFragment
import com.cl.modules_my.viewmodel.MyTroubleViewModel
import com.hyphenate.helpdesk.easeui.util.Config
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView
import javax.inject.Inject

/**
 * 解答疑问界面
 */
@AndroidEntryPoint
class MyTroubleShootingActivity : BaseActivity<MyTroubleShootingBinding>() {
    @Inject
    lateinit var mViewMode: MyTroubleViewModel

    // 通用Title
    private val titleList by lazy {
        mutableListOf("Growing", "abby", "Connect")
    }

    // fragmentList
    private var fragmentList = mutableListOf<Fragment>()

    override fun initView() {
        // 如果不是Vip那么就不限时支持按钮
        if (mViewMode.userInfo?.isVip == 1) {
            binding.title.setRightButtonImg(R.mipmap.my_support)
                .setRightClickListener {
                    sendEmail()
                }
        }
    }

    override fun observe() {
        mViewMode.apply {
            troubleShooting.observe(this@MyTroubleShootingActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    hideProgressLoading()
                    // 主要是有三个Fragment，这个是固定的。并不是动态配置的。
                    data?.let {
                        if (!it.Growing.isNullOrEmpty()) {
                            (fragmentList[0] as? MyTroubleShootingFragment)?.refreshData(it.Growing)
                        }
                        if (!it.abby.isNullOrEmpty()) {
                            (fragmentList[1] as? MyTroubleShootingFragment)?.refreshData(it.abby)
                        }
                        if (!it.Connect.isNullOrEmpty()) {
                            (fragmentList[2] as? MyTroubleShootingFragment)?.refreshData(it.Connect)
                        }
                    }
                }
            })
        }
    }

    override fun initData() {
        mViewMode.troubleShooting()
        // 添加FragmentList
        fragmentList.add(MyTroubleShootingFragment.newInstance(MyTroubleViewModel.Type.GROWING))
        fragmentList.add(MyTroubleShootingFragment.newInstance(MyTroubleViewModel.Type.ABBY))
        fragmentList.add(MyTroubleShootingFragment.newInstance(MyTroubleViewModel.Type.CONNECT))
        // 设置viewPage2
        binding.vpContent.offscreenPageLimit = 1
        binding.vpContent.isUserInputEnabled = true
        binding.vpContent.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getItemCount(): Int {
                return fragmentList.size
            }
        }
        // 设置指示器
        val commonNavigator = CommonNavigator(this@MyTroubleShootingActivity)
        commonNavigator.isAdjustMode = true
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return fragmentList.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = SimplePagerTitleView(context)
                simplePagerTitleView.text = titleList[index]
                simplePagerTitleView.normalColor = getColor(R.color.black)
                simplePagerTitleView.selectedColor = getColor(com.cl.common_base.R.color.mainColor)
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    dp2px(16f).toFloat()
                )
                simplePagerTitleView.setOnClickListener {
                    binding.vpContent.setCurrentItem(
                        index,
                        true
                    )
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.lineWidth = dp2px(24f).toFloat()
                indicator.lineHeight = dp2px(4f).toFloat()
                indicator.roundRadius = dp2px(2f).toFloat()
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.setColors(
                    resources.getColor(com.cl.common_base.R.color.mainColor),
                    resources.getColor(com.cl.common_base.R.color.mainColor)
                )
                return indicator
            }
        }

        binding.indicator.navigator = commonNavigator
        binding.vpContent.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding.indicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.indicator.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                binding.indicator.onPageScrollStateChanged(state)
            }
        })

    }

    /**
     * 发送支持邮件
     */
    private fun sendEmail() {
        // 跳转聊天界面
        EaseUiHelper.getInstance().startChat(null)
    }
}