package com.cl.abby

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.abby.databinding.ActivityMainBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.easeui.EaseUiHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.showToast
import com.cl.common_base.pop.CustomBubbleAttachPopup
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.modules_home.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import dagger.hilt.android.AndroidEntryPoint
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager
import javax.inject.Inject

/**
 * 主页入口
 */
@Route(path = RouterPath.Main.PAGE_MAIN)
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    // 默认为0
    private var mIndex = 0

    //    private val plantGuideFlag by lazy {
    //        intent.getStringExtra(LoginActivity.KEY_GUIDE_STATE)
    //    }

    @Inject
    lateinit var mViewModel: HomeViewModel

    // 引导状态
    @Autowired(name = Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG)
    @JvmField
    var plantGuideFlag: String = "0"

    // 种植状态，植物存在状态（0-未种植、1-已种植、2-未种植，且存在旧种植记录、3-种植完成过）
    @Autowired(name = Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE)
    @JvmField
    var plantFlag: String = "0"

    // 当前设备状态
    @Autowired(name = Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE)
    @JvmField
    var deviceOffLineState = "0"

    // 是否是第一次登录注册、并且是从未绑定过设备
    @Autowired(name = Constants.Global.KEY_GLOBAL_PLANT_FIRST_LOGIN_AND_NO_DEVICE)
    @JvmField
    var firstLoginAndNoDevice = false

    // fragments
    private var homeFragment: Fragment? = null
    private var contactFragment: Fragment? = null
    private var myFragment: Fragment? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currTabIndex", mIndex)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mIndex = savedInstanceState.getInt("currTabIndex")
        }
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        // 设置视频缓存、以及播放内核
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        // Arouter注入
        ARouter.getInstance().inject(this)
        // 切换Fragment
        switchFragment(mIndex)
        // 为null的情况下就是用户第一次种植
        logI(plantGuideFlag)
    }

    private val bubblePop by lazy {
        XPopup.Builder(this@MainActivity)
            // .isCenterHorizontal(true)
            .popupPosition(PopupPosition.Top)
            .dismissOnTouchOutside(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .isClickThrough(true)  //点击透传
            .atView((binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView).getChildAt(0))
            .hasShadowBg(false) // 去掉半透明背景
            //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
            .offsetY(XPopupUtils.dp2px(this@MainActivity, 6f))
            .asCustom(
                CustomBubbleAttachPopup(
                    this@MainActivity,
                    easeNumber = EaseUiHelper.getInstance().unReadMessage
                )
                    //.setArrowOffset(-XPopupUtils.dp2px(this@MainActivity, 40))  //气泡箭头偏移
                    .setBubbleBgColor(Color.RED) //气泡背景
                    .setArrowWidth(XPopupUtils.dp2px(this@MainActivity, 6f))
                    .setArrowHeight(XPopupUtils.dp2px(this@MainActivity, 6f))
                    //.setBubbleRadius(100)
                    .setArrowRadius(XPopupUtils.dp2px(this@MainActivity, 2f))
            )
    }

    private val badgeView by lazy {
        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
        //获取第1个itemView
        val itemView = menuView.getChildAt(0) as BottomNavigationItemView
        //引入badgeView
        val badgeView = LayoutInflater.from(this).inflate(R.layout.layout_badge_view, menuView, false)
        badgeView
    }

    override fun observe() {
        // 设备状态监听变化
        LiveEventBus.get().with(Constants.Global.KEY_MAIN_SHOW_BUBBLE, Boolean::class.java)
            .observe(this) {
                if (it) {
                    if (Constants.Global.KEY_IS_ONLY_ONE_SHOW) {
                        // todo 表示有消息要来了，需要查询一遍
                        // todo 查询接口
                        mViewModel.getMessageNumber()

                        // 查询环信
                        //获取底部菜单view
                        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                        //获取第1个itemView
                        val itemView = menuView.getChildAt(0) as BottomNavigationItemView
                        if (!itemView.contains(badgeView)) {
                            //把badgeView添加到itemView中
                            itemView.addView(badgeView)
                        }
                        // 弹窗
                        bubblePop.show()
                    } else {
                        // todo 清空消息或者是清空消息
                    }
                }
            }
    }

    override fun initData() {
        // 底部点击
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            if (it.itemId != R.id.action_home) {
                // todo 不是首页得时候、都需要请求接口、检查是否有数量
                mViewModel.getMessageNumber()
            }

            when (it.itemId) {
                R.id.action_home -> {
                    // 判断当前的气泡是否弹出
                    if (bubblePop.isShow) {
                        Handler().postDelayed({
                            Constants.Global.KEY_IS_ONLY_ONE_SHOW = false
                            bubblePop.dismiss()
                        }, 10000)
                    }
                    switchFragment(Constants.FragmentIndex.HOME_INDEX)
                }

                // todo 这个到时需要放出来
                /*R.id.action_contact -> switchFragment(Constants.FragmentIndex.CONTACT_INDEX)*/

                R.id.action_my -> {
                    switchFragment(Constants.FragmentIndex.MY_INDEX)
                }
            }
            true
        }
    }


    //退出时间
    private var mExitTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis().minus(mExitTime) <= 2000) {
                finish()
            } else {
                mExitTime = System.currentTimeMillis()
                showToast("Press to exit the program again")
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 切换Fragment
     */
    private fun switchFragment(position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        hideFragment(transaction)
        when (position) {
            Constants.FragmentIndex.HOME_INDEX -> {
                val bundle = Bundle()
                bundle.putString(
                    Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG,
                    plantGuideFlag
                )
                bundle.putString(
                    Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE,
                    plantFlag
                )
                bundle.putString(
                    Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE,
                    deviceOffLineState
                )
                // 是否是第一次登录注册、并且是从未绑定过设备
                bundle.putBoolean(Constants.Global.KEY_GLOBAL_PLANT_FIRST_LOGIN_AND_NO_DEVICE, firstLoginAndNoDevice)
                // todo 跳转到HomeFragment 种植引导页面，附带当前种植状态以及种植记录到第几步
                // todo RouterPath.Home.PAGE_HOME 种植引导页面
                homeFragment?.let {
                    it.arguments = bundle
                    transaction.show(it)
                } ?: kotlin.run {
                    ARouter.getInstance().build(RouterPath.Home.PAGE_HOME).navigation()
                        ?.let {
                            homeFragment = it as Fragment
                            homeFragment?.let { fragment ->
                                fragment.arguments = bundle
                                transaction.add(R.id.container, fragment, null)
                            }
                        }
                }
            }

            Constants.FragmentIndex.CONTACT_INDEX ->
                contactFragment?.let { transaction.show(it) } ?: kotlin.run {
                    ARouter.getInstance().build(RouterPath.Contact.PAGE_CONTACT).navigation()
                        ?.let {
                            contactFragment = it as Fragment
                            contactFragment?.let {
                                contactFragment = it
                                transaction.add(R.id.container, it, null)
                            }
                        }
                }

            Constants.FragmentIndex.MY_INDEX ->
                myFragment?.let { transaction.show(it) } ?: kotlin.run {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY).navigation()
                        ?.let {
                            myFragment = it as Fragment
                            myFragment?.let {
                                myFragment = it
                                transaction.add(R.id.container, it, null)
                            }
                        }
                }
        }
        mIndex = position
        transaction.commitAllowingStateLoss()
    }

    private fun hideFragment(transaction: FragmentTransaction) {
        homeFragment?.let { transaction.hide(it) }
        contactFragment?.let { transaction.hide(it) }
        myFragment?.let { transaction.hide(it) }
    }

    override fun recreate() {
        kotlin.runCatching {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            homeFragment?.let {
                fragmentTransaction.remove(it)
            }
            contactFragment?.let {
                fragmentTransaction.remove(it)
            }
            myFragment?.let {
                fragmentTransaction.remove(it)
            }
            fragmentTransaction.commitAllowingStateLoss()
        }.onFailure {
            it.printStackTrace()
        }
        super.recreate()
    }

    override fun onBleChange(status: String) {
        logI(status)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        homeFragment?.onActivityResult(requestCode, resultCode, data)
    }
}