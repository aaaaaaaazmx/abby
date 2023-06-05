package com.cl.common_base.pop.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.mtjsoft.barcodescanning.extentions.dp2px
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.adapter.HomeKnowMoreAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.databinding.BasePopActivityBinding
import com.cl.common_base.video.videoUiHelp
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.sp2px
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.XPopup.getAnimationDuration
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.lxj.xpopup.widget.SmartDragLayout
import com.shuyu.gsyvideoplayer.GSYVideoManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import javax.inject.Inject

/**
 * 通用弹窗
 * 富文本页面
 */
@AndroidEntryPoint
class BasePopActivity : BaseActivity<BasePopActivityBinding>() {
    /**
     * 是否展示固定按钮、师傅哦展示滑动解锁按钮、滑动解锁按钮文案
     */
    private val isShowButton by lazy { intent.getBooleanExtra(KEY_IS_SHOW_BUTTON, false) }
    private val showButtonText by lazy { intent.getStringExtra(KEY_IS_SHOW_BUTTON_TEXT) }
    private val isShowUnlockButton by lazy { intent.getBooleanExtra(KEY_IS_SHOW_UNLOCK_BUTTON, false) }
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
     * 用于解锁任务包的packetNo
     */
    private val packetNo by lazy { intent.getStringExtra(KEY_PACK_NO) }

    /**
     * 连续解锁任务包Id
     */
    private val isContinueUnlock by lazy { intent.getBooleanExtra(KEY_TASK_PACKAGE_ID, false) }

    /**
     * 解锁ID
     */
    private val unLockId by lazy { intent.getStringExtra(KEY_UNLOCK_TASK_ID) }

    /**
     * 文字颜色
     */
    private val titleColor by lazy { intent.getStringExtra(KEY_TITLE_COLOR) }

    /**
     * veg、auto展示ID
     */
    private val categoryCode by lazy { intent.getStringExtra(KEY_CATEGORYCODE) }

    /**
     * 一系列的TaskId数组
     */
    private val taskIdList by lazy { (intent.getSerializableExtra(KEY_TASK_ID_LIST) as? MutableList<CalendarData.TaskList.SubTaskList>) ?: mutableListOf() }

    override fun initView() { // 添加状态蓝高度
        //        ViewCompat.setOnApplyWindowInsetsListener(binding.smart) { v, insets ->
        //            binding.smart.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        //                topMargin = insets.systemWindowInsetTop
        //            }
        //            return@setOnApplyWindowInsetsListener insets
        //        }
        binding.smart.setDuration(getAnimationDuration())
        binding.smart.enableDrag(true)
        binding.smart.dismissOnTouchOutside(false)
        binding.smart.isThreeDrag(false)
        binding.smart.open()
        binding.smart.setOnCloseListener(callback)

        binding.ivClose.setOnClickListener { directShutdown() }

        // 是否展示固定按钮、是否展示滑动解锁
        ViewUtils.setVisible(isShowButton, binding.btnNext)
        ViewUtils.setVisible(isShowUnlockButton, binding.slideToConfirm)
        binding.btnNext.text = showButtonText ?: "Next"
        binding.btnNext.setOnClickListener {
            fixedProcessingLogic()
        }
        binding.slideToConfirm.setEngageText(unLockButtonEngage ?: "Slide to Unlock")
        binding.slideToConfirm.slideListener = object : ISlideListener {
            override fun onSlideStart() {
            }

            override fun onSlideMove(percent: Float) {
            }

            override fun onSlideCancel() {
            }

            override fun onSlideDone() {
                binding.slideToConfirm.postDelayed(Runnable { binding.slideToConfirm.reset() }, 500) // 解锁完毕、调用解锁功能
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
            fixedId?.let { // 这是个动态界面，我也不知道为什么不做成动态按钮
                when (it) {
                    Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED -> { // 如果是准备种子、那么直接跳转到种子界面
                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_SEED_GERMINATION_PREVIEW)
                        intent.putExtra(KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_SEED_GERMINATION_PREVIEW)
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Unlock")
                        startActivity(intent)
                    }

                    Constants.Fixed.KEY_FIXED_ID_ACTION_NEEDED -> { // 这是是直接调用接口
                        mViewModel.intoPlantBasket()
                    }

                    // 种植前检查
                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_CLONE_CHECK, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_SEED_CHECK -> {
                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_1)
                        intent.putExtra(KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_1)
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(KEY_CATEGORYCODE, categoryCode)
                        intent.putExtra(KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Next")
                        startActivity(intent)
                    }

                    // 解锁Veg\auto这个周期\或者重新开始
                    Constants.Fixed.KEY_FIXED_ID_AUTOFLOWERING_STAGE_PREVIEW, Constants.Fixed.KEY_FIXED_ID_VEGETATIVE_STAGE_PREVIEW -> {
                        if (unLockId.isNullOrEmpty()) { // startRunning 接口
                            mViewModel.startRunning(botanyId = "", goon = false)
                        } else { // 解锁接口
                            mViewModel.finishTask(FinishTaskReq(taskId = unLockId))
                            mViewModel.tuYaUser?.uid?.let { it1 -> mViewModel.checkPlant(it1) }
                        }
                    }

                    else -> { // 跳转下一页
                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, fixedId)
                        startActivity(intent)
                    }
                }
                return
            }
        }

        if (isUnlockTask) {
            // 如果是连续解锁任务包的ID
            if (isContinueUnlock) { // 如果还存在连续多个任务，每次完成之后需要减去1
                if (taskIdList.size - 1 > 0) { // 移除掉第一个
                    taskIdList.removeAt(0)

                    // 换水任务
                    if (taskIdList[0].jumpType == CalendarData.KEY_JUMP_TYPE_TO_WATER) { // 换水加载图文数据
                        mViewModel.advertising()
                        return
                    }

                    val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                    intent.putExtra(KEY_TASK_ID_LIST, taskIdList as? Serializable)
                    intent.putExtra(KEY_INTENT_UNLOCK_TASK, true)
                    intent.putExtra(KEY_FIXED_TASK_ID, fixedId)
                    intent.putExtra(Constants.Global.KEY_TXT_ID, taskIdList[0].textId)
                    intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON, true)
                    intent.putExtra(KEY_TASK_PACKAGE_ID, true)
                    intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Next")
                    intent.putExtra(KEY_PACK_NO, packetNo)
                    startActivity(intent)
                } else {
                    mViewModel.finishTask(FinishTaskReq(taskId = fixedId, packetNo = packetNo))
                }
                return
            }

            fixedId?.let {
                when (it) { // 如果是预览界面、那么直接开始种植、然后关闭界面
                    Constants.Fixed.KEY_FIXED_ID_SEED_GERMINATION_PREVIEW -> {
                        mViewModel.startRunning(botanyId = "", goon = false)
                    }

                    // 种子换水
                    Constants.Fixed.KEY_FIXED_ID_WATER_CHANGE_GERMINATION -> {
                        acFinish()
                    }

                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_1 -> {
                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                        intent.putExtra(KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_2)
                        intent.putExtra(KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_2)
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(KEY_CATEGORYCODE, categoryCode)
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Next")
                        startActivity(intent)
                    }

                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_2 -> {
                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                        intent.putExtra(KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_3)
                        intent.putExtra(KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_3)
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(KEY_CATEGORYCODE, categoryCode)
                        intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Unlock")
                        startActivity(intent)
                    }

                    Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_3 -> {
                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                        intent.putExtra(KEY_UNLOCK_TASK_ID, unLockId)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, if (categoryCode == "100002" || categoryCode == "100004") Constants.Fixed.KEY_FIXED_ID_AUTOFLOWERING_STAGE_PREVIEW else Constants.Fixed.KEY_FIXED_ID_VEGETATIVE_STAGE_PREVIEW)
                        intent.putExtra(KEY_FIXED_TASK_ID, if (categoryCode == "100002" || categoryCode == "100004") Constants.Fixed.KEY_FIXED_ID_AUTOFLOWERING_STAGE_PREVIEW else Constants.Fixed.KEY_FIXED_ID_VEGETATIVE_STAGE_PREVIEW)
                        intent.putExtra(KEY_IS_SHOW_BUTTON, true)
                        intent.putExtra(KEY_INTENT_JUMP_PAGE, true)
                        intent.putExtra(KEY_TITLE_COLOR, "#006241")
                        intent.putExtra(KEY_IS_SHOW_BUTTON_TEXT, if (categoryCode == "100002" || categoryCode == "100004") "Unlock Autoflowering" else "Unlock Veg")
                        startActivity(intent)
                    }

                    else -> {
                        mViewModel.finishTask(FinishTaskReq(taskId = it))
                    }
                }
                return
            }
        }

        if (!isJumpPage && !isUnlockTask) { // 如果都不是、那么直接关闭界面
            acFinish()
        }
    }

    private fun isHaveCheckBoxViewType(): Boolean {/*logI("123123:::: ${adapter.data.filter { data -> data.value?.isCheck == false }.size}")*/
        val size = adapter.data.filter { data -> data.value?.isCheck == false && data.type == "option" }.size
        size.let { checkCount ->
            if (checkCount != 0) {
                ToastUtil.shortShow("Please select all item")
            }
            return checkCount == 0
        }
    }

    private val callback by lazy {
        object : SmartDragLayout.OnCloseListener {
            override fun onClose() {
                directShutdown()
            }

            override fun onDrag(y: Int, percent: Float, isScrollUp: Boolean) { // binding.smart.alpha = percent
            }

            override fun onOpen() {
            }
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
        mViewModel.apply {
            advertising.observe(this@BasePopActivity, resourceObserver {
                success { // 跳转到换水页面
                    android.os.Handler().postDelayed({ // 传递的数据为空
                        val intent = Intent(this@BasePopActivity, BasePumpActivity::class.java)
                        intent.putExtra(KEY_TASK_ID_LIST, taskIdList as? Serializable)
                        intent.putExtra(KEY_FIXED_TASK_ID, fixedId)
                        intent.putExtra(KEY_PACK_NO, packetNo)
                        intent.putExtra(BasePumpActivity.KEY_DATA, data as? Serializable)
                        refreshActivityLauncher.launch(intent)
                    }, 50)
                }
            })

            // 插入篮子植物接口
            intoPlantBasket.observe(this@BasePopActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    acFinish()
                }
            })
            richText.observe(this@BasePopActivity, resourceObserver {
                error { errorMsg, _ ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // 初始化头部Video
                    data.topPage?.firstOrNull { it.type == "video" }?.apply { // 显示头部视频
                        binding.videoItemPlayer.visibility = View.VISIBLE
                        value?.url?.let { initVideo(it, value.autoplay == true) }
                    }

                    // 标题
                    data.bar?.let {
                        binding.tvTitle.text = it
                        binding.tvTitle.setTextColor(Color.parseColor(titleColor ?: "#000000"))
                    }

                    // 动态添加按钮
                    // 不是video的都需要添加
                    val list = data.topPage?.filter { it.type != "video" }
                    list?.forEachIndexed { _, topPage ->
                        val tv = TextView(this@BasePopActivity)
                        tv.setBackgroundResource(R.drawable.create_state_button)
                        tv.isEnabled = true
                        tv.text = topPage.value?.txt
                        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(60))
                        lp.setMargins(dp2px(20), dp2px(5), dp2px(20), dp2px(5))
                        tv.layoutParams = lp
                        tv.gravity = Gravity.CENTER
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp2px(18f).toFloat())
                        tv.setTextColor(Color.WHITE)
                        binding.flRoot.addView(tv)
                    }
                    binding.flRoot.children.forEach {
                        val tv = (it as? TextView)
                        tv?.setOnClickListener {
                            list?.firstOrNull { data -> data.value?.txt == tv.text.toString() }?.apply {
                                when (type) {
                                    "pageClose" -> this@BasePopActivity.acFinish()
                                    "pageDown" -> {
                                        if (!isHaveCheckBoxViewType()) return@setOnClickListener

                                        // 跳转下一页
                                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                                        intent.putExtra(Constants.Global.KEY_TXT_ID, value?.txtId)
                                        startActivity(intent)
                                    }

                                    "finishTask" -> {
                                        if (!isHaveCheckBoxViewType()) return@setOnClickListener

                                        // 完成任务
                                        mViewModel.finishTask(FinishTaskReq(taskId = taskId))
                                    }
                                }
                            }
                        }

                    }

                    // 适配器设置数据
                    adapter.setList(data.page)
                }
            })

            // 完成任务
            finishTask.observe(this@BasePopActivity, resourceObserver {
                error { errorMsg, _ ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    hideProgressLoading() // finishTask 需要直接关闭页面
                    mViewModel.richText.value?.data?.topPage?.firstOrNull { it.type == "finishTask" }?.apply {
                        acFinish()
                    }
                }
            })

            // 植物检查
            checkPlant.observe(this@BasePopActivity, resourceObserver {
                error { errorMsg, _ ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    hideProgressLoading()
                    data?.let { PlantCheckHelp().plantStatusCheck(this@BasePopActivity, it, true) }
                }
            })
        }
    }

    override fun initData() {
        mViewModel.getRichText(txtId = txtId, type = txtType)

        binding.rvList.layoutManager = linearLayoutManager
        binding.rvList.adapter = adapter

        scrollListener()
        adapterClickEvent()
    }

    private fun scrollListener() {
        binding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition() //大于0说明有播放
                if (GSYVideoManager.instance().playPosition >= 0) { //当前播放的位置
                    val position = GSYVideoManager.instance().playPosition //对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag == "$position" && (position < firstVisibleItem || position > lastVisibleItem)) { //如果滑出去了上面和下面就是否，和今日头条一样
                        //是否全屏
                        if (!GSYVideoManager.isFullState(this@BasePopActivity)) {
                            adapter.data[position].videoPosition = GSYVideoManager.instance().currentPosition // 不释放全部
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

    private fun adapterClickEvent() {
        adapter.apply {
            addChildClickViewIds(R.id.iv_pic, R.id.tv_html, R.id.tv_learn, R.id.cl_go_url, R.id.cl_support, R.id.cl_discord, R.id.cl_learn, R.id.cl_check, R.id.tv_page_txt, R.id.tv_txt)
            setOnItemChildClickListener { _, view, position ->
                val bean = data[position]
                when (view.id) {
                    R.id.iv_pic -> { // 弹出图片
                        XPopup.Builder(context).asImageViewer(
                                (view as? ImageView), bean.value?.url, SmartGlideImageLoader()
                            ).show()
                    }

                    // 跳转HTML
                    R.id.cl_go_url, R.id.tv_html -> {
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, bean.value?.title)
                        context.startActivity(intent)
                    }

                    // 阅读更多
                    R.id.cl_learn, R.id.tv_learn -> { // todo 请求id
                        bean.value?.txtId?.let { // 继续请求弹窗
                            val intent = Intent(context, BasePopActivity::class.java)
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
                            intent.putExtra(WebActivity.KEY_WEB_URL, "https://discord.gg/FCj6UGCNtU")
                        } else {
                            intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        }
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "hey abby")
                        context.startActivity(intent)
                    } // 勾选框
                    R.id.cl_check -> {
                        view.findViewById<CheckBox>(R.id.curing_box)?.apply {
                            logI("before: ${data[position].value?.isCheck}")
                            data[position].value?.isCheck = !isChecked
                            isChecked = !isChecked
                            logI("after: ${data[position].value?.isCheck}")
                        }
                    } // 跳转到HTML
                    R.id.tv_page_txt -> {
                        if (bean.value?.url.isNullOrEmpty()) return@setOnItemChildClickListener // 跳转到HTML
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        context.startActivity(intent)
                    }

                    R.id.tv_txt -> {
                        if (bean.value?.url.isNullOrEmpty()) return@setOnItemChildClickListener // 跳转到HTML
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun BasePopActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@BasePopActivity
            viewModel = mViewModel
            executePendingBindings()
        }
    }

    // 富文本适配器
    private val adapter by lazy {
        HomeKnowMoreAdapter(mutableListOf())
    }
    private val linearLayoutManager by lazy {
        LinearLayoutManager(this@BasePopActivity)
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

    @Inject
    lateinit var mViewModel: BaseViewModel


    // 系统返回键
    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        directShutdown()
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

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }


    /**
     *
     * action： 从富文本界面跳准到排水界面，然后排水界面完成任务，并且返回
     * 跳转到其他地方，返回的时候刷新任务
     */
    private val refreshActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) { // 刷新任务
            // 跳转到日历界面
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).navigation(this@BasePopActivity)
        }
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

        // 调用哪个解锁Veg\auto的ID
        const val KEY_CATEGORYCODE = "key_categorycode"

        // 设备ID
        const val KEY_DEVICE_ID = "key_device_id"

        // 配件ID
        const val KEY_PART_ID = "key_part_id"

        // 自动化Id
        const val KEY_AUTOMATION_ID = "key_auto_id"

        // 传递一个数组，这个数组里面有一系列的taskId
        const val KEY_TASK_ID_LIST = "key_task_id_list"

        // 连续解锁任务包ID
        const val KEY_TASK_PACKAGE_ID = "key_task_package_id"

        // packNo的id
        const val KEY_PACK_NO = "key_pack_no"
    }
}