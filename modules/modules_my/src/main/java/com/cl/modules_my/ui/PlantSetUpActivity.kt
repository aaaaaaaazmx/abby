package com.cl.modules_my.ui

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.strictmode.RetainInstanceUsageViolation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.DeviceDetailInfo
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.BaseSearchPop
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.adapter.GrowSpaceSetUpAdapter
import com.cl.modules_my.adapter.GrowTypeChooserAdapter
import com.cl.modules_my.databinding.MyPlantSetupActivityBinding
import com.cl.modules_my.pop.MyChooerTipPop
import com.cl.modules_my.pop.MySetUpPop
import com.cl.modules_my.request.DeviceDetailsBean
import com.cl.modules_my.request.MyPlantInfoData
import com.cl.modules_my.viewmodel.GrowSpaceSetViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.smart.android.sweeper.IThingSweeperDataListener
import com.thingclips.smart.camera.middleware.p2p.ThingSmartCameraP2P
import dagger.hilt.android.AndroidEntryPoint
import java.time.chrono.HijrahEra
import javax.inject.Inject

/**
 * This is a short description.
 *
 * @author 李志军 2023-09-17 20:45
 *
 * 植物设置界面
 */
@AndroidEntryPoint
class PlantSetUpActivity : BaseActivity<MyPlantSetupActivityBinding>() {

    @Inject
    lateinit var mViewModel: GrowSpaceSetViewModel

    // 当前数量
    private val number by lazy {
        intent.getIntExtra(KEY_NUMBER, -1)
    }

    // 是否新增
    private val isAdd by lazy {
        intent.getBooleanExtra(KEY_IS_ADD, true)
    }

    // 帐篷信息
    private val growDetailInfo by lazy {
        intent.getSerializableExtra(KEY_GROW_DETAIL_INFO) as? DeviceDetailsBean
    }

    private val pop by lazy {
        val pop = BaseSearchPop(this@PlantSetUpActivity, onItemClickAction = {

        }).setBubbleBgColor(Color.WHITE) //气泡背景
            .setArrowWidth(XPopupUtils.dp2px(this@PlantSetUpActivity, 3f))
            .setArrowHeight(
                XPopupUtils.dp2px(
                    this@PlantSetUpActivity,
                    3f
                )
            )
            //.setBubbleRadius(100)
            .setArrowRadius(
                XPopupUtils.dp2px(
                    this@PlantSetUpActivity,
                    3f
                )
            )
        pop as? BaseSearchPop
    }

    private val adapter by lazy {
        GrowSpaceSetUpAdapter(mutableListOf(), onEditDoAfterAction = { editable, editText, clRoot ->
            // 联想搜索
            xpopup(this@PlantSetUpActivity) {
                atView(editText)
                isDestroyOnDismiss(true)
                dismissOnTouchOutside(true)
                isViewMode(true)
                isRequestFocus(false)
                isClickThrough(true)
                isTouchThrough(true)
                hasShadowBg(false)
                positionByWindowCenter(true)
                popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                val basePop = asCustom(pop)

                pop?.setData(editText.text.toString(), editText)
                mViewModel.getName(editText.text.toString())
                if(editText.text.toString().isEmpty()){
                    basePop.dismiss()
                    return@xpopup
                }
                if(basePop.isDismiss){
                    basePop.show()
                }
            }

        })
    }

    override fun initView() {
        binding.title
            .setRightButtonTextBack(R.drawable.background_check_tags_r5)
            .setRightButtonText(getString(com.cl.common_base.R.string.string_1393))
            .setRightButtonTextSize(13f)
            .setRightButtonTextHeight(25f)
            .setRightButtonTextColor(Color.WHITE)
            .setRightClickListener {
                if (isAdd) {
                    // 如果是添加
                    (adapter.data as? MutableList<DeviceDetailInfo>)?.let {
                        growDetailInfo?.apply {
                            list = it
                            mViewModel.addDevice(this)
                        }
                    }
                } else {
                    // 如果是编辑
                    (adapter.data as? MutableList<DeviceDetailInfo>)?.let {
                        growDetailInfo?.apply {
                            mViewModel.updateDeviceInfo(
                                UpDeviceInfoReq(
                                    deviceId = deviceId,
                                    list = it,
                                    spaceName = spaceName,
                                    spaceSize = spaceSize,
                                    numPlant = numPlant.toString(),
                                    ledWattage = ledWattage
                                )
                            )
                        }
                    }
                }
            }


        binding.rvPlantSet.layoutManager = LinearLayoutManager(this@PlantSetUpActivity)
        binding.rvPlantSet.adapter = this@PlantSetUpActivity.adapter
        binding.rvPlantSet.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (pop?.isShow == true) pop?.dismiss()
                // dx 和 dy 分别表示水平和垂直方向的滚动距离
                // 在这里，你可以添加你自己的逻辑
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // newState 表示新的滚动状态
                // 在这里，你可以添加你自己的逻辑
            }
        })


        // 暂时编辑帐篷不能填写
        // 获取列表的大小，如果列表为空则默认为0
        val existingSize = growDetailInfo?.list?.size ?: 0
        when {
            null == growDetailInfo || existingSize == 0 -> {
                // 如果列表为空，则创建新列表
                val newList = List(number) {
                    DeviceDetailInfo(
                        attribute = "Photo",
                        plantWay = "Seed",
                        plantName = "${growDetailInfo?.spaceName}${it + 1}",
                    )
                }
                adapter.setList(newList)
            }

            existingSize > number -> {
                // 如果现有列表的大小大于输入的数值，则删除多余的元素
                growDetailInfo?.list?.let { list ->
                    list.subList(number, existingSize).clear()
                    adapter.setList(list)
                }
            }

            existingSize < number -> {
                // 如果现有列表的大小小于输入的数值，则添加额外的元素
                growDetailInfo?.list?.also { list ->
                    val toBeAdded = List(number - existingSize) {
                        DeviceDetailInfo(
                            attribute = "Photo",
                            plantWay = "Seed",
                            plantName = "grow space name${existingSize + it}"
                        )
                    }
                    list.addAll(toBeAdded)
                    adapter.setList(list)
                }
            }

            else -> {
                // 如果现有列表的大小等于输入的数值，则不进行任何操作
                adapter.setList(growDetailInfo?.list)
            }
        }


    }


    override fun observe() {
        mViewModel.apply {
            stranNameList.observe(this@PlantSetUpActivity, resourceObserver {
                success {
                    pop?.setDatas(data)
                }
            })

            addDevice.observe(this@PlantSetUpActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                        .navigation()
                }
            })

            updateDeviceInfo.observe(this@PlantSetUpActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                        .navigation()
                }
            })
        }
    }

    override fun initData() {
        adapter.addChildClickViewIds(
            com.cl.modules_my.R.id.tv_attribute,
            com.cl.modules_my.R.id.tv_plant_way,
            com.cl.modules_my.R.id.rl_syne_type,
            com.cl.modules_my.R.id.rl_sync_strain,

            )
        adapter.setOnItemChildClickListener { adapter, view, position ->
            if (adapter.data.isEmpty()) return@setOnItemChildClickListener
            val dataInfo = adapter.data[position] as? DeviceDetailInfo
            when (view.id) {
                com.cl.modules_my.R.id.tv_attribute -> {
                    if (!isAdd) {
                        ToastUtil.shortShow(getString(R.string.string_1849))
                        return@setOnItemChildClickListener
                    }
                    val newList = List(2) {
                        MyPlantInfoData(
                            plantName = if (it == 0) "Photo" else "Auto",
                            isSelected = false
                        )
                    }

                    pop(view, newList, dataInfo?.attribute) {
                        dataInfo?.attribute = it
                        val newList = getPlantWayList(it)
                        if (newList.isNotEmpty()) {
                            dataInfo?.plantWay = newList[0].plantName
                        }
                        adapter.notifyItemChanged(position)
                    }
                }

                com.cl.modules_my.R.id.tv_plant_way -> {
                    if (!isAdd) {
                        ToastUtil.shortShow(getString(R.string.string_1849))
                        return@setOnItemChildClickListener
                    }
                    val newList = getPlantWayList(dataInfo?.attribute)
                    pop(view, newList, dataInfo?.plantWay) {
                        dataInfo?.plantWay = it
                        adapter.notifyItemChanged(position)
                    }
                }

                com.cl.modules_my.R.id.rl_sync_strain -> {
                    // Find the CheckBox for syncing strain names
                    val syncStrainCheckbox =
                        view.findViewById<CheckBox>(com.cl.modules_my.R.id.cb_sync)
                    // Toggle the checkbox state
                    val isCheckboxChecked = !syncStrainCheckbox.isChecked
                    syncStrainCheckbox.isChecked = isCheckboxChecked
                    // If the checkbox is checked, proceed with syncing
                    // Return early if the adapter data is empty
                    if (adapter.data.isEmpty()) return@setOnItemChildClickListener
                    // Safely cast and update the strain names if the checkbox is checked
                    (adapter.data as? MutableList<DeviceDetailInfo>)?.let { list ->
                        if (list.isEmpty()) return@setOnItemChildClickListener
                        list.forEachIndexed { index, deviceDetailInfo ->
                            if (isCheckboxChecked) {
                                deviceDetailInfo.strainName = list[0].strainName
                            }
                            deviceDetailInfo.isSyncStrainCheck = isCheckboxChecked
                            // 这里不需要重新设置适配器数据，因为我们已经直接修改了原有列表中的元素
                            this@PlantSetUpActivity.adapter.notifyItemChanged(index)
                        }
                    }

                }

                com.cl.modules_my.R.id.rl_syne_type -> {
                    // 同步当前的植物名字
                    // Find the CheckBox for syncing strain names
                    val syncTypeCheckbox =
                        view.findViewById<CheckBox>(com.cl.modules_my.R.id.cb_sync_type)
                    // Toggle the checkbox state
                    val isCheckboxChecked = !syncTypeCheckbox.isChecked
                    syncTypeCheckbox.isChecked = isCheckboxChecked
                    // If the checkbox is checked, proceed with syncing
                    // Return early if the adapter data is empty
                    if (adapter.data.isEmpty()) return@setOnItemChildClickListener
                    // Safely cast and update the strain names if the checkbox is checked
                    (adapter.data as? MutableList<DeviceDetailInfo>)?.let { list ->
                        if (list.isEmpty()) return@setOnItemChildClickListener
                        list.forEachIndexed { index, deviceDetailInfo ->
                            if (isCheckboxChecked) {
                                deviceDetailInfo.attribute = list[0].attribute
                                deviceDetailInfo.plantWay = list[0].plantWay
                            }
                            deviceDetailInfo.isSyncTypeCheck = isCheckboxChecked
                            // 这里不需要重新设置适配器数据，因为我们已经直接修改了原有列表中的元素
                            this@PlantSetUpActivity.adapter.notifyItemChanged(index)
                        }
                    }
                }
            }
        }
    }

    private fun getPlantWayList(attribute: String?): List<MyPlantInfoData> {
        val newList = if (attribute == "Photo") {
            List(2) {
                MyPlantInfoData(
                    plantName = when (it) {
                        0 -> "Clone"
                        1 -> "Seed"
                        else -> ""
                    },
                    isSelected = false
                )
            }
        } else {
            List(2) {
                MyPlantInfoData(
                    plantName = when (it) {
                        0 -> "Clone"
                        1 -> "Seeding"
                        else -> ""
                    },
                    isSelected = false
                )
            }
        }
        return newList
    }

    private fun pop(
        view: View,
        newList: List<MyPlantInfoData>,
        selectName: String? = null,
        names: ((name: String) -> Unit)? = null,
    ) {
        xpopup(this@PlantSetUpActivity) {
            isDestroyOnDismiss(false)
            dismissOnTouchOutside(true)
            isClickThrough(false)
            hasShadowBg(true)
            atView(view)
            isCenterHorizontal(false)
            asCustom(
                MySetUpPop(
                    this@PlantSetUpActivity,
                    plantName = selectName,
                    list = newList.toMutableList(),
                    onConfirmAction = { name ->
                        names?.invoke(name)
                    }
                )
                    .setBubbleBgColor(Color.WHITE) //气泡背景
                    .setArrowWidth(XPopupUtils.dp2px(this@PlantSetUpActivity, 3f))
                    .setArrowHeight(
                        XPopupUtils.dp2px(
                            this@PlantSetUpActivity,
                            3f
                        )
                    )
                    //.setBubbleRadius(100)
                    .setArrowRadius(
                        XPopupUtils.dp2px(
                            this@PlantSetUpActivity,
                            3f
                        )
                    )
            ).show()
        }
    }

    companion object {
        const val KEY_NUMBER = "number"
        const val KEY_IS_ADD = "key_is_add"
        const val KEY_GROW_DETAIL_INFO = "key_grow_detail_info"
    }
}