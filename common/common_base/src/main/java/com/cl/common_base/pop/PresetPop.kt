package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.R
import com.cl.common_base.bean.PresetData
import com.cl.common_base.bean.ProModeInfoBean
import com.cl.common_base.databinding.PopPresetBinding
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.core.BottomPopupView

class PresetPop(
    context: Context,
    private val presetDataBean: PresetData? = null, // 这些就带了外面的一些机器的参数。
    private val listPreset: MutableList<ProModeInfoBean>? = null,
    private val onNextAction: ((PresetData?) -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_preset
    }

    /*private val listPreset = {
        Prefs.getObjects()
    }*/


    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PopPresetBinding>(popupImplView)?.apply {
            lifecycleOwner = this@PresetPop
            executePendingBindings()
            ivClose.setOnClickListener { dismiss() }
            ivClearEmail.setOnClickListener { etEmail.setText("") }
            btnSuccess.setSafeOnClickListener(lifecycleScope) {
                if (etEmail.text.toString().isEmpty()) {
                    ToastUtil.shortShow(context.getString(R.string.string_210))
                    return@setSafeOnClickListener
                }
                if (listPreset?.size == 5) {
                    ToastUtil.shortShow(context.getString(R.string.string_211))
                    return@setSafeOnClickListener
                }
                // save 之后是保存在本地。
                // 这是保存在本地时的需要添加的数据
                presetDataBean?.id = 0
                presetDataBean?.name = etEmail.text.toString()
                presetDataBean?.note = etNote.text.toString()
                // 保存在本地。
                // presetDataBean?.let { it1 -> Prefs.addObject(it1) }
                // 那就是把对象给gson化。
                onNextAction?.invoke(presetDataBean)
                // 保存成功
                // ToastUtil.shortShow("Save successful")
                dismiss()
            }
        }
    }
}