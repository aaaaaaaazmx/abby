package com.cl.abby

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.abby.databinding.ActivityMainBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.showToast
import dagger.hilt.android.AndroidEntryPoint

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

    // fragments
    private var homeFragment: Fragment? = null
    private var contactFragment: Fragment? = null
    private var myFragment: Fragment? = null

    // ViewModel
    private val mViewModel by viewModels<MainViewModel>()

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
        // Arouter注入
        ARouter.getInstance().inject(this)
        // 切换Fragment
        switchFragment(mIndex)
        // 为null的情况下就是用户第一次种植
        logI(plantGuideFlag)
    }

    override fun observe() {
    }

    override fun initData() {
        // 底部点击
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_home ->
                    switchFragment(Constants.FragmentIndex.HOME_INDEX)
                // todo 这个到时需要放出来
//                R.id.action_contact ->
//                    switchFragment(Constants.FragmentIndex.CONTACT_INDEX)
                R.id.action_my ->
                    switchFragment(Constants.FragmentIndex.MY_INDEX)
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
                showToast("再按一次退出程序")
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
}