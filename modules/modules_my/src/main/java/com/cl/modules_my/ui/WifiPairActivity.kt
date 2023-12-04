package com.cl.modules_my.ui

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.util.ViewUtils
import com.cl.modules_my.adapter.MyImageAdapter
import com.cl.modules_my.databinding.MyWifiPairActivityBinding
import com.youth.banner.Banner
import com.youth.banner.indicator.CircleIndicator
import dagger.hilt.android.AndroidEntryPoint

/**
 * 通用wifi配对界面
 */
@AndroidEntryPoint
@Route(path = RouterPath.My.WIFI_PAIR)
class WifiPairActivity: BaseActivity<MyWifiPairActivityBinding>() {

    private val wifiName by lazy {
        intent.getStringExtra("wifiName")
    }

    private val wifiPassWord by lazy {
        intent.getStringExtra("wifiPassWord")
    }

    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }
    private val accessoryId by lazy {
        intent.getStringExtra("accessoryId")
    }

    override fun initView() {
        binding.ftbTitle.setLeftClickListener { finish() }
        // 轮播图
        binding.banner.apply {
            addBannerLifecycleObserver(context as? LifecycleOwner)
            isAutoLoop(false)
            setBannerRound(10f)
            setUserInputEnabled(false)
            indicator = CircleIndicator(context)
            val urlList = mutableListOf<String>()
            /*item.imageUrls?.let {
                // 手动添加图片集合
                it.forEach { data -> data.imageUrl?.let { it1 -> urlList.add(it1) } }
            }*/
            setAdapter(MyImageAdapter(urlList, context))
        }

        binding.tvNext.setOnClickListener {
            // 显示第二张图。
            ViewUtils.setGone(binding.tvNext)
            ViewUtils.setVisible(binding.llTwo)
            binding.tvContent.text = "Press and hold the RESET button for 5s."
        }

        binding.tvBackTwo.setOnClickListener {
            // 显示第一张图。
            ViewUtils.setGone(binding.llTwo)
            ViewUtils.setGone(binding.tvNextThree)
            ViewUtils.setVisible(binding.tvNext)
            binding.tvNext.text = "Next"
            binding.tvContent.text = "Power on the device after it has been powered off for 10s."
        }

        binding.tvNextTwo.setOnClickListener {
            // 显示第三张图。
            ViewUtils.setGone(binding.llTwo)
            ViewUtils.setGone(binding.tvNext)
            ViewUtils.setVisible(binding.tvNextThree)
            binding.tvContent.text = "Confirm the indicator is blinking"
        }

        binding.tvNextThree.setOnClickListener {
            // 跳转到最后一张配网阶段的图。
            startActivity(Intent(this@WifiPairActivity, WifiPairTwoActivity::class.java).apply {
                putExtra("deviceId", deviceId)
                putExtra("accessoryId", accessoryId)
                putExtra("wifiName", wifiName)
                putExtra("wifiPassWord", wifiPassWord)
            })
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }
}