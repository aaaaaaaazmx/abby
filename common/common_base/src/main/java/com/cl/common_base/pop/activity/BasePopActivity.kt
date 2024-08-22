package com.cl.common_base.pop.activity

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.mtjsoft.barcodescanning.extentions.dp2px
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.adapter.HomeKnowMoreAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.bean.ChoosePicBean
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.bean.ImageUrl
import com.cl.common_base.bean.JumpTypeBean
import com.cl.common_base.bean.RichTextData
import com.cl.common_base.bean.SnoozeReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.databinding.BasePopActivityBinding
import com.cl.common_base.video.videoUiHelp
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.sp2px
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.CustomLoadingPopupView
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.toast.ToastUtil
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.XPopup.getAnimationDuration
import com.lxj.xpopup.impl.LoadingPopupView
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.lxj.xpopup.widget.LoadingView
import com.lxj.xpopup.widget.SmartDragLayout
import com.shuyu.gsyvideoplayer.GSYVideoManager
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.IntercomContent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
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
     * 用于推迟任务包的编号
     */
    private val snoozeNo by lazy { intent.getStringExtra(KEY_TASK_NO) }

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

    /**
     * 是否是预览
     */
    private val isPreview by lazy { intent.getBooleanExtra(KEY_PREVIEW, false) }


    /**
     * 传入过来的用于FinishTask的ViewDatas
     */
    private val viewDatas by lazy {
        val inputData = intent.getSerializableExtra(KEY_INPUT_BOX) as? MutableList<FinishTaskReq.ViewData>
        inputData ?: mutableListOf()
    }
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
        ViewUtils.setVisible(isShowButton && !isPreview, binding.btnNext)
        ViewUtils.setVisible(isShowUnlockButton && !isPreview, binding.slideToConfirm)
        binding.btnNext.text = showButtonText ?: "Next"
        binding.btnNext.setOnClickListener {
            fixedProcessingLogic()
        }
        /*binding.slideToConfirm.setEngageText(unLockButtonEngage ?: "Slide to Unlock")*/
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
                    Constants.Fixed.KEY_FIXED_ID_GO_TO_CAMERA -> {
                        // 去拍照。
                        PermissionHelp().applyPermissionHelp(
                            this@BasePopActivity,
                            getString(com.cl.common_base.R.string.profile_request_camera),
                            object : PermissionHelp.OnCheckResultListener {
                                override fun onResult(result: Boolean) {
                                    if (!result) return
                                    //跳转到调用系统相机
                                    gotoCamera()
                                }
                            },
                            Manifest.permission.CAMERA
                        )
                    }
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

                    // 土培种植前检查
                    Constants.Fixed.KEY_FIXED_ID_SOIL_TRANSPLANT_CLONE_CHECK -> {
                        mViewModel.startRunning(botanyId = "", goon = false)
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
                            mViewModel.checkPlant()
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
                // 判断当前任务是否包含input_box类型
                val dataArray = adapter.data
                    .filter { it.itemType == RichTextData.KEY_TYPE_INPUT_BOX }
                    .mapNotNull { adapter.getViewByPosition(adapter.data.indexOf(it), R.id.input_weight) as? EditText }
                    .map { it.text.toString() }
                    .toMutableList()

                // 一个任务一个FinishTaskReq.ViewData， 多个任务多个FinishTaskReq.ViewData，但是都保存在viewDatas里面
                if (dataArray.isNotEmpty()) {
                    viewDatas.add(FinishTaskReq.ViewData(textId = taskIdList[0].textId, dataArray = dataArray.toMutableList()))
                }

                if (taskIdList.size - 1 > 0) { // 移除掉第一个
                    taskIdList.removeAt(0)

                    // 换水任务
                    if (taskIdList[0].jumpType == CalendarData.KEY_JUMP_TYPE_TO_WATER) { // 换水加载图文数据
                        val intent = Intent(this@BasePopActivity, BasePumpActivity::class.java)
                        intent.putExtra(KEY_TASK_ID, taskId)
                        intent.putExtra(KEY_TASK_ID_LIST, taskIdList as? Serializable)
                        intent.putExtra(KEY_FIXED_TASK_ID, fixedId)
                        intent.putExtra(KEY_PACK_NO, packetNo)
                        intent.putExtra(KEY_INPUT_BOX, viewDatas as? Serializable)
                        refreshActivityLauncher.launch(intent)
                        return
                    }

                    // 继续是富文本任务
                    val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                    intent.putExtra(KEY_TASK_ID, taskId)
                    intent.putExtra(KEY_TASK_NO, taskIdList[0].taskNo)
                    intent.putExtra(KEY_TASK_ID_LIST, taskIdList as? Serializable)
                    intent.putExtra(KEY_INTENT_UNLOCK_TASK, true)
                    intent.putExtra(KEY_FIXED_TASK_ID, fixedId)
                    intent.putExtra(Constants.Global.KEY_TXT_ID, taskIdList[0].textId)
                    intent.putExtra(KEY_INPUT_BOX, viewDatas as? Serializable)
                    intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON, true)
                    intent.putExtra(KEY_TASK_PACKAGE_ID, true)
                    intent.putExtra(KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Unlock")
                    intent.putExtra(KEY_PACK_NO, packetNo)
                    startActivity(intent)
                } else {
                    mViewModel.finishTask(FinishTaskReq(taskId = fixedId, packetNo = packetNo, viewDatas = if (viewDatas.isEmpty()) null else viewDatas))
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

    // 去拍照
    private var imageUri: Uri? = null
    private fun gotoCamera() {
        imageUri = createImageUri()
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
             val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
             intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
             startActivityForResult(intent, REQUEST_CAPTURE)
        }
    }

    /**
     * 根据uri获取文件路径
     */
    private fun getRealFilePathFromUri(context: Context, uri: Uri?): String? {
        if (null == uri) {
            return null
        }
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) {
            data = uri.path
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }

    /**
     * 结果返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // camera
            REQUEST_CAPTURE -> {
                if (resultCode == RESULT_OK && imageUri != null) {
                    // gotoClipActivity(imageUri)
                    val cropImagePath = getRealFilePathFromUri(applicationContext, imageUri)
                    // 直接上传
                    cropImagePath?.let {
                        Luban.with(this@BasePopActivity).load(it).ignoreBy(100)
                            .setCompressListener(object : OnNewCompressListener {
                                override fun onSuccess(source: String?, compressFile: File?) {
                                    logI("@#!@#: ${source.toString()} mmm ${compressFile?.length()}")
                                    compressFile?.let {
                                        val upLoadImage = upLoadImage(compressFile)
                                        mViewModel.uploadImg(upLoadImage)
                                    }
                                }

                                override fun onError(source: String?, e: Throwable?) {
                                   logI(e.toString())
                                }

                                override fun onStart() {

                                }
                            }).launch();

                    }
                }
            }
        }
    }

    /**
     * 表单提交
     * 需要循环上传
     */
    private fun upLoadImage(file: File): List<MultipartBody.Part> {
        //1.创建MultipartBody.Builder对象
        val builder = MultipartBody.Builder()
            //表单类型
            .setType(MultipartBody.FORM)

        //2.获取图片，创建请求体
        //表单类x型
        //表单类型
        val body: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)

        //3.调用MultipartBody.Builder的addFormDataPart()方法添加表单数据
        /**
         * ps:builder.addFormDataPart("code","123456");
         * ps:builder.addFormDataPart("file",file.getName(),body);
         */
        builder.addFormDataPart("imgType", "aiCheck") //传入服务器需要的key，和相应value值
        builder.addFormDataPart("files", file.name, body) //添加图片数据，body创建的请求体
        //4.创建List<MultipartBody.Part> 集合，
        //  调用MultipartBody.Builder的build()方法会返回一个新创建的MultipartBody
        //  再调用MultipartBody的parts()方法返回MultipartBody.Part集合
        return builder.build().parts
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private fun createImageUri(): Uri? {
        //Android 10以上
        val photoUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val status = Environment.getExternalStorageState()
            // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            if (status == Environment.MEDIA_MOUNTED) {
                contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                )
            } else {
                contentResolver.insert(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                    ContentValues()
                )
            }
        } else {
            val tempFile: File = FileUtil.createFileIfNotExists(
                SDCard.getContextPictureDir(this@BasePopActivity)
                    .toString() + File.separator + System.currentTimeMillis() + ".jpg"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider", tempFile
                )
            } else {
                Uri.fromFile(tempFile)
            }
        }
        return photoUri
    }

    private fun isHaveCheckBoxViewType(): Boolean {/*logI("123123:::: ${adapter.data.filter { data -> data.value?.isCheck == false }.size}")*/
        val size = adapter.data.filter { data -> data.value?.select == false && data.type == "option" }.size
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

    private val loadingPopup by lazy {
        CustomLoadingPopupView(this@BasePopActivity, 0).setTitle("AI check in progress, please do not exit this page.")
    }

    override fun observe() {
        mViewModel.apply {
            // 上传图片回调
            uploadImg.observe(this@BasePopActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    data?.forEach {
                        val oneArray = it.split("com/")
                        if (oneArray.isNotEmpty()) {
                            if (oneArray.isNotEmpty()) {
                                val result = oneArray[1].split("?")
                                if (result.isNotEmpty()) {
                                    logI(result[0])
                                    // 更新用户信息
                                    // 更新集合
                                    aiCheck(plantId = userInfo?.plantId.toString(), url = result[0])
                                }
                            }
                        }
                    }
                }
                loading { showProgressLoading() }
            })

            aiCheck.observe(this@BasePopActivity, resourceObserver {
                loading {
                    xpopup(this@BasePopActivity) {
                        asCustom(loadingPopup).show()
                    }
                }

                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    loadingPopup.dismiss()
                }

                success {
                    loadingPopup.dismiss()
                    //YES：识别成功并通过。返回一个富文本ID，打开后，并自动勾选，点击解锁，调用完成任务接口
                    //NO: 弹提示窗
                    //NG: 弹提示窗
                    //USED_UP: 调用次数已经用完（非会员）跳转购买订阅地址
                    //USED_UP_SUBSCRIPTION: 调用次数已用完（会员）
                    //UNKNOWN:其它情况，弹出提示
                    val code = data?.resultCode ?: ""
                    when(code) {
                        "YES" -> {
                            val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                            intent.putExtra(Constants.Global.KEY_TXT_ID, data?.textId)
                            intent.putExtra(KEY_IS_SHOW_BUTTON, true)
                            intent.putExtra(KEY_IS_SHOW_BUTTON_TEXT, "Unlock")
                            intent.putExtra(KEY_INTENT_UNLOCK_TASK, true)
                            intent.putExtra(KEY_TASK_ID, taskId)
                            intent.putExtra(KEY_FIXED_TASK_ID, taskId)
                            refreshActivityLauncher.launch(intent)
                        }

                        "NG", "NO", "UNKNOWN" -> {
                            xpopup(this@BasePopActivity) {
                                isDestroyOnDismiss(false)
                                dismissOnTouchOutside(false)
                                asCustom(BaseCenterPop(this@BasePopActivity, isShowCancelButton = false, content = data?.content, confirmText = "Exit")).show()
                            }
                        }

                        "USED_UP" -> { // 非会员
                            xpopup(this@BasePopActivity) {
                                isDestroyOnDismiss(false)
                                dismissOnTouchOutside(false)
                                asCustom(BaseCenterPop(this@BasePopActivity, cancelText = "No,thanks", content = data?.content, confirmText = "Subscribe Now", onConfirmAction =  {
                                    // 跳转到购买地址
                                    //  跳转订阅网站
                                    val intent = Intent(this@BasePopActivity, WebActivity::class.java)
                                    intent.putExtra(WebActivity.KEY_WEB_URL, data?.subscribeNow)
                                    startActivity(intent)
                                })).show()
                            }
                        }

                        "USED_UP_SUBSCRIPTION" -> {  // 会员
                            xpopup(this@BasePopActivity) {
                                isDestroyOnDismiss(false)
                                dismissOnTouchOutside(false)
                                asCustom(BaseCenterPop(this@BasePopActivity, isShowCancelButton = false, content = data?.content, confirmText = "OK", onConfirmAction =  {
                                    // 跳转到购买地址
                                    //  跳转订阅网站
                                    val intent = Intent(this@BasePopActivity, WebActivity::class.java)
                                    intent.putExtra(WebActivity.KEY_WEB_URL, data?.subscribeNow)
                                    startActivity(intent)
                                })).show()
                            }
                        }

                    }
                }
            })

            // 生成会话
            conversationId.observe(this@BasePopActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    data?.conversation_id?.let { IntercomContent.Conversation(id = it) }?.let { Intercom.client().presentContent(it) }
                }
            })

            // 延迟任务
            delayTask.observe(this@BasePopActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (isContinueUnlock) {
                        // 跳转到日历界面
                        ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).navigation(this@BasePopActivity)
                        return@success
                    }
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

                    // 滑动结果按钮文案
                    mViewModel.getSliderText(data.topPage?.firstOrNull { it.type == "finishTask" }?.let {
                        binding.slideToConfirm.setEngageText(it.value?.txt ?: "Slide to Unlock")
                        it.value?.txt
                    })

                    // 标题
                    data.bar?.let {
                        binding.tvTitle.text = it
                        binding.tvTitle.setTextColor(Color.parseColor(titleColor ?: "#000000"))
                    }

                    // 动态添加按钮
                    // 不是video的都需要添加
                    val list = data.topPage?.filter { it.type != "video" }
                    list?.forEachIndexed { _, topPage ->
                        if (!isPreview) {
                            // 参考KnowMoreActivity
                          /*  val tv = TextView(this@BasePopActivity)
                            tv.setBackgroundResource(R.drawable.create_state_button)
                            tv.isEnabled = true
                            tv.text = topPage.value?.txt
                            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(60))
                            lp.setMargins(dp2px(20), dp2px(10), dp2px(20), dp2px(0))
                            tv.layoutParams = lp
                            tv.gravity = Gravity.CENTER
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp2px(18f).toFloat())
                            tv.setTextColor(Color.WHITE)
                            binding.flRoot.addView(tv)*/
                        }
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
                   data.page?.map { it.copy(isPreview = isPreview) }?.let { adapter.setList(it) }
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

                    // 如果是连续解锁任务包到最后一个任务，完成之后就直接跳转到日历界面
                    if (isContinueUnlock) {
                        // 判断当前是否还有最后一个任务，并且是弹窗任务
                        if (taskIdList.size == 1) {
                            if (taskIdList[0].jumpType == CalendarData.KEY_JUMP_TYPE_POP_UP) {
                                jumpToPop(taskIdList)
                                return@success
                            }
                        }
                        // 跳转到日历界面
                        ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).navigation(this@BasePopActivity)
                        return@success
                    }

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
        mViewModel.getRichText(taskId = taskId, txtId = txtId, type = txtType)

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
                            if (adapter.data.size > position) {
                                adapter.data[position].videoPosition = GSYVideoManager.instance().currentPosition // 不释放全部
                            }
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
            addChildClickViewIds(R.id.iv_pic, R.id.tv_html, R.id.tv_learn, R.id.cl_go_url, R.id.cl_support, R.id.cl_discord, R.id.cl_learn, R.id.cl_check, R.id.tv_page_txt, R.id.tv_txt,
                R.id.input_delete, R.id.tv_delay_task, R.id.rl_ono_on_one, R.id.rl_ai_check)
            setOnItemChildClickListener { _, view, position ->
                val bean = data[position]
                when (view.id) {
                    R.id.rl_ai_check -> {
                        // aiCheck
                        // 跳转富文本界面,获取下面的txtId文本，继续跳转
                        bean.value?.txtId?.let { // 继续请求弹窗
                            val intent = Intent(context, BasePopActivity::class.java)
                            intent.putExtra(Constants.Global.KEY_TXT_ID, it)
                            intent.putExtra(KEY_TASK_ID, taskId)
                            intent.putExtra(KEY_IS_SHOW_BUTTON, true)
                            intent.putExtra(KEY_INTENT_JUMP_PAGE, true)
                            intent.putExtra(KEY_IS_SHOW_BUTTON_TEXT, "Next")
                            intent.putExtra(KEY_INTENT_JUMP_PAGE, true)
                            intent.putExtra(KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_GO_TO_CAMERA)
                            context.startActivity(intent)
                        }
                    }

                    R.id.rl_ono_on_one -> {
                        // 判断当前是否是vip
                        if (mViewModel.userInfo?.isVip == 1) {
                            // 发起会话
                            mViewModel.conversations(taskNo = snoozeNo, textId = mViewModel.richText.value?.data?.txtId)
                        } else {
                            // 跳转到购买链接
                            val intent = Intent(context, WebActivity::class.java)
                            intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/app-subscription-plan")
                            context.startActivity(intent)
                        }
                    }

                    R.id.input_delete -> {
                        // 删除当前的输入框
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.input_weight) as? EditText
                        etWeight?.setText("")
                    }

                    R.id.iv_pic -> { // 弹出图片
                        XPopup.Builder(context).asImageViewer(
                            (view as? ImageView), bean.value?.url, SmartGlideImageLoader()
                        ).isShowSaveButton(false).show()
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
                    }
                    // 勾选框
                    R.id.cl_check -> {
                        view.findViewById<CheckBox>(R.id.curing_box)?.apply {
                            logI("before: ${data[position].value?.select}")
                            data[position].value?.select = !isChecked
                            isChecked = !isChecked
                            logI("after: ${data[position].value?.select}")
                        }
                    }
                    // 跳转到HTML
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

                    R.id.tv_delay_task -> {
                        // 延迟任务
                        mViewModel.delayTask(SnoozeReq(taskId = taskId, taskNo = snoozeNo))
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
        if (isContinueUnlock) {
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).navigation(this@BasePopActivity)
            finish()
            return
        }
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

    private fun jumpToPop(taskData: MutableList<CalendarData.TaskList.SubTaskList>?) {
        if (taskData.isNullOrEmpty()) return
        // 跳转弹窗
        taskData.firstOrNull { it.jumpType == CalendarData.KEY_JUMP_TYPE_POP_UP }?.let { data ->
            // 解析jumpJson这个json
            val parseObject = GSON.parseObject(data.jumpJson, JumpTypeBean::class.java)
            // 如果是会员
            val isVip = parseObject?.subscribe == true
            xpopup(this@BasePopActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(false)
                asCustom(BaseCenterPop(this@BasePopActivity, content = if (isVip) parseObject?.onOnOne else parseObject?.pleaseSubscribe,
                    cancelText = "No,thanks", confirmText = if (isVip) "Yes" else "Subscribe Now", onConfirmAction = {
                        if (isVip) {
                            // 跳转到日历界面，然后在发起会话。
                            ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).withString(CalendarData.KEY_TASK_NO, data.taskNo).navigation(this@BasePopActivity)
                        } else {
                            //  跳转订阅网站
                            val intent = Intent(this@BasePopActivity, WebActivity::class.java)
                            intent.putExtra(WebActivity.KEY_WEB_URL, parseObject?.subscribeNow)
                            startActivity(intent)
                        }
                    }, onCancelAction = {
                        // 完成任务
                        // 跳转到日历界面
                        ARouter.getInstance().build(RouterPath.My.PAGE_MY_CALENDAR).navigation(this@BasePopActivity)
                    }
                )).show()
            }
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

        // 摄像头id，用于解绑
        const val KEY_CAMERA_ID = "key_camera_id"

        // 配件ID
        const val KEY_PART_ID = "key_part_id"

        // usbPort
        const val KEY_USB_PORT = "key_usb_port"

        // 自动化Id
        const val KEY_AUTOMATION_ID = "key_auto_id"

        // 传递一个数组，这个数组里面有一系列的taskId
        const val KEY_TASK_ID_LIST = "key_task_id_list"

        // 所有任务类型获取富文本展示接口的时候，都需要taskId
        const val KEY_TASK_ID = "key_task_id"

        // 连续解锁任务包ID
        const val KEY_TASK_PACKAGE_ID = "key_task_package_id"

        // packNo的id
        const val KEY_PACK_NO = "key_pack_no"

        // taskNo 子任务编号，用于推迟任务
        const val KEY_TASK_NO = "key_task_no"

        // 当富文本里包含input_box时，才需要传入这个值
        const val KEY_INPUT_BOX = "key_input_box"

        // 只是起到预览, 不限时not ready 也不显示按钮
        const val KEY_PREVIEW = "key_preview"

        // 植物Id
        const val KEY_PLANT_ID = "key_plant_id"

        // 共享设备类型
        const val KEY_SHARE_TYPE = "key_share_type"

        // 共享设备ID KEY_RELATION_ID
        const val KEY_RELATION_ID = "key_relation_id"

        // 返回的时候是否需要弹窗
        const val KEY_IS_SHOW_POP = "key_is_show_pop"

        private const val REQUEST_CAPTURE = 100
    }
}