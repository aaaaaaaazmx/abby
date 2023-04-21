package com.cl.modules_contact.pop

import android.content.Context
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.util.ViewUtils
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ContactEnvAdapter
import com.cl.modules_contact.databinding.ContactEnvPopBinding
import com.cl.modules_contact.request.ContactEnvData
import com.lxj.xpopup.core.BottomPopupView

/**
 * Trend 环境信息弹窗
 */
class ContactEnvPop(private val context: Context, private val envInfoData: MutableList<ContactEnvData>, val name: String? = null, val url: String?= null) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_env_pop
    }

    private val adapter by lazy {
        ContactEnvAdapter(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<ContactEnvPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ContactEnvPop
            executePendingBindings()

            tvName.text = name

            ViewUtils.setGone(ivAvatar,TextUtils.isEmpty(url))
            ViewUtils.setVisible(TextUtils.isEmpty(url), noheadShow)

            Glide.with(context).load(url)
                .apply(RequestOptions.circleCropTransform())
                .into(ivAvatar)

            noheadShow.text = name?.substring(0,1)

            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter
            adapter.setList(envInfoData)

            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }
}