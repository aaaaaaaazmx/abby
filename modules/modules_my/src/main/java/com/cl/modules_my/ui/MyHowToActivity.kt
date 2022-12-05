package com.cl.modules_my.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.easeui.EaseUiHelper
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.LearnIdGuidePop
import com.cl.common_base.pop.SendEmailTipsPop
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.MyTroubleAdapter
import com.cl.modules_my.databinding.MyHowToBinding
import com.cl.modules_my.repository.MyTroubleData
import com.cl.modules_my.viewmodel.MyHowToViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyHowToActivity : BaseActivity<MyHowToBinding>() {

    @Inject
    lateinit var mViewMode: MyHowToViewModel

    private val adapter by lazy {
        MyTroubleAdapter(mutableListOf())
    }


    private val finishUsuallyPop by lazy {
        LearnIdGuidePop(this@MyHowToActivity)
    }

    private val pop by lazy {
        XPopup.Builder(this@MyHowToActivity)
    }


    override fun initView() {
        // 如果不是Vip那么就不限时支持按钮
        if (mViewMode.userInfo?.isVip == 1) {
            binding.title.setRightButtonImg(R.mipmap.my_support)
                .setRightClickListener {
                    sendEmail()
                }
        }

        binding.rvList.layoutManager = LinearLayoutManager(this@MyHowToActivity)
        binding.rvList.adapter = adapter
    }


    override fun observe() {
        mViewMode.howTo.observe(this, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                hideProgressLoading()
                errorMsg?.let { ToastUtil.shortShow(it) }
            }
            success {
                hideProgressLoading()
                if (data.isNullOrEmpty()) return@success
                adapter.setList(data)
            }
        })

        mViewMode.getDetailByLearnMoreId.observe(this, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                hideProgressLoading()
                errorMsg?.let { ToastUtil.shortShow(it) }
            }
            success {
                hideProgressLoading()
                finishUsuallyPop.setData(data)
                pop
                    .isDestroyOnDismiss(false)
                    .enableDrag(true)
                    .maxHeight(dp2px(700f))
                    .dismissOnTouchOutside(false)
                    .asCustom(finishUsuallyPop).show()
            }
        })
    }

    override fun initData() {
        mViewMode.howTo()

        adapter.addChildClickViewIds(R.id.cl_content)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            (adapter.data[position] as? MyTroubleData.Bean)?.learnMoreId?.let {
                mViewMode.getDetailByLearnMoreId(it)
            }
        }
    }

    /**
     * 发送支持邮件
     */
    private fun sendEmail() {
        /*val uriText = "mailto:growsupport@heyabby.com" + "?subject=" + Uri.encode("Support")
        val uri = Uri.parse(uriText)
        val sendIntent = Intent(Intent.ACTION_SENDTO)
        sendIntent.data = uri
        val pm = context?.packageManager
        // 根据意图查找包
        val activityList = pm?.queryIntentActivities(sendIntent, 0)
        if (activityList?.size == 0) {
            // 弹出框框
            val clipboard =
                context?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
            val clipData = ClipData.newPlainText(null, "growsupport@heyabby.com")
            // 把数据集设置（复制）到剪贴板
            clipboard.setPrimaryClip(clipData)
            XPopup.Builder(context)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(SendEmailTipsPop(context!!)).show()
            return
        }
        try {
            startActivity(Intent.createChooser(sendIntent, "Send email"))
        } catch (ex: ActivityNotFoundException) {
            XPopup.Builder(context)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .asCustom(context?.let { SendEmailTipsPop(it) }).show()
        }*/
        EaseUiHelper.getInstance().startChat(null)
    }
}