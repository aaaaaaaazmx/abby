package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.R
import com.cl.common_base.bean.PresetData
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.PopPresetLoadBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.core.BottomPopupView

/**
 * load弹窗。
 */
class PresetLoadPop(
    context: Context,
    private val presetDataBean: PresetData? = null, // 这些就带了外面的一些机器的参数。
    private val onNextAction: ((PresetData?) -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_preset_load
    }

    private val listPreset = {
        Prefs.getObjects()
    }

    private var index = -1

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PopPresetLoadBinding>(popupImplView)?.apply {
            lifecycleOwner = this@PresetLoadPop
            executePendingBindings()

            btnSuccess.setSafeOnClickListener(lifecycleScope) {
                // load 是调用dp接口
                // 这个让数据保持一直，还没做好。需要和发送dp点给涂鸦设备。
                if (index == -1) {
                    ToastUtil.shortShow("Please select the preset")
                    return@setSafeOnClickListener
                }
                onNextAction?.invoke(listPreset()[index])
                dismiss()
            }

            btnDelete.setSafeOnClickListener(lifecycleScope) {
                runCatching {
                    xpopup(context) {
                        dismissOnTouchOutside(false)
                        isDestroyOnDismiss(false)
                        asCustom(
                            BaseCenterPop(context, content = "Are you sure you want to delete this preset?", onConfirmAction = {
                                if (index == -1) return@BaseCenterPop
                                Prefs.removeObject(listPreset()[index])
                                ToastUtil.shortShow("Delete successful")
                                etEmail.text = "Select Preset"
                                etNote.text = ""
                                index = -1
                            })
                        ).show()
                    }
                }
            }

            runCatching {
                clEmailInput.setSafeOnClickListener(lifecycleScope) {
                    val stringList = listPreset().map { it.name }.toMutableList()
                    if (stringList.isEmpty()) {
                        ToastUtil.shortShow("No preset")
                        return@setSafeOnClickListener
                    }
                    // 显示已经save的配置
                    xpopup(context) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(false)
                        asCustom(
                            BaseStringPickPop(context,
                                title = "Preset",
                                listString = stringList,
                                confirmAction = {
                                    index = it
                                    etEmail.text = stringList.getOrNull(it)
                                    etNote.text = listPreset()[index].note
                                })
                        ).show()
                    }
                }
            }

            ivClose.setOnClickListener { dismiss() }
        }
    }
}