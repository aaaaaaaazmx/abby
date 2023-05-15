package com.cl.modules_my.ui

import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.WallAdapter
import com.cl.modules_my.databinding.MyWallListActivityBinding
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.viewmodel.MyViewModel
import com.cl.modules_my.viewmodel.WallViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 壁纸列表页面
 */
@AndroidEntryPoint
class WallActivity : BaseActivity<MyWallListActivityBinding>() {

    @Inject
    lateinit var viewModel: WallViewModel

    private val adapter by lazy {
        WallAdapter(mutableListOf())
    }

    override fun initView() {
        binding.rvWall.apply {
            layoutManager = LinearLayoutManager(this@WallActivity)
            adapter = this@WallActivity.adapter
        }
    }

    override fun observe() {
        viewModel.apply {
            wallpaperList.observe(this@WallActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (data.isNullOrEmpty()) return@success
                    adapter.setList(data)
                }
            })

            modifyUserDetail.observe(this@WallActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 再请求一次
                    wallpaperList()
                }
            })
        }
    }

    override fun initData() {
        viewModel.wallpaperList()

        adapter.addChildClickViewIds(R.id.tv_apply)
        adapter.setOnItemChildClickListener { _, view, position ->
            val item = adapter.getItem(position)
            when (view.id) {
                R.id.tv_apply -> {
                    // 修改用户信息
                    viewModel.modifyUserDetail(ModifyUserDetailReq(wallId = item.id.toString(), wallAddress = item.address.toString()))
                }
            }
        }
    }
}