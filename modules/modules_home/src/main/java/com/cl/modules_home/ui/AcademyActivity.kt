package com.cl.modules_home.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbgo.module_home.databinding.HomeAcademyActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AcademyListData
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.adapter.HomeAcademyPopAdapter
import com.cl.modules_home.viewmodel.HomeAcademyViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AcademyActivity : BaseActivity<HomeAcademyActivityBinding>() {
    private val adapter by lazy {
        HomeAcademyPopAdapter(mutableListOf())
    }

    @Inject
    lateinit var mViewMode: HomeAcademyViewModel


    override fun initView() {
        binding.rvAcademy.layoutManager = LinearLayoutManager(this@AcademyActivity)
        binding.rvAcademy.adapter = adapter

        mViewMode.getAcademyList()
    }

    override fun observe() {
        mViewMode.apply {
            getAcademyList.observe(this@AcademyActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (data?.isEmpty() == true) return@success
                    adapter.setList(data)
                }
            })
        }
    }

    override fun initData() {
        adapter.setOnItemClickListener { adapter, view, position ->
            val data = adapter.data[position] as? AcademyListData
            val intent = Intent(this@AcademyActivity, AcademyDetailActivity::class.java)
            intent.putExtra(KEY_ID, data?.id)
            myActivityLauncher.launch(intent)
        }
    }

    private val myActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val result = activityResult.data?.extras?.getStringArray(AcademyDetailActivity.KEY_ID_LIST)
            if (result.isNullOrEmpty()) return@registerForActivityResult
            logI("123312 + $result")
            result.forEach { id ->
                val index = adapter.data.indexOfFirst { it.id == id }
                if (index != -1) {
                    // 设置为已读
                    adapter.data[index].isRead = 1
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }

    companion object {
        const val KEY_ID = "id"
    }
}