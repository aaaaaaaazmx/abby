package com.cl.modules_my.ui

import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.letMultiple
import com.cl.modules_my.databinding.MyOutletsAutoBinding
import com.cl.modules_my.viewmodel.MyOutletsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 每个排插的自动化列表总览页面
 */
@AndroidEntryPoint
class OutletsAutoActivity: BaseActivity<MyOutletsAutoBinding>() {
    @Inject
    lateinit var mViewMode: MyOutletsViewModel

    private val accessoryId by lazy {
        intent.getIntExtra("accessoryId", -1)
    }

    private val accessoryDeviceId by lazy {
        intent.getStringExtra("accessoryDeviceId")
    }

    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    private val portId by lazy {
        intent.getStringExtra("portId")
    }

    override fun initView() {
        letMultiple(accessoryId, deviceId, portId) { a,b,c ->
            mViewMode.getRuleList(a.toString(), b, c)
        }
    }

    override fun observe() {
        mViewMode.apply {

        }
    }

    override fun initData() {
    }
}