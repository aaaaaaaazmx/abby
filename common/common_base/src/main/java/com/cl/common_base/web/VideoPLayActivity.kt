package com.cl.common_base.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.databinding.BaseVideoPlayFullBinding


/**
 * 全屏网页播放视频
 */
class VideoPLayActivity: BaseActivity<BaseVideoPlayFullBinding>() {
    var mAgentWebFragment: AgentWebFragment? = null

    private val url by lazy {
        intent.getStringExtra(WebActivity.KEY_WEB_URL)
    }

    private val title by lazy {
        intent.getStringExtra(WebActivity.KEY_WEB_TITLE_NAME)
    }

    @SuppressLint("CommitTransaction")
    override fun initView() {
        mAgentWebFragment = AgentWebFragment.getInstance(Bundle().also {
            it.putString(AgentWebFragment.URL_KEY, url)
            it.putBoolean(AgentWebFragment.IS_ALWAYS_SHOW_BACK, true)
            it.putBoolean(AgentWebFragment.IS_SHOW_SHOP_CAR, false)
        })
        supportFragmentManager.beginTransaction().apply {
            mAgentWebFragment?.let {
                add(R.id.container_framelayout, it)
                commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.containerFramelayout) { v, insets ->
            binding.containerFramelayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }
}