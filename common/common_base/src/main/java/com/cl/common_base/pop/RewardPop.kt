package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.cl.common_base.R
import com.cl.common_base.adapter.RewardAdapter
import com.cl.common_base.bean.RewardBean
import com.cl.common_base.databinding.ContactPopRewardBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 打赏弹窗
 */
class RewardPop(
    context: Context,
    private val onRewardListener: ((reward: String) -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_pop_reward
    }

    private val rewardAdapter by lazy {
        val list = mutableListOf<RewardBean>()
        list.add(RewardBean("10", true))
        list.add(RewardBean("50", false))
        list.add(RewardBean("100", false))
        list.add(RewardBean("150", false))
        list.add(RewardBean("200", false))
        list.add(RewardBean("500", false))
        RewardAdapter(list)
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<ContactPopRewardBinding>(popupImplView)?.apply {
            lifecycleOwner = this@RewardPop
            executePendingBindings()

            rvReward.layoutManager = GridLayoutManager(context, 3)
            rvReward.adapter = rewardAdapter

            rewardAdapter.addChildClickViewIds(com.cl.common_base.R.id.check_reward)
            rewardAdapter.setOnItemChildClickListener { adapter, view, position ->
                val data = rewardAdapter.data[position] as? RewardBean
                when (view.id) {
                    com.cl.common_base.R.id.check_reward -> {
                        rewardAdapter.data.indexOfFirst {
                            it.isSelected
                        }.apply {
                            if (this != -1) {
                                rewardAdapter.data[this].isSelected = false
                                rewardAdapter.notifyItemChanged(this)
                            }
                        }
                        data?.isSelected = !(data?.isSelected ?: false)
                        rewardAdapter.notifyItemChanged(position)
                    }
                }
            }

            ivClose.setOnClickListener { dismiss() }

            btnSuccess.setOnClickListener {
                rewardAdapter.data.firstOrNull { it.isSelected }?.apply {
                    onRewardListener?.invoke(number)
                    dismiss()
                }
            }

        }
    }
}