package com.cl.abby

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.abby.databinding.ActivityMainBinding
import com.cl.abby.viewmodel.MainViewModel
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.showToast
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.CustomBubbleAttachPopup
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.web.AgentWebFragment
import com.cl.common_base.web.FragmentKeyDown
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.ui.ShopFragment
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
@SuppressLint("RestrictedApi")
@Route(path = RouterPath.Main.PAGE_MAIN)
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    // 默认为0
    private var mIndex = 0

    //    private val plantGuideFlag by lazy {
    //        intent.getStringExtra(LoginActivity.KEY_GUIDE_STATE)
    //    }

    @Inject
    lateinit var mViewModel: MainViewModel

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

    // 是否是手动模式
    @Autowired(name = Constants.Global.KEY_MANUAL_MODE)
    @JvmField
    var manualMode = false

    // fragments
    private var homeFragment: Fragment? = null
    private var plantingLogFragment: Fragment? = null
    private var contactFragment: Fragment? = null
    private var shopFragment: Fragment? = null
    private var myFragment: Fragment? = null

    // 第一次也就是新用户进入的时候，显示的界面
    private var firstJoinInFragment: Fragment? = null

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
        // 查看是否需要显示红点
        mViewModel.setIsPlants(plantFlag != "0")

        // 是否显示和隐藏种植Menu
        val menu = binding.bottomNavigation.menu
        menu.findItem(R.id.action_plant).isVisible = !firstLoginAndNoDevice
    }

    private val bubblePopHor by lazy {
        // 居中显示
        XPopup.Builder(this@MainActivity)
            .popupPosition(PopupPosition.Top)
            .dismissOnTouchOutside(true)
            .isClickThrough(true)  //点击透传
            .hasShadowBg(false) // 去掉半透明背景
            //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
            .offsetY(XPopupUtils.dp2px(this@MainActivity, 6f))
    }

    private val asPop by lazy {
        val pop = CustomBubbleAttachPopup(this@MainActivity, bubbleClickAction = {
            switchFragment(Constants.FragmentIndex.HOME_INDEX)
        })
            //.setArrowOffset(-XPopupUtils.dp2px(this@MainActivity, 40))  //气泡箭头偏移
            .setBubbleBgColor(Color.RED) //气泡背景
            .setArrowWidth(XPopupUtils.dp2px(this@MainActivity, 6f))
            .setArrowHeight(XPopupUtils.dp2px(this@MainActivity, 6f))
            //.setBubbleRadius(100)
            .setArrowRadius(XPopupUtils.dp2px(this@MainActivity, 2f))
        pop as CustomBubbleAttachPopup
    }

    private val badgeView by lazy {
        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
        //获取第1个itemView
        val itemView = menuView.getChildAt(0) as BottomNavigationItemView
        //引入badgeView
        val badgeView =
            LayoutInflater.from(this).inflate(R.layout.layout_badge_view, menuView, false)
        badgeView
    }

    override fun onResume() {
        super.onResume()
        // logI("1111: ${(userInfo.invoke())?.deviceStatus == "1"}")
        // 只有绑定的时候才会调用这个
        if (mIndex == 0) {
            // 当选中第0个的时候
            // 主要是消除小红点
            val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
            val itemView = menuView.getChildAt(0) as BottomNavigationItemView
            if (itemView.contains(badgeView)) {
                mViewModel.userDetail()
            }
        }
    }

    override fun observe() {
        mViewModel.apply {
            // 判断当前是处于种植状态
            isPlant.observe(this@MainActivity) {
                if (it == true) {
                    mViewModel.userDetail()
                } else {
                    // 直接删除
                    val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                    //获取第1个itemView
                    val itemView = menuView.getChildAt(0) as BottomNavigationItemView
                    if (itemView.contains(badgeView)) {
                        itemView.removeView(badgeView)
                    }
                }
            }

            // 前提是判断是否是会员
            userDetail.observe(this@MainActivity, resourceObserver {
                success {
                    if (null == data) return@success
                    // 获取存储的单位值，默认为 false
                    val storedUnit = saveUnit.value ?: false

                    // 计算新的单位值，如果 data?.inchMetricMode 为 "inch" 则为 false，否则为 true
                    val newUnit = data?.inchMetricMode != "inch"

                    // 只有在新旧单位不相等的情况下才进行赋值和保存
                    if (newUnit != storedUnit) {
                        setSaveUnit(newUnit)
                        // 异步保存新的单位值
                        Prefs.putBooleanAsync(Constants.My.KEY_MY_WEIGHT_UNIT, newUnit)
                    }


                    if (data?.isVip != 1 || mViewModel.isPlant.value == false) {
                        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                        //获取第1个itemView
                        val itemView = menuView.getChildAt(0) as BottomNavigationItemView
                        if (itemView.contains(badgeView)) {
                            itemView.removeView(badgeView)
                        }
                    } else {
                        // 刷新小红点
                        mViewModel.getMessageNumber()
                    }
                }
            })

            // 消息统计
            getHomePageNumber.observe(this@MainActivity, resourceObserver {
                loading {}
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                    val itemView = menuView.getChildAt(0) as BottomNavigationItemView

                    val shouldAddBadge = (mViewModel.unReadMessageNumber.value ?: 0) > 0 ||
                            (data?.calendarHighMsgCount ?: 0) > 0 ||
                            (data?.academyMsgCount ?: 0) > 0 ||
                            (mViewModel.environmentInfo.value?.data?.environmentLowCount ?: 0) > 0

                    updateBadgeViewStatus(itemView, shouldAddBadge)

                    data?.calendarHighMsgCount?.let { asPop.setCalendarNumbers(it) }

                    if (mIndex != 0 && shouldAddBadge) {
                        val showPopHorizontallyCentered = (data?.calendarHighMsgCount != 0 && mViewModel.unReadMessageNumber.value != 0)
                        bubblePopHor.isCenterHorizontal(!showPopHorizontallyCentered).atView(itemView).asCustom(asPop).show()

                        if (asPop.isShow) {
                            Handler().postDelayed({
                                asPop.dismiss()
                            }, 10000)
                        }

                        Constants.Global.KEY_IS_ONLY_ONE_SHOW = false
                    }
                }
            })

            // 环境消息的统计
            environmentInfo.observe(this@MainActivity, resourceObserver {
                success {
                    data?.let {
                        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                        val itemView = menuView.getChildAt(0) as BottomNavigationItemView

                        val shouldShowBadge = it.healthStatus != "Ideal" || (it.environmentLowCount ?: 0) > 0

                        if (shouldShowBadge) {
                            if (badgeView.parent != null) {
                                (badgeView.parent as ViewGroup).removeView(badgeView)
                            }
                            if (!itemView.contains(badgeView)) {
                                itemView.addView(badgeView)
                            }
                        } else {
                            val easeMessage = mViewModel.unReadMessageNumber.value ?: 0
                            val calendarMessage = mViewModel.getHomePageNumber.value?.data?.calendarHighMsgCount ?: 0
                            val acadeMessage = mViewModel.getHomePageNumber.value?.data?.academyMsgCount ?: 0
                            val shouldRemoveBadge = easeMessage == 0 && calendarMessage == 0 && acadeMessage == 0

                            if (shouldRemoveBadge && itemView.contains(badgeView)) {
                                itemView.removeView(badgeView)
                            }
                        }
                    }
                }
            })

            // 监听消息的变化
            unReadMessageNumber.observe(this@MainActivity) {
                if (null == it) return@observe
                asPop.setEaseNumber(it)
            }
        }

        // InterCome消息变化监听
        LiveEventBus.get().with(Constants.InterCome.KEY_INTER_COME_UNREAD_MESSAGE, Int::class.java)
            .observe(this) {
                mViewModel.userDetail()
                /*// 如果不是等于0、那么是不要展示的
                // 如果是设备在线状态 && 并且是已经开始种植的。
                if (mIndex == 0) {
                    // 当选中第0个的时候
                    // 主要是消除小红点
                    val menuView =
                        binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                    val itemView = menuView.getChildAt(0) as BottomNavigationItemView
                    if (!itemView.contains(badgeView)) {
                        itemView.addView(badgeView)
                    }
                    return@observe
                }
                // 不等于0
                if (it) {
                    // 只展示一次
                    if (Constants.Global.KEY_IS_ONLY_ONE_SHOW) {
                        //  表示有消息要来了，需要查询一遍
                        //  查询接口
                        mViewModel.userDetail()
                    }
                }*/
            }
    }

    private fun updateBadgeViewStatus(itemView: BottomNavigationItemView, shouldAdd: Boolean) {
        if (shouldAdd && !itemView.contains(badgeView)) {
            if (badgeView.parent != null) {
                (badgeView.parent as ViewGroup).removeView(badgeView)
            }
            itemView.addView(badgeView)
        } else if (!shouldAdd && itemView.contains(badgeView)) {
            itemView.removeView(badgeView)
            if (asPop.isShow) asPop.dismiss()
        }
    }

    override fun initData() {
        // 底部点击
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            if (it.itemId != R.id.action_home) {
                //  不是首页得时候、都需要请求接口、检查是否有数量
                if (Constants.Global.KEY_IS_ONLY_ONE_SHOW) {
                    mViewModel.userDetail()
                }
            }

            when (it.itemId) {
                R.id.action_home -> {
                    // 判断气泡是否弹出
                    if (asPop.isShow) {
                        asPop.dismiss()
                        Constants.Global.KEY_IS_ONLY_ONE_SHOW = false
                    }
                    switchFragment(Constants.FragmentIndex.HOME_INDEX)
                }

                R.id.action_plant -> switchFragment(Constants.FragmentIndex.PLANT_LOG)

                //  这个到时需要放出来
                R.id.action_contact -> switchFragment(Constants.FragmentIndex.CONTACT_INDEX)

                R.id.action_shop -> switchFragment(Constants.FragmentIndex.SHOP_INDEX)

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
            // todo asd
            if (mIndex == Constants.FragmentIndex.SHOP_INDEX) {
                (shopFragment as? ShopFragment)?.let {
                    it.mAgentWebFragment?.let { ftagments ->
                        val mFragmentKeyDown: FragmentKeyDown = ftagments
                        return if (mFragmentKeyDown.onFragmentKeyDown(keyCode, event)) {
                            true
                        } else {
                            if (System.currentTimeMillis().minus(mExitTime) <= 2000) {
                                finish()
                            } else {
                                mExitTime = System.currentTimeMillis()
                                showToast("Press to exit the program again")
                            }
                            true
                        }
                    }
                }
            } else {
                if (System.currentTimeMillis().minus(mExitTime) <= 2000) {
                    finish()
                } else {
                    mExitTime = System.currentTimeMillis()
                    showToast("Press to exit the program again")
                }
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
                    Constants.Global.KEY_GLOBAL_PLANT_GUIDE_FLAG, plantGuideFlag
                )
                bundle.putString(
                    Constants.Global.KEY_GLOBAL_PLANT_PLANT_STATE, plantFlag
                )
                bundle.putString(
                    Constants.Global.KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE, deviceOffLineState
                )
                // 是否是第一次登录注册、并且是从未绑定过设备
                bundle.putBoolean(
                    Constants.Global.KEY_GLOBAL_PLANT_FIRST_LOGIN_AND_NO_DEVICE,
                    firstLoginAndNoDevice
                )
                // 是否是手动模式
                bundle.putBoolean(
                    Constants.Global.KEY_MANUAL_MODE,
                    manualMode
                )

                // 第一次进入的界面、或者没绑定过设备
                if (mViewModel.userinfoBean?.deviceId.isNullOrEmpty() || firstLoginAndNoDevice) {
                    firstJoinInFragment?.let {
                        it.arguments = bundle
                        transaction.show(it)
                    } ?: kotlin.run {
                        ARouter.getInstance().build(RouterPath.Home.PAGE_FIRST_JOIN).navigation()?.let {
                            firstJoinInFragment = it as Fragment
                            firstJoinInFragment?.let { fragment ->
                                fragment.arguments = bundle
                                transaction.add(R.id.container, fragment, null)
                            }
                        }
                    }
                } else {
                    //  跳转到HomeFragment 种植引导页面，附带当前种植状态以及种植记录到第几步
                    //  RouterPath.Home.PAGE_HOME 种植引导页面
                    homeFragment?.let {
                        it.arguments = bundle
                        transaction.show(it)
                    } ?: kotlin.run {
                        ARouter.getInstance().build(RouterPath.Home.PAGE_HOME).navigation()?.let {
                            homeFragment = it as Fragment
                            homeFragment?.let { fragment ->
                                fragment.arguments = bundle
                                transaction.add(R.id.container, fragment, null)
                            }
                        }
                    }
                }
            }

            Constants.FragmentIndex.CONTACT_INDEX -> contactFragment?.let { transaction.show(it) }
                ?: kotlin.run {
                    ARouter.getInstance().build(RouterPath.Contact.PAGE_CONTACT).navigation()?.let {
                        contactFragment = it as Fragment
                        contactFragment?.let {
                            contactFragment = it
                            transaction.add(R.id.container, it, null)
                        }
                    }
                }

            Constants.FragmentIndex.SHOP_INDEX -> shopFragment?.let { transaction.show(it) }
                ?: kotlin.run {
                    ARouter.getInstance().build(RouterPath.Contact.PAGE_SHOP).navigation()?.let {
                        shopFragment = it as Fragment
                        shopFragment?.let {
                            shopFragment = it
                            transaction.add(R.id.container, it, null)
                        }
                    }
                }


            Constants.FragmentIndex.MY_INDEX -> myFragment?.let { transaction.show(it) }
                ?: kotlin.run {
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY).navigation()?.let {
                        myFragment = it as Fragment
                        myFragment?.let {
                            myFragment = it
                            transaction.add(R.id.container, it, null)
                        }
                    }
                }

            Constants.FragmentIndex.PLANT_LOG -> plantingLogFragment?.let {
                transaction.show(it)
            } ?: kotlin.run {
                ARouter.getInstance().build(RouterPath.Plant.PAGE_PLANT).navigation()?.let {
                    plantingLogFragment = it as Fragment
                    plantingLogFragment?.let {
                        plantingLogFragment = it
                        transaction.add(R.id.container, it, null)
                    }
                }
            }
        }
        mIndex = position
        transaction.commitAllowingStateLoss()
    }

    private fun hideFragment(transaction: FragmentTransaction) {
        firstJoinInFragment?.let { transaction.hide(it) }
        homeFragment?.let { transaction.hide(it) }
        contactFragment?.let { transaction.hide(it) }
        shopFragment?.let { transaction.hide(it) }
        myFragment?.let { transaction.hide(it) }
        plantingLogFragment?.let { transaction.hide(it) }
    }

    override fun recreate() {
        kotlin.runCatching {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            firstJoinInFragment?.let {
                fragmentTransaction.remove(it)
            }
            homeFragment?.let {
                fragmentTransaction.remove(it)
            }
            contactFragment?.let {
                fragmentTransaction.remove(it)
            }
            shopFragment?.let {
                fragmentTransaction.remove(it)
            }
            myFragment?.let {
                fragmentTransaction.remove(it)
            }
            plantingLogFragment?.let { fragmentTransaction.remove(it) }
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

    override fun onDestroy() {
        super.onDestroy()
        // 注销InterCome
        InterComeHelp.INSTANCE.logout()
        // BleManager.get().closeAll()
    }

    override fun inAppInfoChange(status: String) {
        val map = GSON.parseObject(status, Map::class.java)
        map?.forEach { (key, value) ->
            when (key) {
                // 是否是Vip
                /*Constants.APP.KEY_IN_APP_VIP -> {
                    logI("KEY_IN_APP_VIP： $value")
                    if (value != "1") {
                        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                        //获取第1个itemView
                        val itemView = menuView.getChildAt(0) as BottomNavigationItemView
                        if (itemView.contains(badgeView)) {
                            itemView.removeView(badgeView)
                        }
                    } else {
                        // 刷新小红点
                        mViewModel.userDetail()
                    }
                }*/
                // 开始种植
                Constants.APP.KEY_IN_APP_START_RUNNING -> {
                    logI("KEY_IN_APP_START_RUNNING: $value")
                    mViewModel.setIsPlants(value == "true")
                }
            }
        }
    }
}