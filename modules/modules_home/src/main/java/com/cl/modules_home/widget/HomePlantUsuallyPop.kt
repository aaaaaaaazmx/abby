package com.cl.modules_home.widget

import android.content.Context
import android.widget.CheckBox
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.ext.logI
import com.cl.modules_home.adapter.PlantInitPopAdapter
import com.cl.modules_home.response.GuideInfoData
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePlantUsuallyPopBinding
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.SmartGlideImageLoader

/**
 * 种植引导从后台拉取的数据POP
 *
 * @author 李志军 2022-08-08 13:46
 */
class HomePlantUsuallyPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null,
    private var popData: MutableList<GuideInfoData.PlantInfo>? = null,
    private var data: GuideInfoData? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_usually_pop
    }

    private var binding: HomePlantUsuallyPopBinding? = null
    private val adapter by lazy {
        PlantInitPopAdapter(mutableListOf())
    }

    /**
     * 设置数据
     */
    fun setData(popData: GuideInfoData?) {
        binding?.btnSuccess?.isEnabled = false
        this.popData = popData?.items
        this.data = popData
    }

    override fun beforeShow() {
        super.beforeShow()
        // 设置数据
        popData?.let {
            adapter.setList(it)
        }
    }

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantUsuallyPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
            tvDec.text = data?.title

            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter

            // todo 需要判断是否有checkBox的存在，，如果没有checkBox的存在，那么按钮直接  = true
            // todo 但是目前不知道根据什么来判断是否显示checkBox
            adapter.addChildClickViewIds(R.id.cl_two, R.id.box, R.id.iv_pic)
            // 按钮点击
            adapter.setOnItemChildClickListener { adapter, view, position ->
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
                }
            }
        }
    }
}
