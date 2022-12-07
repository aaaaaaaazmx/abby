package com.cl.modules_home.ui

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbgo.module_home.databinding.HomeAcademyDetailActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AcademyDetails
import com.cl.common_base.constants.Constants
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
        binding.title.setLeftClickListener {
            back()
        }
    }

    override fun onBackPressed() {
        back()
    }

    private fun back() {
        val value = mViewMode.messageReadList
        if (value.isEmpty()) {
            finish()
        } else {
            intent.putExtra(KEY_ID_LIST, value.toList().toTypedArray())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
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

            /*  mViewMode.messageRead.observe(this@AcademyDetailActivity, resourceObserver {
                  loading { }
                  error { errorMsg, code ->
                      ToastUtil.shortShow(errorMsg)
                  }
                  success {
                  }
              })*/
        }
    }

    override fun initData() {
        adapter.setOnItemClickListener { adapter, view, position ->
            val data = adapter.data[position] as? AcademyDetails

            // 已读消息
            id?.let {
                mViewMode.messageRead(it)
                mViewMode.setReadList(it)
                data?.isRead = 1
                adapter.notifyItemChanged(position)
            }

            val intent = Intent(
                this@AcademyDetailActivity,
                KnowMoreActivity::class.java
            )
            intent.putExtra(Constants.Global.KEY_TXT_ID, data?.txtId)
            startActivity(intent)
        }
    }

    companion object {
        const val KEY_ID_LIST = "key_id_list"
    }

}