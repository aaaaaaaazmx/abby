package com.cl.modules_home.activity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.bbgo.module_home.databinding.HomeCameraBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.modules_home.adapter.MyAdapter
import com.cl.modules_home.widget.CenterLayoutManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * 摄像头功能界面
 */
@AndroidEntryPoint
@Route(path = RouterPath.Home.PAGE_CAMERA)
class CameraActivity: BaseActivity<HomeCameraBinding>() {
    var letters = arrayOf("TIME-LAPSE", "VIDEO", "PHOTO", "MIC")

    internal interface OnScrollListener {
        fun onScrolled(position: Int, adapters: MyAdapter)
    }
    private val scrollListener = object : OnScrollListener {
        override fun onScrolled(position: Int, adapters: MyAdapter) {
            adapters.setFocusedPosition(position)
        }
    }

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }

        binding.recyclerView.apply {
            layoutManager = CenterLayoutManager(this@CameraActivity, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = layoutManager


            var recyclerViewWidth: Int = layoutParams.width
            if (recyclerViewWidth == -1) {
                val displayMetrics = resources.displayMetrics
                recyclerViewWidth = displayMetrics.widthPixels
            }

            val targetPosition = 1
            val adapters = MyAdapter(letters, this@CameraActivity, recyclerViewWidth, this)
            val snapHelper: SnapHelper = LinearSnapHelper()
            adapter = adapters
            adapters.setFocusedPosition(targetPosition)
            snapHelper.attachToRecyclerView(this)

            // 滑动监听
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout) {
                        val centerView = snapHelper.findSnapView(layoutManager)
                        val pos = layoutManager!!.getPosition(centerView!!)
                        scrollListener.onScrolled(pos, adapters)
                        logI("onScrollStateChanged: pos = $pos")
                    }
                }
            })
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }
}