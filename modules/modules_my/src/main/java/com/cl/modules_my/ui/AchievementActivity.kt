package com.cl.modules_my.ui

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.AchievementAdapter
import com.cl.modules_my.databinding.MyAchievementLayoutBinding
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
            .setTitle(if (isAchievement) "Achievement" else "Frame")
            .setRightButtonTextBack(com.cl.common_base.R.drawable.background_check_tags_r5)
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
            }

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
                    finish()
                }
            })

            frameList.observe(this@AchievementActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    adapter.setList(data)
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
            when (view.id) {
                R.id.my_achievement_item -> {
                    //  选中操作逻辑
                    when (isAchievement) {
                        true -> {
                            if (!currentDataInfo.isGain) {
                                // 未获得
                                ToastUtil.shortShow("You have not gained this achievement yet")
                                return@setOnItemChildClickListener
                            }
                            // achievement
                            /*dataInfo?.selectedStatus = !(dataInfo?.selectedStatus ?: true)
                            adapter.notifyItemChanged(position)*/
                            // 只能选一个
                            // 先找到已经选中的项目，并取消其选中状态
                            adapter.data.forEachIndexed { index, data ->
                                (data as? AchievementBean)?.let {
                                    if (it.selectedStatus) {
                                        it.selectedStatus = false
                                        adapter.notifyItemChanged(index)
                                    }
                                }
                            }
                            // 选中当前点击的项目
                            currentDataInfo.selectedStatus = true
                            adapter.notifyItemChanged(position)
                        }

                        false -> {
                            if (!currentDataInfo.isGain) {
                                // 未获得
                                ToastUtil.shortShow("You have not gained this frame yet")
                                return@setOnItemChildClickListener
                            }
                            // frame
                            // 只能选一个
                            adapter.data.forEachIndexed { index, data ->
                                (data as? AchievementBean)?.let {
                                    if (it.selectedStatus) {
                                        it.selectedStatus = false
                                        adapter.notifyItemChanged(index)
                                    }
                                }
                            }
                            // 选中当前点击的项目
                            currentDataInfo.selectedStatus = true
                            adapter.notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "AchievementActivity"
        const val IS_ACHIEVEMENT = "is_achievement"
    }
}