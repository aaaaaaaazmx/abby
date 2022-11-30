package com.cl.modules_home.ui

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbgo.module_home.databinding.HomeAcademyDetailActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AcademyDetails
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.adapter.HomeAcademyDetailAdapter
import com.cl.modules_home.viewmodel.HomeAcademyViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 学院详情列表
 */
@AndroidEntryPoint
class AcademyDetailActivity : BaseActivity<HomeAcademyDetailActivityBinding>() {

    @Inject
    lateinit var mViewMode: HomeAcademyViewModel

    private val adapter by lazy {
        HomeAcademyDetailAdapter(mutableListOf())
    }

    private val id by lazy {
        intent.getStringExtra(AcademyActivity.KEY_ID)
    }

    override fun initView() {
        binding.rvAcademy.layoutManager = LinearLayoutManager(this@AcademyDetailActivity)
        binding.rvAcademy.adapter = adapter

        id?.let { mViewMode.getAcademyDetails(it) }
    }

    override fun observe() {
        mViewMode.apply {
            getAcademyDetails.observe(this@AcademyDetailActivity, resourceObserver {
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
            val data = adapter.data[position] as? AcademyDetails
            val intent = Intent(
                this@AcademyDetailActivity,
                KnowMoreActivity::class.java
            )
            intent.putExtra(KnowMoreActivity.KEY_TXT_ID, data?.txtId)
            startActivity(intent)
        }
    }

}