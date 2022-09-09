package com.cl.modules_my.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.LearnIdGuidePop
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.MyTroubleAdapter
import com.cl.modules_my.databinding.MyTroubleFragmentBinding
import com.cl.modules_my.repository.MyTroubleData
import com.cl.modules_my.viewmodel.MyTroubleViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 通用疑问弹窗
 */
@AndroidEntryPoint
class MyTroubleShootingFragment : BaseFragment<MyTroubleFragmentBinding>() {

    @Inject
    lateinit var mViewMode: MyTroubleViewModel

    private var myTroubleData: MutableList<MyTroubleData.Bean>? = null

    private val adapter =
        MyTroubleAdapter(mutableListOf())

    override fun MyTroubleFragmentBinding.initBinding() {

    }

    override fun initView(view: View) {
        binding.rvList.layoutManager = LinearLayoutManager(context)
        binding.rvList.adapter = adapter
    }

    override fun lazyLoad() {
    }

    private val finishUsuallyPop by lazy {
        context?.let { LearnIdGuidePop(it) }
    }

    private val pop by lazy {
        XPopup.Builder(context)
    }

    fun refreshData(myTroubleData: MutableList<MyTroubleData.Bean>?) {
        this.myTroubleData = myTroubleData
        adapter.setList(myTroubleData)
    }

    override fun observe() {
        mViewMode.apply {
            getDetailByLearnMoreId.observe(viewLifecycleOwner, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                }
                success {
                    hideProgressLoading()
                    // 弹出图文接口
                    finishUsuallyPop?.setData(data)
                }
            })
        }

        // 点击事件
        adapter.addChildClickViewIds(R.id.cl_content)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.cl_content -> {
                    (adapter.data[position] as? MyTroubleData.Bean)?.learnMoreId?.let {
                        // 获取图文接口
                        mViewMode.getDetailByLearnMoreId(it)
                        pop
                            .isDestroyOnDismiss(false)
                            .enableDrag(true)
                            .maxHeight(dp2px(700f))
                            .dismissOnTouchOutside(false)
                            .asCustom(finishUsuallyPop).show()
                    }
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(type: MyTroubleViewModel.Type): Fragment {
            val fragment = MyTroubleShootingFragment()
            val bundle = Bundle()
            bundle.putSerializable("type", type)
            fragment.arguments = bundle
            return fragment
        }
    }
}