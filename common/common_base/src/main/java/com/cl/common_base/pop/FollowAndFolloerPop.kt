package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.adapter.FollowerAndFolleringAdapter
import com.cl.common_base.adapter.MedialAdapter
import com.cl.common_base.bean.FolowerData
import com.cl.common_base.databinding.FollowPopBinding
import com.cl.common_base.databinding.PopRvBinding
import com.lxj.xpopup.core.BottomPopupView

class FollowAndFolloerPop(context: Context, private val data: MutableList<FolowerData>? = null, private val isFollow: Boolean) : BottomPopupView(context) {

    private val adapter by lazy {
        FollowerAndFolleringAdapter(mutableListOf())
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_rv
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PopRvBinding>(popupImplView)?.apply {
            lifecycleOwner = this@FollowAndFolloerPop
            executePendingBindings()

            tvLogin.text = if (isFollow) context.getString(R.string.string_189) else context.getString(R.string.string_190)

            rv.layoutManager = LinearLayoutManager(context)
            rv.adapter = adapter
            this@FollowAndFolloerPop.adapter.setList(data)
        }
    }
}