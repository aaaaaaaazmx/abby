package com.cl.common_base.pop

import android.content.Context
import android.content.Intent
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.adapter.PlantInitMultiplePopAdapter
import com.cl.common_base.bean.GuideInfoData
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.databinding.HomePlantUsuallyPopBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.SmartGlideImageLoader


/**
 * 种植引导从后台拉取的数据POP
 *
 * @author 李志军 2022-08-08 13:46
 */
class BasePlantUsuallyPop(
    context: Context,
    private val onNextAction: ((weight: String?) -> Unit)? = null,
    private var popData: MutableList<GuideInfoData.PlantInfo>? = null,
    private var data: GuideInfoData? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_usually_pop
    }

    private var binding: HomePlantUsuallyPopBinding? = null
    private val adapter by lazy {
        PlantInitMultiplePopAdapter(
            mutableListOf(),
            dryingEtWeightChange = { _, _, _, _, _, _, adapterData ->
                // 按钮是否可点击啊
                binding?.btnSuccess?.isEnabled =
                    adapterData.filter { it.isCheck == true }.size == adapterData.size
            },
            curingEtWeightChange = { _, _, _, _, _, adapterData ->
                // 按钮是否可点击
                binding?.btnSuccess?.isEnabled =
                    adapterData.filter { it.isCheck == true }.size == adapterData.size
            })
    }

    /**
     * 当前状态
     */
    private var isCurrentStatus: Int? = null

    /**
     * 设置数据
     */
    fun setData(popData: GuideInfoData?) {
        binding?.btnSuccess?.isEnabled = false
        this.popData = popData?.items
        this.data = popData
        this.isCurrentStatus = popData?.type
    }

    override fun beforeShow() {
        super.beforeShow()
        binding?.tvDec?.text = data?.title
        // 设置数据
        popData?.let {
            if (it.size == 0) return
            when ("$isCurrentStatus") {
                // 设置最后一个Bean类为当前周期状态
                UnReadConstants.Plant.KEY_CURING,
                UnReadConstants.Plant.KEY_DRYING -> {
                    it[it.size - 1].isCurrentStatus = isCurrentStatus
                }
                // 设置当前type = 种子孵化阶段
                UnReadConstants.Plant.KEY_INCUBATION -> {
                    it.forEach { bean ->
                        if (bean.url.isNullOrEmpty()) {
                            bean.isCurrentStatus = UnReadConstants.Plant.KEY_INCUBATION.toInt()
                        } else {
                            bean.isCurrentStatus = GuideInfoData.KEY_URL_TYPE
                        }
                    }
                    // 这个没有勾选框，默认为可选状态
                    binding?.btnSuccess?.isEnabled = true
                }
            }
            adapter.setList(it)
        }

        when ("$isCurrentStatus") {
            UnReadConstants.Plant.KEY_CURING -> {
                // 按钮显示不一致
                binding?.btnSuccess?.text = context.getString(R.string.base_complete)
            }
            UnReadConstants.Plant.KEY_INCUBATION -> {
                binding?.btnSuccess?.text = context.getString(R.string.base_done)
            }
            else -> {
                // 按钮显示不一致
                binding?.btnSuccess?.text = context.getString(R.string.my_next)
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        if (isCurrentStatus == UnReadConstants.Plant.KEY_DRYING.toInt()) {
            //  todo 这个position目前时固定写死的，有可能会有问题
            val etWeight = adapter.getViewByPosition(2, R.id.et_weight) as? EditText
            etWeight?.setText("")

            (adapter.getViewByPosition(2, R.id.type_two_box) as? CheckBox)?.isChecked = false
        }

        if (isCurrentStatus == UnReadConstants.Plant.KEY_CURING.toInt()) {
            // todo 这个position目前时固定写死的,有可能会有问题
            val etWeight = adapter.getViewByPosition(0, R.id.curing_et_weight) as? EditText
            etWeight?.setText("")
        }
    }

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantUsuallyPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                // 需要判断当前是否有需要称重的周期，
                when ("$isCurrentStatus") {
                    UnReadConstants.Plant.KEY_DRYING -> {
                        val etWeight = adapter.getViewByPosition(2, R.id.et_weight) as? EditText
                        logI(
                            """
                                    etWeight: ${etWeight?.text.toString()}
                                """.trimIndent()
                        )
                        onNextAction?.invoke(etWeight?.text.toString())
                    }
                    UnReadConstants.Plant.KEY_CURING -> {
                        // todo VALUE_STATUS_CURING
                        val etWeight =
                            adapter.getViewByPosition(0, R.id.curing_et_weight) as? EditText
                        logI(
                            """
                                    etWeight: ${etWeight?.text.toString()}
                                """.trimIndent()
                        )
                        onNextAction?.invoke(etWeight?.text.toString())
                    }
                    else -> {
                        onNextAction?.invoke(null)
                    }
                }
                dismiss()
            }

            tvDec.text = data?.title

            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter

            // todo 需要判断是否有checkBox的存在，，如果没有checkBox的存在，那么按钮直接  = true
            // todo 但是目前不知道根据什么来判断是否显示checkBox
            adapter.addChildClickViewIds(
                R.id.cl_two,
                R.id.box,
                R.id.iv_pic,
                R.id.type_two_box,
                R.id.type_box,
                R.id.cl_type,
                R.id.cl_type_skip,
                R.id.type_ask,
                R.id.iv_delete,
                R.id.type_ask,
                R.id.cl_curing_type,
                R.id.curing_box,
                R.id.curing_delete,
                R.id.tv_html
            )
            // 按钮点击
            adapter.setOnItemChildClickListener { adapter, view, position ->
                val data = adapter.data[position] as? GuideInfoData.PlantInfo
                when (view.id) {
                    R.id.cl_two -> {
                        val cb = if (view.id == R.id.cl_two) {
                            view.findViewById(R.id.box)
                        } else {
                            (view as? CheckBox)
                        }
                        val check = cb?.isChecked
                        (adapter.data[position] as? GuideInfoData.PlantInfo)?.apply {
                            cb?.isChecked = check == false
                            isCheck = check == false
                        }
                        // 按钮是否可点击
                        btnSuccess.isEnabled =
                            (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                    }
                    R.id.box -> {
                        (adapter.data[position] as? GuideInfoData.PlantInfo)?.apply {
                            isCheck = isCheck == false
                        }

                        (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.forEach {
                            logI("${it.isCheck}")
                        }

                        // 按钮是否可点击
                        btnSuccess.isEnabled =
                            (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                    }
                    R.id.iv_pic -> {
                        (adapter.data[position] as? GuideInfoData.PlantInfo)?.apply {
                            XPopup.Builder(context)
                                .asImageViewer(
                                    (view as? ImageView),
                                    picture,
                                    SmartGlideImageLoader()
                                )
                                .show()
                        }
                    }

                    // 这是during周期时点击事件产生的事情
                    R.id.type_ask -> {
                        XPopup.Builder(context)
                            .asCustom(HomeCuringUnlockPop(context) {

                            }).show()
                    }
                    R.id.cl_type -> {
                        // 勾选这个弹窗，需要判断当前是否输入重量了
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.et_weight) as? EditText
                        val weight = etWeight?.text

                        val cb = if (view.id == R.id.cl_type) {
                            view.findViewById<CheckBox>(R.id.type_box)
                        } else {
                            (view as? CheckBox)
                        }

                        // 如果是没有输入重量的话，是不允许勾选的
                        if (weight.isNullOrEmpty()) {
                            ToastUtil.shortShow("Invalid weight value")
                            cb?.isChecked = false
                            return@setOnItemChildClickListener
                        }


                        val check = cb?.isChecked
                        (adapter.data[position] as? GuideInfoData.PlantInfo)?.apply {
                            cb?.isChecked = check == false
                            isCheck = check == false
                        }
                        btnSuccess.isEnabled =
                            (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                    }
                    R.id.type_box -> {
                        // 勾选这个弹窗，需要判断当前是否输入重量了
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.et_weight) as? EditText
                        val weight = etWeight?.text
                        // 如果是没有输入重量的话，是不允许勾选的
                        if (weight.isNullOrEmpty()) {
                            ToastUtil.shortShow("Invalid weight value")
                            ((view as? CheckBox))?.isChecked = false
                            return@setOnItemChildClickListener
                        }

                        (adapter.data[position] as? GuideInfoData.PlantInfo)?.apply {
                            isCheck = isCheck == false
                        }
                        (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.forEach {
                            logI("${it.isCheck}")
                        }
                        // 按钮是否可点击啊
                        btnSuccess.isEnabled =
                            (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                    }
                    R.id.type_two_box -> {
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.et_weight) as? EditText
                        logI(
                            """
                            text : "${etWeight?.text}"
                        """.trimIndent()
                        )
                        val isCheck = ((view as? CheckBox))?.isChecked
                        // 没有输入内容，并且勾选跳过了。
                        if (etWeight?.text.isNullOrEmpty()) {
                            if (isCheck == true) {
                                // 判断按钮是否可点击
                                // 按钮是否可点击啊
                                // 数量需要减去1，最后一个是输入数量的
                                btnSuccess.isEnabled =
                                    ((adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size
                                        ?: 0) == if (adapter.data.size == 0) 1 else adapter.data.size - 1
                            } else {
                                btnSuccess.isEnabled =
                                    (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                            }
                        } else {
                            // 如果已经输入了重量
                            // 并且也是勾选了跳过
                            if (isCheck == true) {
                                etWeight?.setText("")
                                // 数量需要减去1，最后一个是输入数量的
                                btnSuccess.isEnabled =
                                    ((adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size
                                        ?: 0) == if (adapter.data.size == 0) 1 else adapter.data.size - 1
                            } else {
                                btnSuccess.isEnabled =
                                    (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                            }
                        }
                    }
                    R.id.cl_type_skip -> {
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.et_weight) as? EditText
                        logI(
                            """
                            text : "${etWeight?.text}"
                        """.trimIndent()
                        )
                        val cbBox = if (view.id == R.id.cl_type_skip) {
                            view.findViewById(R.id.type_two_box)
                        } else {
                            (view as? CheckBox)
                        }
                        // 取反
                        cbBox?.isChecked = cbBox?.isChecked == false
                        // 没有输入内容，并且勾选跳过了。
                        if (etWeight?.text.isNullOrEmpty()) {
                            if (cbBox?.isChecked == true) {
                                // 判断按钮是否可点击
                                // 按钮是否可点击啊
                                // 数量需要减去1，最后一个是输入数量的
                                btnSuccess.isEnabled =
                                    ((adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size
                                        ?: 0) == if (adapter.data.size == 0) 1 else adapter.data.size - 1
                            } else {
                                btnSuccess.isEnabled =
                                    (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                            }
                        } else {
                            // 如果已经输入了重量
                            // 并且也是勾选了跳过
                            if (cbBox?.isChecked == true) {
                                etWeight?.setText("")
                                // 数量需要减去1，最后一个是输入数量的
                                btnSuccess.isEnabled =
                                    ((adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size
                                        ?: 0) == if (adapter.data.size == 0) 1 else adapter.data.size - 1
                            } else {
                                btnSuccess.isEnabled =
                                    (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                            }
                        }
                    }
                    // 删除输入的东西
                    R.id.iv_delete -> {
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.et_weight) as? EditText
                        etWeight?.setText("")
                    }
                    R.id.type_ask -> {
                        // 点击问号时，需要弹窗

                    }

                    // 这是curing周期时点击事件发生的事情
                    R.id.cl_curing_type -> {
                        logI(
                            """
                            position : $position
                        """.trimIndent()
                        )
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.curing_et_weight) as? EditText
                        val weight = etWeight?.text.toString()

                        val cbBox = if (view.id == R.id.cl_curing_type) {
                            view.findViewById(R.id.curing_box)
                        } else {
                            (view as? CheckBox)
                        }

                        // 如果是没有输入重量的话，是不允许勾选的
                        if (weight.isEmpty()) {
                            ToastUtil.shortShow("Invalid weight value")
                            cbBox?.isChecked = false
                            return@setOnItemChildClickListener
                        }


                        val check = cbBox?.isChecked
                        (adapter.data[position] as? GuideInfoData.PlantInfo)?.apply {
                            cbBox?.isChecked = check == false
                            isCheck = check == false
                        }

                        btnSuccess.isEnabled =
                            (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                    }
                    R.id.curing_box -> {
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.curing_et_weight) as? EditText
                        val weight = etWeight?.text.toString()

                        val cbBox = ((view as? CheckBox))
                        // 如果是没有输入重量的话，是不允许勾选的
                        if (weight.isEmpty()) {
                            ToastUtil.shortShow("Invalid weight value")
                            cbBox?.isChecked = false
                            return@setOnItemChildClickListener
                        }

                        (adapter.data[position] as? GuideInfoData.PlantInfo)?.apply {
                            isCheck = isCheck == false
                        }

                        // 按钮是否可点击
                        btnSuccess.isEnabled =
                            (adapter.data as? MutableList<GuideInfoData.PlantInfo>)?.filter { it.isCheck == true }?.size == adapter.data.size
                    }
                    R.id.curing_delete -> {
                        val etWeight =
                            adapter.getViewByPosition(position, R.id.curing_et_weight) as? EditText
                        etWeight?.setText("")
                    }

                    // 超链接
                    R.id.tv_html -> {
                        // 主要是判断当前后缀是否是视频
                        if (data?.url?.let { Constants.videoList.contains(it) } == true) {
                            // 视频播放
                            val showImgList: ArrayList<LocalMedia> = ArrayList()
                            val media = LocalMedia()
                            media.path = data.url    // this就是网络视频地址 ， https 开头的，阿里云链接视频可直接播放
                            showImgList.add(media)
                            PictureSelector.create(context)
                                .openPreview()
                                .setImageEngine(GlideEngine.createGlideEngine())
                                .isAutoVideoPlay(true)
                                .isLoopAutoVideoPlay(true)
                                .isVideoPauseResumePlay(true)
                                .startActivityPreview(0, false, showImgList)
                            return@setOnItemChildClickListener
                        }
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, data?.url)
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, data?.title)
                        context?.startActivity(intent)
                    }
                }
            }
        }
    }
}
