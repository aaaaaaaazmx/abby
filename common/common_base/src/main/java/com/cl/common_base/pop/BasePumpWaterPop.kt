package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.cl.common_base.R
import com.cl.common_base.adapter.PumpWaterAdapter
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BasePumpWaterPopBinding
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.core.BottomPopupView
import me.yuqirong.cardswipelayout.CardItemTouchHelperCallback
import me.yuqirong.cardswipelayout.CardLayoutManager


/**
 * 排水弹窗
 *
 * @author 李志军 2022-08-10 15:06
 */
class BasePumpWaterPop(
    context: Context,
    private val onSuccessAction: ((status: Boolean) -> Unit)? = null,
    private var data: MutableList<AdvertisingData>? = null,
    private var onWaterFinishedAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_pump_water_pop
    }

    private val adapter by lazy {
        PumpWaterAdapter(mutableListOf())
    }

    fun setData(data: MutableList<AdvertisingData>) {
        this.data = data
    }

    override fun beforeShow() {
        super.beforeShow()
        adapter.setList(data)
    }

    var binding: BasePumpWaterPopBinding? = null
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<BasePumpWaterPopBinding>(popupImplView)?.apply {
            btnSuccess.setOnClickListener {
                onSuccessAction?.invoke(btnSuccess.isChecked)
                // true 排水
                // false 停止
                it.background = if (btnSuccess.isChecked) context.resources.getDrawable(
                    R.mipmap.base_start_bg,
                    context.theme
                ) else context.resources.getDrawable(R.mipmap.base_suspend_bg, context.theme)
            }
            ivClose.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
                // 涂鸦指令，添加排水功能
                isOpenOrStop(false)
            }

            rvAdd.adapter = adapter
            val cardCallback: CardItemTouchHelperCallback<AdvertisingData> =
                CardItemTouchHelperCallback<AdvertisingData>(adapter, adapter.data)
            val touchHelper = ItemTouchHelper(cardCallback)
            val cardLayoutManager = CardLayoutManager(rvAdd, touchHelper)
            rvAdd.layoutManager = cardLayoutManager
            touchHelper.attachToRecyclerView(rvAdd)
        }

        // 蓝牙状态监听变化
        LiveEventBus.get().with(Constants.Tuya.KEY_TUYA_DEVICE_TO_APP, String::class.java)
            .observe(this) {
                val map = GSON.parseObject(it, Map::class.java)
                map?.forEach { (key, value) ->
                    when (key) {
                        TuYaDeviceConstants.KAY_PUMP_WATER_FINISHED -> {
                            // 涂鸦指令，添加排水功能
                            isOpenOrStop(false)
                            // 排水成功
                            onWaterFinishedAction?.invoke()
                            dismiss()
                        }
                        TuYaDeviceConstants.KAY_PUMP_WATER -> {
                            // 涂鸦指令，添加排水功能
                            isOpenOrStop(value)
                        }
                    }
                }

            }
    }

    private fun isOpenOrStop(value: Any?) {
        DeviceControl.get()
            .success {
            }
            .error { code, error ->
                ToastUtil.shortShow(
                    """
                      pumpWater: 
                      code-> $code
                      errorMsg-> $error
                """.trimIndent()
                )
            }
            .pumpWater((value as? Boolean == true))
    }
}