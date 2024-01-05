package com.cl.common_base.base

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.mtjsoft.barcodescanning.extentions.dp2px
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.adapter.HomeKnowMoreAdapter
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.SnoozeReq
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.databinding.HomeKnowMoreLayoutBinding
import com.cl.common_base.video.videoUiHelp
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.sp2px
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 统一图文接口
 */
@Route(path = RouterPath.Home.PAGE_KNOW)
@AndroidEntryPoint
class KnowMoreActivity : BaseActivity<HomeKnowMoreLayoutBinding>() {
    /**
     * 是否展示固定按钮、师傅哦展示滑动解锁按钮、滑动解锁按钮文案
     */
    private val isShowButton by lazy { intent.getBooleanExtra(KEY_IS_SHOW_BUTTON, false) }
    private val showButtonText by lazy { intent.getStringExtra(KEY_IS_SHOW_BUTTON_TEXT) }
    private val isShowUnlockButton by lazy {
        intent.getBooleanExtra(
            KEY_IS_SHOW_UNLOCK_BUTTON,
            false
        )
    }
    private val unLockButtonEngage by lazy { intent.getStringExtra(KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE) }

    /**
     * 固定按钮的意图、滑动解锁的意图
     */
    private val isJumpPage by lazy { intent.getBooleanExtra(KEY_INTENT_JUMP_PAGE, false) }
    private val isUnlockTask by lazy { intent.getBooleanExtra(KEY_INTENT_UNLOCK_TASK, false) }

    /**
     * 用于固定解锁的或者跳转的id
     */
    private val fixedId by lazy { intent.getStringExtra(KEY_FIXED_TASK_ID) }

    /**
     * 解锁ID
     */
    private val unLockId by lazy { intent.getStringExtra(KEY_UNLOCK_TASK_ID) }

    /**
     * 文字颜色
     */
    private val titleColor by lazy { intent.getStringExtra(BasePopActivity.KEY_TITLE_COLOR) }

    /**
     * veg、auto展示ID
     */
    private val categoryCode by lazy { intent.getStringExtra(BasePopActivity.KEY_CATEGORYCODE) }

    /**
     * 设备Id
     */
    private val deviceId by lazy { intent.getStringExtra(BasePopActivity.KEY_DEVICE_ID) }

    /**
     * 配件Id
     */
    private val accessoryId by lazy { intent.getStringExtra(BasePopActivity.KEY_PART_ID) }

    /**
     * 植物Id
     */
    private val plantId by lazy { intent.getStringExtra(BasePopActivity.KEY_PLANT_ID) }

    /**
     * 摄像头Id
     */
    // private val cameraId by lazy { intent.getStringExtra(BasePopActivity.KEY_CAMERA_ID) }

    /**
     * 是否是预览
     */
    private val isPreview by lazy { intent.getBooleanExtra(BasePopActivity.KEY_PREVIEW, false) }

    /**
     * 共享设备类型
     */
    private val spaceType by lazy { intent.getStringExtra(BasePopActivity.KEY_SHARE_TYPE) }

    /**
     * 删除配件时，关联IdrelationId
     */
    private val relationId by lazy { intent.getStringExtra(BasePopActivity.KEY_RELATION_ID) }

    @Inject
    lateinit var mViewMode: KnowMoreViewModel

    // 富文本适配器
    private val adapter by lazy {
        HomeKnowMoreAdapter(mutableListOf())
    }
    private val linearLayoutManager by lazy {
        LinearLayoutManager(this@KnowMoreActivity)
    }

    private val txtId by lazy {
        intent.getStringExtra(Constants.Global.KEY_TXT_ID)
    }

    private val txtType by lazy {
        intent.getStringExtra(Constants.Global.KEY_TXT_TYPE)
    }

    private val taskId by lazy {
        intent.getStringExtra(Constants.Global.KEY_TASK_ID)
    }

    // taksNo
    private val taskNo by lazy {
        intent.getStringExtra(BasePopActivity.KEY_TASK_NO)
    }

    override fun initView() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.unbindCamera.setSafeOnClickListener(lifecycleScope) {
            // 解绑设备
            xpopup(this@KnowMoreActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(false)
                asCustom(
                    BaseCenterPop(
                        this@KnowMoreActivity,
                        content = "Are you sure you want to delete it?",
                        isShowCancelButton = true,
                        cancelText = "No",
                        confirmText = "Yes",
                        onConfirmAction = {
                            if (!relationId.isNullOrEmpty()) {
                                // 删除当前设备的配件
                                mViewMode.cameraSetting(UpdateInfoReq(binding = false, deviceId = deviceId, relationId = relationId))
                            } else {
                                // 删除共享设备
                                mViewMode.deleteDevice(deviceId.toString())
                            }
                        })
                ).show()
            }
        }

        binding.rvKnow.layoutManager = linearLayoutManager
        binding.rvKnow.adapter = adapter
        logI("txtId = $txtId, type = $txtType")
        mViewMode.getRichText(txtId = txtId, type = txtType, taskId = null)
        // 学院任务一进来就已读。
        when (txtType) {
            CalendarData.TASK_TYPE_TEST -> {
                mViewMode.finishTask(FinishTaskReq(taskId = taskId))
            }
        }

        // 是否展示固定按钮、是否展示滑动解锁
        ViewUtils.setVisible(isShowButton && !isPreview, binding.btnNext)
        ViewUtils.setVisible(isShowUnlockButton && !isPreview, binding.slideToConfirm)
        // 是否显示删除按钮
        ViewUtils.setVisible(!spaceType.isNullOrEmpty(), binding.unbindCamera)
        binding.btnNext.text = showButtonText ?: "Next"
        binding.btnNext.setOnClickListener {
            fixedProcessingLogic()
        }
        /*binding.slideToConfirm.setEngageText(unLockButtonEngage ?: "Slide to Unlock")*/
        // 滑动解锁按钮的文案由后台下发
        binding.slideToConfirm.setEngageText(mViewMode.sliderText.value ?: unLockButtonEngage ?: "Slide to Next")
        binding.slideToConfirm.slideListener = object : ISlideListener {
            override fun onSlideStart() {
            }

            override fun onSlideMove(percent: Float) {
            }

            override fun onSlideCancel() {
            }

            override fun onSlideDone() {
                binding.slideToConfirm.postDelayed(Runnable { binding.slideToConfirm.reset() }, 500)
                // 解锁完毕、调用解锁功能
                fixedProcessingLogic()
            }

        }
    }

    /**
     * 固定跳转逻辑判断
     */
    private fun fixedProcessingLogic() {
        if (!isHaveCheckBoxViewType()) return
        if (isJumpPage) {
            fixedId?.let {
                // 这是个动态界面，我也不知道为什么不做成动态按钮
                when (it) {
                    Constants.Fixed.KEY_FIXED_ID_A_FEW_TIPS -> {
                        ARouter.getInstance().build(RouterPath.Home.PAGE_PLANT_NAME)
                            .navigation(this@KnowMoreActivity)
                    }

                    Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED -> {
                        // 如果是准备种子、那么直接跳转到种子界面
                        val intent = Intent(this@KnowMoreActivity, KnowMoreActivity::class.java)
                        intent.putExtra(
                            Constants.Global.KEY_TXT_ID,
                            Constants.Fixed.KEY_FIXED_ID_SEED_GERMINATION_PREVIEW
                        )
                        intent.putExtra(
                            BasePopActivity.KEY_FIXED_TASK_ID,
                            Constants.Fixed.KEY_FIXED_ID_SEED_GERMINATION_PREVIEW
                        )
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(
                            BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE,
                            "Slide to Unlock"
                        )
                        startActivity(intent)
                    }

                    Constants.Fixed.KEY_FIXED_ID_ACTION_NEEDED -> {
                        // 这是是直接调用接口
                        mViewMode.intoPlantBasket()
                    }

                    // 种植前检查
                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_CLONE_CHECK,
                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_SEED_CHECK -> {
                        val intent = Intent(this@KnowMoreActivity, BasePopActivity::class.java)
                        intent.putExtra(
                            Constants.Global.KEY_TXT_ID,
                            Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_1
                        )
                        intent.putExtra(
                            BasePopActivity.KEY_FIXED_TASK_ID,
                            Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_1
                        )
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(BasePopActivity.KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(BasePopActivity.KEY_CATEGORYCODE, categoryCode)
                        intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(
                            BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE,
                            "Slide to Next"
                        )
                        startActivity(intent)
                    }

                    // 解锁Veg\auto这个周期\或者重新开始
                    Constants.Fixed.KEY_FIXED_ID_AUTOFLOWERING_STAGE_PREVIEW,
                    Constants.Fixed.KEY_FIXED_ID_VEGETATIVE_STAGE_PREVIEW -> {
                        if (unLockId.isNullOrEmpty()) {
                            // startRunning 接口
                            mViewMode.startRunning(botanyId = "", goon = false)
                        } else {
                            // 解锁接口
                            mViewMode.finishTask(FinishTaskReq(taskId = unLockId))
                            mViewMode.checkPlant()
                        }
                    }


                    else -> {
                        // 跳转下一页
                        val intent = Intent(this@KnowMoreActivity, BasePopActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, fixedId)
                        startActivity(intent)
                    }
                }
                return
            }
        }

        if (isUnlockTask) {
            fixedId?.let {
                when (it) {
                    Constants.Fixed.KEY_FIXED_ID_PREPARE_UNLOCK_PERIOD -> {
                        mViewMode.getUnLockNow(plantId.toString())
                    }

                    // 如果是预览界面、那么直接开始种植、然后关闭界面
                    Constants.Fixed.KEY_FIXED_ID_SEED_GERMINATION_PREVIEW -> {
                        mViewMode.startRunning(botanyId = "", goon = false)
                    }

                    // 种子发芽
                    Constants.Fixed.KEY_FIXED_ID_WATER_CHANGE_GERMINATION -> {
                        acFinish()
                    }

                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_1 -> {
                        val intent = Intent(this@KnowMoreActivity, BasePopActivity::class.java)
                        intent.putExtra(BasePopActivity.KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(
                            Constants.Global.KEY_TXT_ID,
                            Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_2
                        )
                        intent.putExtra(
                            BasePopActivity.KEY_FIXED_TASK_ID,
                            Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_2
                        )
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(BasePopActivity.KEY_CATEGORYCODE, categoryCode)
                        intent.putExtra(
                            BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE,
                            "Slide to Next"
                        )
                        startActivity(intent)
                    }

                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_2 -> {
                        val intent = Intent(this@KnowMoreActivity, BasePopActivity::class.java)
                        intent.putExtra(BasePopActivity.KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(
                            Constants.Global.KEY_TXT_ID,
                            Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_3
                        )
                        intent.putExtra(
                            BasePopActivity.KEY_FIXED_TASK_ID,
                            Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_3
                        )
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(BasePopActivity.KEY_CATEGORYCODE, categoryCode)
                        intent.putExtra(
                            BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE,
                            "Slide to Unlock"
                        )
                        startActivity(intent)
                    }

                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_3 -> {
                        val intent = Intent(this@KnowMoreActivity, BasePopActivity::class.java)
                        intent.putExtra(BasePopActivity.KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(
                            Constants.Global.KEY_TXT_ID,
                            if (categoryCode == "100002" || categoryCode == "100004") Constants.Fixed.KEY_FIXED_ID_AUTOFLOWERING_STAGE_PREVIEW else Constants.Fixed.KEY_FIXED_ID_VEGETATIVE_STAGE_PREVIEW
                        )
                        intent.putExtra(
                            BasePopActivity.KEY_FIXED_TASK_ID,
                            if (categoryCode == "100002" || categoryCode == "100004") Constants.Fixed.KEY_FIXED_ID_AUTOFLOWERING_STAGE_PREVIEW else Constants.Fixed.KEY_FIXED_ID_VEGETATIVE_STAGE_PREVIEW
                        )
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                        intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(
                            BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT,
                            if (categoryCode == "100002" || categoryCode == "100004") "Unlock Autoflowering" else "Unlock Veg"
                        )
                        startActivity(intent)
                    }
                    // 自动模式
                    Constants.Fixed.KEY_FIXED_ID_MANUAL_MODE -> {
                        // 打开手动模式
                        mViewMode.updateDeviceInfo(
                            UpDeviceInfoReq(
                                proMode = "On",
                                deviceId = mViewMode.userInfo?.deviceId
                            )
                        )
                        /*mViewMode.startRunning(botanyId = "", goon = false)*/
                    }
                    // 新增配件
                    Constants.Fixed.KEY_FIXED_ID_NEW_ACCESSORIES -> {
                        letMultiple(accessoryId, deviceId) { a, b ->
                            // 新增配件接口
                            mViewMode.addAccessory(a, b)
                        }
                    }

                    else -> {
                        mViewMode.finishTask(FinishTaskReq(taskId = it))
                    }
                }
                return
            }
        }

        if (!isJumpPage && !isUnlockTask) {
            // 如果都不是、那么直接关闭界面
            acFinish()
        }
    }

    private fun isHaveCheckBoxViewType(): Boolean {
        /*logI("123123:::: ${adapter.data.filter { data -> data.value?.isCheck == false }.size}")*/
        val size =
            adapter.data.filter { data -> data.value?.isCheck == false && data.type == "option" }.size
        size.let { checkCount ->
            if (checkCount != 0) {
                ToastUtil.shortShow("Please select all item")
            }
            return checkCount == 0
        }
    }

    /**
     * 初始化Video
     */
    private fun initVideo(url: String, autoPlay: Boolean) {
        binding.videoItemPlayer.apply {
            videoUiHelp(url, -1)
            if (autoPlay) startPlayLogic()
        }
    }

    override fun observe() {
        mViewMode.apply {
            // 删除当前设备下的配件
            saveCameraSetting.observe(this@KnowMoreActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                        .navigation(this@KnowMoreActivity)
                }
                loading { showProgressLoading() }
            })
            // 删除配件设备
            deleteDevice.observe(this@KnowMoreActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                        .navigation(this@KnowMoreActivity)
                }
            })
            unLockNow.observe(this@KnowMoreActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    acFinish()
                }
            })

            updateDeviceInfo.observe(this@KnowMoreActivity, resourceObserver {
                success {
                    if (fixedId == Constants.Fixed.KEY_FIXED_ID_MANUAL_MODE) {
                        // 更新涂鸦Bean
                        ThingHomeSdk.newHomeInstance(mViewMode.homeId)
                            .getHomeDetail(object : IThingHomeResultCallback {
                                @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
                                override fun onSuccess(bean: HomeBean?) {
                                    bean?.let { it ->
                                        val arrayList = it.deviceList as ArrayList<DeviceBean>
                                        logI("123123123: ${arrayList.size}")
                                        arrayList.firstOrNull { dev -> dev.devId == deviceId }
                                            .apply {
                                                // 在线的、数据为空、是盒子的
                                                if (null == this && mViewMode.userInfo?.spaceType == ListDeviceBean.KEY_SPACE_TYPE_BOX && mViewMode.userInfo?.deviceOnlineStatus == "1") {
                                                    /*val aa = mViewMode.thingDeviceBean
                                                    aa()?.devId = mViewMode.deviceId.value
                                                    GSON.toJson(aa)?.let {
                                                        Prefs.putStringAsync(
                                                            Constants.Tuya.KEY_DEVICE_DATA,
                                                            it
                                                        )
                                                    }
                                                    return@applyh*/
                                                    ToastUtil.shortShow("Device error, please re-pair with the device")
                                                }
                                                GSON.toJson(this)?.let {
                                                    Prefs.putStringAsync(
                                                        Constants.Tuya.KEY_DEVICE_DATA,
                                                        it
                                                    )
                                                }

                                                // 重新注册服务
                                                // 开启服务
                                                val intent = Intent(
                                                    this@KnowMoreActivity,
                                                    TuYaDeviceUpdateReceiver::class.java
                                                )
                                                startService(intent)
                                                // 切换之后需要重新刷新所有的东西
                                                mViewMode.checkPlant()
                                            }
                                    }
                                }

                                override fun onError(errorCode: String?, errorMsg: String?) {

                                }
                            })
                    }
                }
            })

            startRunning.observe(this@KnowMoreActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
            })

            // 新增配件
            addAccessory.observe(this@KnowMoreActivity, resourceObserver {
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

            // 植物检查
            checkPlant.observe(this@KnowMoreActivity, resourceObserver {
                error { errorMsg, _ ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    hideProgressLoading()
                    data?.let { PlantCheckHelp().plantStatusCheck(this@KnowMoreActivity, it, true) }
                }
            })

            // 插入篮子植物接口
            intoPlantBasket.observe(this@KnowMoreActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    ARouter.getInstance().build(RouterPath.Main.PAGE_MAIN).navigation()
                    acFinish()
                }
            })
            // 完成任务
            finishTask.observe(this@KnowMoreActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    // 学院任务不管
                    if (txtType == CalendarData.TASK_TYPE_TEST) return@success

                    hideProgressLoading()
                    setResult(Activity.RESULT_OK)
                    this@KnowMoreActivity.finish()
                }
            })

            richText.observe(this@KnowMoreActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // 初始化头部Video
                    data.topPage?.firstOrNull { it.type == "video" }?.apply {
                        // 显示头部视频
                        binding.videoItemPlayer.visibility = View.VISIBLE
                        value?.url?.let { initVideo(it, value.autoplay == true) }
                    }
                    data.bar?.let {
                        // todo 设置标题
                        binding.tvTitle.text = it
                    }

                    // 动态添加按钮
                    // 不是video的都需要添加
                    val list = data.topPage?.filter { it.type != "video" }
                    list?.forEachIndexed { index, topPage ->
                        if (!isPreview) {
                            // 乱七八糟的，不管啥都返回这个这个
                            /*val tv = TextView(this@KnowMoreActivity)
                            tv.setBackgroundResource(R.drawable.create_state_button)
                            tv.isEnabled = true
                            tv.text = topPage.value?.txt
                            val lp = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                dp2px(60)
                            )
                            lp.setMargins(dp2px(20), dp2px(10), dp2px(20), dp2px(0))
                            tv.layoutParams = lp
                            tv.gravity = Gravity.CENTER
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp2px(18f).toFloat())
                            tv.setTextColor(Color.WHITE)
                            binding.flRoot.addView(tv)*/
                        }
                    }

                    // 滑动结果按钮文案
                    mViewMode.getSliderText(data.topPage?.firstOrNull { it.type == "finishTask" }?.let {
                        binding.slideToConfirm.setEngageText(it.value?.txt ?: "Slide to Unlock")
                        it.value?.txt
                    })

                    binding.flRoot.children.forEach {
                        val tv = (it as? TextView)
                        tv?.setOnClickListener {
                            list?.firstOrNull { data -> data.value?.txt == tv.text.toString() }
                                ?.apply {
                                    when (type) {
                                        "pageClose" -> this@KnowMoreActivity.finish()
                                        "pageDown" -> {
                                            if (!isHaveCheckBoxViewType()) return@setOnClickListener

                                            // 跳转下一页
                                            val intent = Intent(
                                                this@KnowMoreActivity,
                                                KnowMoreActivity::class.java
                                            )
                                            intent.putExtra(
                                                Constants.Global.KEY_TXT_ID,
                                                value?.txtId
                                            )
                                            startActivity(intent)
                                        }

                                        "finishTask" -> {
                                            if (!isHaveCheckBoxViewType()) return@setOnClickListener

                                            // 完成任务
                                            mViewMode.finishTask(FinishTaskReq(taskId = taskId))
                                        }
                                    }
                                }
                        }

                    }

                    // 适配器设置数据
                    data.page?.map { it.copy(isPreview = isPreview) }?.let { adapter.setList(it) }
                }
            })
        }
    }

    override fun initData() {
        scrollListener()
        adapterClickEvent()
    }

    private fun scrollListener() {
        binding.rvKnow.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                //大于0说明有播放
                if (GSYVideoManager.instance().playPosition >= 0) {
                    //当前播放的位置
                    val position = GSYVideoManager.instance().playPosition
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag == "$position" && (position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        //是否全屏
                        if (!GSYVideoManager.isFullState(this@KnowMoreActivity)) {
                            adapter.data[position].videoPosition =
                                GSYVideoManager.instance().currentPosition
                            // 不释放全部
                            // GSYVideoManager.instance().setListener(this@KnowMoreActivity)
                            // GSYVideoManager.onPause()
                            // 释放全部
                            GSYVideoManager.releaseAllVideos()
                            adapter.notifyItemChanged(position)
                        }
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    /**
     * 适配器的点击事件
     */
    private fun adapterClickEvent() {
        adapter.apply {
            addChildClickViewIds(
                R.id.iv_pic,
                R.id.tv_html,
                R.id.tv_learn,
                R.id.cl_go_url,
                R.id.cl_support,
                R.id.cl_discord,
                R.id.cl_learn,
                R.id.cl_check,
                R.id.tv_page_txt,
                R.id.tv_txt,
                R.id.tv_delay_task,
            )
            setOnItemChildClickListener { _, view, position ->
                val bean = data[position]
                when (view.id) {
                    com.cl.common_base.R.id.iv_pic -> {
                        // 弹出图片
                        XPopup.Builder(context)
                            .asImageViewer(
                                (view as? ImageView),
                                bean.value?.url,
                                SmartGlideImageLoader()
                            )
                            .show()
                    }

                    // 跳转HTML
                    R.id.cl_go_url,
                    com.cl.common_base.R.id.tv_html -> {
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, bean.value?.title)
                        context.startActivity(intent)
                    }

                    // 阅读更多
                    R.id.cl_learn,
                    R.id.tv_learn -> {
                        // todo 请求id
                        bean.value?.txtId?.let {
                            // 继续请求弹窗
                            val intent = Intent(context, KnowMoreActivity::class.java)
                            intent.putExtra(Constants.Global.KEY_TXT_ID, it)
                            context.startActivity(intent)
                        }
                    }

                    // 跳转到客服
                    R.id.cl_support -> {
                        InterComeHelp.INSTANCE.openInterComeHome()
                    }

                    // 跳转到Discord
                    R.id.cl_discord -> {
                        val intent = Intent(context, WebActivity::class.java)
                        if (bean.value?.url.isNullOrEmpty()) {
                            intent.putExtra(
                                WebActivity.KEY_WEB_URL,
                                "https://discord.gg/FCj6UGCNtU"
                            )
                        } else {
                            intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        }
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "hey abby")
                        context.startActivity(intent)
                    }
                    // 勾选框
                    R.id.cl_check -> {
                        view.findViewById<CheckBox>(R.id.curing_box)?.apply {
                            logI("before: ${data[position].value?.isCheck}")
                            data[position].value?.isCheck = !isChecked
                            isChecked = !isChecked
                            logI("after: ${data[position].value?.isCheck}")
                        }
                    }
                    // 跳转到HTML
                    R.id.tv_page_txt -> {
                        if (bean.value?.url.isNullOrEmpty()) return@setOnItemChildClickListener
                        // 跳转到HTML
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        context.startActivity(intent)
                    }

                    R.id.tv_txt -> {
                        if (!bean.value?.articleId.isNullOrEmpty()) {
                            InterComeHelp.INSTANCE.openInterComeSpace(space = InterComeHelp.InterComeSpace.Article, id = bean.value?.articleId)
                            return@setOnItemChildClickListener
                        }
                        if (!bean.value?.url.isNullOrEmpty()) {
                            // 跳转到HTML
                            val intent = Intent(context, WebActivity::class.java)
                            intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                            context.startActivity(intent)
                            return@setOnItemChildClickListener
                        }

                    }
                    // 延迟任务
                    R.id.tv_delay_task -> {
                        // 应该是传过来的taskId
                        mViewMode.delayTask(SnoozeReq(taskId = taskId, taskNo = taskNo))
                    }
                }
            }
        }
    }

    // 关闭页面的回调
    private fun acFinish() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    // 直接关闭
    private fun directShutdown() {
        finish()
    }


    override fun HomeKnowMoreLayoutBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@KnowMoreActivity
            viewModel = mViewMode
            executePendingBindings()
        }
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
        // 添加背景高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.vvRoot) { v, insets ->
            binding.vvRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object {
        const val KEY_IS_SHOW_BUTTON = "key_is_show_button"
        const val KEY_IS_SHOW_UNLOCK_BUTTON = "key_is_show_unlock_button"
        const val KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE = "key_is_show_unlock_button_engage"
        const val KEY_IS_SHOW_BUTTON_TEXT = "key_is_show_button_text"

        // 意图
        const val KEY_INTENT_JUMP_PAGE = "key_intent_jump_page"
        const val KEY_INTENT_UNLOCK_TASK = "key_intent_unlock_task"

        // 用于固定的跳转
        const val KEY_FIXED_TASK_ID = "key_fixed_task_id"

        // 解锁ID
        const val KEY_UNLOCK_TASK_ID = "key_unlock_id"

        // Title颜色
        const val KEY_TITLE_COLOR = "key_title_color"
    }
}