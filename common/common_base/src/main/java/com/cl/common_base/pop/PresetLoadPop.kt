package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.R
import com.cl.common_base.bean.ProModeInfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.PopPresetLoadBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * load弹窗。
 */
class PresetLoadPop(
    context: Context,
    private val listPreset: MutableList<ProModeInfoBean>? = null,
    private val onNextAction: ((ProModeInfoBean?) -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_preset_load
    }

    private val service = ServiceCreators.create(BaseApiService::class.java)
    private var binding: PopPresetLoadBinding? = null

    /*private val listPreset = {
        Prefs.getObjects()
    }*/

    private var index = -1

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<PopPresetLoadBinding>(popupImplView)?.apply {
            lifecycleOwner = this@PresetLoadPop
            executePendingBindings()

            btnSuccess.setSafeOnClickListener(lifecycleScope) {
                // load 是调用dp接口
                // 这个让数据保持一直，还没做好。需要和发送dp点给涂鸦设备。
                if (index == -1) {
                    ToastUtil.shortShow("Please select the preset")
                    return@setSafeOnClickListener
                }
                onNextAction?.invoke(listPreset?.get(index))
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
                                lifecycleScope.launch {
                                    deletePresets(listPreset?.get(index)?.id ?: 0, index)
                                }
                            })
                        ).show()
                    }
                }
            }

            runCatching {
                clEmailInput.setSafeOnClickListener(lifecycleScope) {
                    val stringList = listPreset?.map { it.name }?.toMutableList()
                    if (stringList?.isEmpty() == true) {
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
                                    etEmail.text = stringList?.getOrNull(it)
                                    etNote.text = listPreset?.get(index)?.notes
                                })
                        ).show()
                    }
                }
            }

            ivClose.setOnClickListener { dismiss() }
        }
    }

    /**
     * 删除当前预制模版。
     */
   private suspend fun deletePresets(id: Int, index: Int) {
        // service.showAchievement(bean.relationId)
        service.deleteProModeRecord(id.toString()).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            if (it is Resource.DataError) {
                ToastUtil.show(it.errorMsg)
            }
            if (it is Resource.Success) {
                // Prefs.removeObject(listPreset?.get(index))
                listPreset?.removeAt(index)
                ToastUtil.shortShow("Delete successful")
                binding?.etEmail?.text = "Select Preset"
                binding?.etNote?.text = ""
                this.index = -1
            }
        }
    }
}