package com.cl.modules_my.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.BaseNumberPickPop
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.SoftInputUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.GrowTypeChooserAdapter
import com.cl.modules_my.databinding.MyGrowSpaceActivityBinding
import com.cl.modules_my.request.DeviceDetailsBean
import com.cl.modules_my.request.GrowTypeListDataItem
import com.cl.modules_my.viewmodel.GrowSpaceSetViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This is a short description.
 *
 * @author 李志军 2023-09-17 20:12
 *
 * 添加帐篷设置界面
 */
@AndroidEntryPoint
class GrowSpaceSetActivity : BaseActivity<MyGrowSpaceActivityBinding>() {

    @Inject
    lateinit var mViewModel: GrowSpaceSetViewModel

    // 帐篷的设备Id
    private val tenDeviceId by lazy {
        intent.getStringExtra(KEY_DEVICE_DETAIL_INFO)
    }

    private val typeAdapter by lazy {
        val list = mutableListOf(
            GrowTypeListDataItem(logType = "", showUiText = "2x2", isSelected = false),
            GrowTypeListDataItem(logType = "", showUiText = "3x2", isSelected = false),
            GrowTypeListDataItem(logType = "", showUiText = "4x2", isSelected = false),
            GrowTypeListDataItem(logType = "", showUiText = "5x2", isSelected = false),
            GrowTypeListDataItem(logType = "", showUiText = "3x3", isSelected = false),
            GrowTypeListDataItem(logType = "", showUiText = "4x4", isSelected = false),
            GrowTypeListDataItem(logType = "", showUiText = "5x5", isSelected = false),
            GrowTypeListDataItem(logType = "", showUiText = "8x4", isSelected = false),
        )
        GrowTypeChooserAdapter(list)
    }

    override fun MyGrowSpaceActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@GrowSpaceSetActivity
            isAdd = this@GrowSpaceSetActivity.tenDeviceId.isNullOrEmpty()
            executePendingBindings()
        }
    }


    override fun initView() {
        // 如果是编辑，那么就不能更改植物数量
        binding.etNumberPlant.apply {
            setOnClickListener {
                if (tenDeviceId?.isNotEmpty() == true) {
                    SoftInputUtils.hideSoftInput(this@GrowSpaceSetActivity)
                    ToastUtil.shortShow("Non-editable")
                    return@setOnClickListener
                }
                xpopup(this@GrowSpaceSetActivity) {
                    asCustom(
                        BaseNumberPickPop(
                            this@GrowSpaceSetActivity,
                            showNumber = 1,
                            dataNumber = binding.etNumberPlant.text.safeToInt(),
                            onConfirm = {
                                binding.etNumberPlant.text = "$it"
                            })
                    ).show()
                }
            }
        }

        binding.etLedNumber.apply {
            setOnClickListener {
                xpopup(this@GrowSpaceSetActivity) {
                    asCustom(
                        BaseNumberPickPop(
                            this@GrowSpaceSetActivity,
                            showNumber = 4,
                            dataNumber = binding.etLedNumber.text.safeToInt(),
                            onConfirm = {
                                binding.etLedNumber.text = "$it"
                            })
                    ).show()
                }
            }
        }

        binding.unbindZp.setOnClickListener {
            xpopup(this@GrowSpaceSetActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(false)
                asCustom(BaseCenterPop(this@GrowSpaceSetActivity, content = "Are you certain you wish to delete", isShowCancelButton = true, onConfirmAction = {
                    mViewModel.deleteDevice(tenDeviceId.toString())
                })).show()
            }
        }

        binding.tvSend.setOnClickListener {
            val spaceName = binding.etApsceName.text
            val type = binding.etTypeName.text
            val number =
                runCatching { binding.etNumberPlant.text.toString().toInt() }.getOrDefault(-1)
            val led = binding.etLedNumber.text

            if (spaceName.isNullOrEmpty() || type.isNullOrEmpty() || binding.etNumberPlant.text
                    .isNullOrEmpty() || led.isNullOrEmpty()
            ) {
                ToastUtil.shortShow("Please improve the content")
                return@setOnClickListener
            }

            if (number > 12) {
                ToastUtil.show("The maximum number of plants is 12.")
                return@setOnClickListener
            }

            startActivity(Intent(this@GrowSpaceSetActivity, PlantSetUpActivity::class.java).apply {
                putExtra(
                    PlantSetUpActivity.KEY_NUMBER,
                    number
                )

                // 是否是新增
                putExtra(
                    PlantSetUpActivity.KEY_IS_ADD,
                    tenDeviceId.isNullOrEmpty()
                )

                val dataInfo = mViewModel.deviceDetails.value?.data ?: DeviceDetailsBean()
                dataInfo.let {
                    it.spaceName = binding.etApsceName.text.toString()
                    it.spaceSize = binding.etTypeName.text.toString()
                    it.numPlant = binding.etNumberPlant.text.toString().safeToInt()
                    it.ledWattage = binding.etLedNumber.text.toString()
                }

                // 如果是新增、传递当前帐篷的所有信息
                putExtra(
                    PlantSetUpActivity.KEY_GROW_DETAIL_INFO,
                    dataInfo
                )
            })
        }

        binding.etTypeName.setOnClickListener {
            // 隐藏键盘
            SoftInputUtils.hideSoftInput(this@GrowSpaceSetActivity)
            fadeAnimation(binding.rvType, !binding.rvType.isVisible)
        }

        binding.rvType.apply {
            layoutManager = GridLayoutManager(
                context,
                2, GridLayoutManager.VERTICAL, false
            )
            adapter = typeAdapter

            typeAdapter.addChildClickViewIds(R.id.check_period_chooser)
            typeAdapter.setOnItemChildClickListener { adapter, view, position ->
                if (view.id != R.id.check_period_chooser) return@setOnItemChildClickListener

                val data = adapter.data as? MutableList<GrowTypeListDataItem>
                    ?: return@setOnItemChildClickListener
                val previousSelectedIndex = data.indexOfFirst { it.isSelected }
                var beforeShowUiText: String? = null

                // 如果找到上一个选中的项，则取消选中
                if (previousSelectedIndex != -1 && previousSelectedIndex != position) {
                    beforeShowUiText = data[previousSelectedIndex].showUiText
                    data[previousSelectedIndex].isSelected = false
                    adapter.notifyItemChanged(previousSelectedIndex)
                }

                // 选中当前项
                data[position].isSelected = true
                adapter.notifyItemChanged(position)

                // 显示文案
                binding.etTypeName.setText(data[position].showUiText)

                // 选中后隐藏
                fadeAnimation(binding.rvType, false)
            }

        }

        //获取帐篷的设备信息
        tenDeviceId?.let { mViewModel.getDeviceDetails(it) }
    }

    private fun fadeAnimation(view: View, isVisible: Boolean) {
        if (isVisible) {
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.animate()
                .alpha(1f)
                .setDuration(250) // 250毫秒的持续时间
                .setListener(null)
        } else {
            view.animate()
                .alpha(0f)
                .setDuration(250) // 250毫秒的持续时间
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
        }
    }

    override fun observe() {
        mViewModel.apply {
            // 删除帐篷
            deleteDevice.observe(this@GrowSpaceSetActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                        .navigation(this@GrowSpaceSetActivity)
                }
            })

            deviceDetails.observe(this@GrowSpaceSetActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // 回显的时候赋值
                    data?.let {
                        binding.etApsceName.setText(it.spaceName)
                        binding.etTypeName.setText(it.spaceSize)
                        binding.etNumberPlant.setText("${(it.list?.size ?: 0)}")
                        binding.etLedNumber.setText(it.ledWattage)

                        if (typeAdapter.data.isEmpty()) return@success
                        runCatching {
                            typeAdapter.data.indexOfFirst { listData -> listData.showUiText == it.spaceSize }
                                .apply {
                                    if (this != -1) {
                                        typeAdapter.data[this].isSelected = true
                                        typeAdapter.notifyItemChanged(this)
                                    }
                                }
                        }
                    }
                }
            })
        }
    }

    override fun initData() {
    }

    companion object {
        const val KEY_DEVICE_DETAIL_INFO = "key_device_detail_info"
    }
}