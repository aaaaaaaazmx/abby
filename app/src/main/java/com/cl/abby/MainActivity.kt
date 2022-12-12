package com.cl.abby

import android.content.Intent
import android.graphics.Color
import android.media.midi.MidiDevice
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.abby.databinding.ActivityMainBinding
import com.cl.abby.viewmodel.MainViewModel
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.showToast
import com.cl.common_base.pop.CustomBubbleAttachPopup
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.hyphenate.chat.ChatClient
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import dagger.hilt.android.AndroidEntryPoint
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager
import javax.inject.Inject
import kotlin.math.min


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
        // 查看是否需要显示红点
        mViewModel.setIsPlants(plantFlag != "0")
    }

    private val bubblePop by lazy {
        XPopup.Builder(this@MainActivity).isCenterHorizontal(true).popupPosition(PopupPosition.Top)
            .dismissOnTouchOutside(false).isClickThrough(true)  //点击透传
            .atView(
                (binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView).getChildAt(
                    0
                )
            ).hasShadowBg(false) // 去掉半透明背景
            //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
            .offsetY(XPopupUtils.dp2px(this@MainActivity, 6f)).asCustom(
                asPop
            )
    }

    private val asPop by lazy {
        val pop = CustomBubbleAttachPopup(this@MainActivity, bubbleClickAction = {
            switchFragment(0)
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

    /**
     * 获取用户信息
     */
    private val userInfo = {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
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
                    if (null == data) return@success

                    /**
                     * 只有2个中有一个是不等于0、那么就可以添加弹窗
                     */
                    val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                    //获取第1个itemView
                    val itemView = menuView.getChildAt(0) as BottomNavigationItemView
                    if ((mViewModel.unReadMessageNumber.value ?: 0) > 0 || (data?.calendarMsgCount
                            ?: 0) > 0
                    ) {
                        if (!itemView.contains(badgeView)) {
                            //把badgeView添加到itemView中
                            itemView.addView(badgeView)
                        }
                        // 不是第0个的时候才显示弹窗、不然只显示下面的小红点
                        if (mIndex != 0) {
                            // 弹窗
                            if (!bubblePop.isShow && Constants.Global.KEY_IS_ONLY_ONE_SHOW) {
                                bubblePop.show()
                             /*   if ((data?.calendarMsgCount != 0 && mViewModel.unReadMessageNumber.value != 0)) {

                                } else {
                                    XPopup.Builder(this@MainActivity).popupPosition(PopupPosition.Top)
                                        .dismissOnTouchOutside(false).isClickThrough(true)  //点击透传
                                        .atView(
                                            (binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView).getChildAt(
                                                0
                                            )
                                        ).hasShadowBg(false) // 去掉半透明背景
                                        .offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                                        .offsetY(XPopupUtils.dp2px(this@MainActivity, 6f)).asCustom(
                                            asPop
                                        ).show()
                                }*/
                            }
                            // 不再显示气泡
                            Constants.Global.KEY_IS_ONLY_ONE_SHOW = false
                        }
                        // 学院消息
                    } else if ((data?.academyMsgCount ?: 0) > 0) {
                        if (!itemView.contains(badgeView)) {
                            //把badgeView添加到itemView中
                            itemView.addView(badgeView)
                        }
                        // 环信消息
                    } else if ((mViewModel.unReadMessageNumber.value
                            ?: 0) == 0 && (data?.calendarMsgCount
                            ?: 0) == 0 && (data?.academyMsgCount ?: 0) == 0
                    ) {
                        if (itemView.contains(badgeView)) {
                            //把badgeView添加到itemView中
                            itemView.removeView(badgeView)
                            if (bubblePop.isShow) bubblePop.dismiss()
                        }
                    }
                    data?.calendarMsgCount?.let { asPop.setCalendarNumbers(it) }

                    // 选中其他TAb的时候、请求这个接口、弹出弹窗、然后在10秒内隐藏。
                    if (mIndex != 0) {
                        // 判断当前的气泡是否弹出
                        if (bubblePop.isShow) {
                            Handler().postDelayed({
                                bubblePop.dismiss()
                            }, 10000)
                        }
                    }
                }
            })

            // 环境消息的统计
            plantInfoLoop.observe(this@MainActivity, resourceObserver {
                success {
                    if (null == data) return@success
                    logI("1231231231plantInfoLoopplantInfoLoopplantInfoLoop")
                    /**
                     * 只有2个中有一个是不等于0、那么就可以添加弹窗
                     */
                    val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
                    //获取第1个itemView
                    val itemView = menuView.getChildAt(0) as BottomNavigationItemView
                    if (data?.healthStatus != "Ideal") {
                        if (!itemView.contains(badgeView)) {
                            itemView.addView(badgeView)
                        }
                    } else {
                        // 表示健康的
                        // 需要判断上面的消息还有没有没有的话那就直接消除
                        val easeMessage = mViewModel.unReadMessageNumber.value
                        val calendarMessage = mViewModel.getHomePageNumber.value?.data?.calendarMsgCount
                        val acadeMessage = mViewModel.getHomePageNumber.value?.data?.academyMsgCount
                        if (easeMessage == 0 && calendarMessage == 0 && acadeMessage == 0) {
                            if (itemView.contains(badgeView)) {
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

        // 环信消息变化监听
        LiveEventBus.get().with(Constants.Global.KEY_MAIN_SHOW_BUBBLE, Boolean::class.java)
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
                    if (bubblePop.isShow) {
                        bubblePop.dismiss()
                        Constants.Global.KEY_IS_ONLY_ONE_SHOW = false
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
                // todo 跳转到HomeFragment 种植引导页面，附带当前种植状态以及种植记录到第几步
                // todo RouterPath.Home.PAGE_HOME 种植引导页面
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

    override fun onDestroy() {
        super.onDestroy()
        //  需要判断先登录
        if (ChatClient.getInstance().isLoggedInBefore) {
            // 注销环信
            ChatClient.getInstance().logout(true, null)
        }
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