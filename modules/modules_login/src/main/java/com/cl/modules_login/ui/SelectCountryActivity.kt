package com.cl.modules_login.ui

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.modules_login.R
import com.cl.modules_login.adapter.SelectCountryAdapter
import com.cl.modules_login.databinding.ActivitySelectCountryBinding
import com.cl.modules_login.response.CountData

/**
 * 选择国家页面
 */
class SelectCountryActivity : BaseActivity<ActivitySelectCountryBinding>() {
    private val countList by lazy {
        val mCountList =
            intent.getSerializableExtra(CreateAccountActivity.COUNT_LIST) as? MutableList<CountData>
        mCountList
    }

    private val adapter by lazy {
        SelectCountryAdapter(mutableListOf())
    }

    override fun initView() {
        binding.title.setLeftImageRes(R.mipmap.login_close)
            .setLeftClickListener {
                // -1 表示当前直接退出
                setResult(RESULT_OK, Intent().putExtra(KEY_POSITION, -1))
                finish()
            }
    }

    override fun observe() {
    }

    override fun initData() {
        binding.rvCountry.layoutManager = LinearLayoutManager(this)
        binding.rvCountry.adapter = adapter
        logD(countList)
        adapter.setList(countList)

        // 条目点击事件
        adapter.setOnItemClickListener { _, _, position ->
            logI("setOnItemClickListener: $position")
            setResult(RESULT_OK, Intent().putExtra(KEY_POSITION, position))
            finish()
        }
    }

    companion object {
        const val KEY_POSITION = "key_position"
    }
}