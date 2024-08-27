package com.cl.modules_my.ui

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.xpopup
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.AchievementAdapter
import com.cl.modules_my.databinding.MyAchievementLayoutBinding
import com.cl.modules_my.pop.AchievementPop
import com.cl.modules_my.request.AchievementBean
import com.cl.modules_my.request.ShowAchievementReq
import com.cl.modules_my.viewmodel.AchievementViewModel
import com.luck.picture.lib.utils.DensityUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 成就页面
 */
@AndroidEntryPoint
class AchievementActivity : BaseActivity<MyAchievementLayoutBinding>() {

    @Inject
    lateinit var mViewModel: AchievementViewModel

    private val isAchievement by lazy {
        intent.getBooleanExtra(IS_ACHIEVEMENT, true)
    }

    private val adapter by lazy {
        AchievementAdapter(mutableListOf(), isAchievement)
    }

    override fun initView() {
        binding.featureTitleBar
            .setTitle(if (isAchievement) getString(com.cl.common_base.R.string.string_1839) else getString(com.cl.common_base.R.string.string_1840))
            /*.setRightButtonTextBack(com.cl.common_base.R.drawable.background_check_tags_r5)
            .setRightButtonText("Save")
            .setRightButtonTextSize(13f)
            .setRightButtonTextHeight(25f)
            .setRightButtonTextColor(Color.WHITE)
            .setRightClickListener {
                if (isAchievement) {
                    mViewModel.showAsset(adapter.data.firstOrNull { it.selectedStatus }?.achievementId ?: 0)
                } else {
                    mViewModel.showFrame(adapter.data.firstOrNull { it.selectedStatus }?.goodsId ?: 0)
                }
            }*/

        binding.rvAchievement.layoutManager = GridLayoutManager(this, 3)
        binding.rvAchievement.addItemDecoration(
            GridSpaceItemDecoration(
                3,
                DensityUtil.dip2px(this@AchievementActivity, 4f), DensityUtil.dip2px(this@AchievementActivity, 2f)
            )
        )
        binding.rvAchievement.adapter = adapter
        if (isAchievement) mViewModel.assetList() else mViewModel.frameList()
    }

    override fun observe() {
        mViewModel.apply {
            assetList.observe(this@AchievementActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    adapter.setList(data)
                    binding.featureTitleBar.setTitle("${getString(com.cl.common_base.R.string.string_1839)}( ${data?.filter { it.isGain }?.size ?: 0}/${data?.size ?: 0} )")
                }
            })

            showAsset.observe(this@AchievementActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    // finish()
                }
            })

            frameList.observe(this@AchievementActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    adapter.setList(data)
                    binding.featureTitleBar.setTitle("${getString(com.cl.common_base.R.string.string_1840)}( ${data?.filter { it.isGain }?.size ?: 0}/${data?.size ?: 0} )")
                }
            })

            showFrame.observe(this@AchievementActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    finish()
                }
            })
        }
    }

    override fun initData() {
        adapter.addChildClickViewIds(R.id.my_achievement_item)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val currentDataInfo = adapter.data[position] as? AchievementBean ?: return@setOnItemChildClickListener
            val dataList = adapter.data as? MutableList<AchievementBean> ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.my_achievement_item -> {
                    //  选中操作逻辑
                    /*if (!currentDataInfo.isGain) {
                        val toastMessage = if (isAchievement) "You have not gained this achievement yet" else "You have not gained this frame yet"
                        ToastUtil.shortShow(toastMessage)
                        return@setOnItemChildClickListener
                    }*/

                    // 弹窗详情弹窗
                    xpopup(this@AchievementActivity) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(true)
                        asCustom(
                            AchievementPop(
                                this@AchievementActivity,
                                currentDataInfo.achDescribe,
                                currentDataInfo.backgroundPicture,
                                currentDataInfo.title,
                                currentDataInfo.selectedStatus,
                                currentDataInfo.isGain,
                            ) {
                                // 如果已选中则取消选中
                                if (currentDataInfo.selectedStatus) {
                                    // 取消之前选中的项
                                    dataList.find { it.selectedStatus }?.let {
                                        it.selectedStatus = false
                                        adapter.notifyItemChanged(dataList.indexOf(it))
                                    }
                                    // 保存
                                    if (isAchievement) mViewModel.showAsset(0) else mViewModel.showFrame(0)
                                    return@AchievementPop
                                }

                                // 取消之前选中的项
                                dataList.find { it.selectedStatus }?.let {
                                    it.selectedStatus = false
                                    adapter.notifyItemChanged(dataList.indexOf(it))
                                }

                                // 选中点击的项
                                currentDataInfo.selectedStatus = true
                                adapter.notifyItemChanged(position)

                                // 保存
                                if (isAchievement) mViewModel.showAsset(currentDataInfo.achievementId) else mViewModel.showFrame(currentDataInfo.goodsId)
                            }
                        ).show()
                    }

                    // 如果点击的不是第一项，移动到第一项
                    /*if (position > 0) {
                        dataList.removeAt(position).apply {
                            dataList.add(0, this)
                        }
                        adapter.notifyItemMoved(position, 0)
                        adapter.notifyItemRangeChanged(0, position + 1)
                    }*/
                }
            }
        }
    }

    companion object {
        const val TAG = "AchievementActivity"
        const val IS_ACHIEVEMENT = "is_achievement"
    }
}