package com.cl.common_base.pop

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cl.common_base.R
import com.cl.common_base.adapter.PumpWaterAdapter
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BasePumpWaterPopBinding
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.widget.toast.ToastUtil
import com.lin.cardlib.CardLayoutManager
import com.lin.cardlib.CardSetting
import com.lin.cardlib.CardTouchHelperCallback
import com.lin.cardlib.OnSwipeCardListener
import com.lin.cardlib.utils.ReItemTouchHelper
import com.lxj.xpopup.core.BottomPopupView
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.bean.DeviceBean
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlin.concurrent.thread


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
    // 获取当前设备信息
    private val tuYaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

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
        if (data?.size == 0) return
        kotlin.runCatching {
            // 卡片布局需要展示3张，所以需要多添加几张
            if ((data?.size ?: 0) == 1) {
                data?.get(0)?.let { data?.add(it) }
            }
            if ((data?.size ?: 0) == 2) {
                data?.get(0)?.let { data?.add(it) }
                data?.get(1)?.let { data?.add(it) }
            }
            adapter.setList(data)
        }
    }

    override fun onDismiss() {
        // 涂鸦指令，添加排水功能
        isOpenOrStop(false)
        // 因为是通过by lazy 添加的， 所以状态需要重置
        binding?.btnSuccess?.isChecked = true
        binding?.btnSuccess?.let {
            it.background = context.resources.getDrawable(
                R.mipmap.base_start_bg,
                context.theme
            )
            binding?.tvAddClockTime?.text = "Click the button to start draining"
        }
        super.onDismiss()
    }

    var binding: BasePumpWaterPopBinding? = null
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<BasePumpWaterPopBinding>(popupImplView)?.apply {
            btnSuccess.setOnClickListener {
                onSuccessAction?.invoke(btnSuccess.isChecked)
                // true 排水
                // false 停止
                synchronized(this) {
                    it.background = if (btnSuccess.isChecked) context.resources.getDrawable(
                        R.mipmap.base_start_bg,
                        context.theme
                    ) else context.resources.getDrawable(R.mipmap.base_suspend_bg, context.theme)

                    tvAddClockTime.text =
                        if (btnSuccess.isChecked) "Click the button to start draining"
                        else "Click the button to stop draining"
                }
            }
            ivClose.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
            }


            val setting = CardSetting()
            setting.setSwipeListener(object : OnSwipeCardListener<AdvertisingData?> {
                override fun onSwiping(
                    viewHolder: RecyclerView.ViewHolder,
                    dx: Float,
                    dy: Float,
                    direction: Int
                ) {
                    when (direction) {
                        ReItemTouchHelper.DOWN -> Log.e("aaa", "swiping direction=down")
                        ReItemTouchHelper.UP -> Log.e("aaa", "swiping direction=up")
                        ReItemTouchHelper.LEFT -> Log.e("aaa", "swiping direction=left")
                        ReItemTouchHelper.RIGHT -> Log.e("aaa", "swiping direction=right")
                    }
                }

                override fun onSwipedOut(
                    viewHolder: RecyclerView.ViewHolder,
                    o: AdvertisingData?,
                    direction: Int
                ) {
//                    when (direction) {
//                        ReItemTouchHelper.DOWN -> Toast.makeText(
//                            context,
//                            "swipe down out",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        ReItemTouchHelper.UP -> Toast.makeText(
//                            context,
//                            "swipe up out ",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        ReItemTouchHelper.LEFT -> Toast.makeText(
//                            context,
//                            "swipe left out",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        ReItemTouchHelper.RIGHT -> Toast.makeText(
//                            context,
//                            "swipe right out",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
                }

                override fun onSwipedClear() {
//                    Toast.makeText(context, "cards are consumed", Toast.LENGTH_SHORT)
//                        .show()
                }
            })

            rvAdd.adapter = adapter
            val helperCallback: CardTouchHelperCallback<AdvertisingData> =
                CardTouchHelperCallback<AdvertisingData>(rvAdd, adapter.data, setting)
            val mReItemTouchHelper = ReItemTouchHelper(helperCallback)
            val layoutManager = CardLayoutManager(mReItemTouchHelper, setting)
            rvAdd.layoutManager = layoutManager

            // 手动滑动第一张
            handler.postDelayed(Runnable {
                mReItemTouchHelper.swipeManually(ReItemTouchHelper.RIGHT)
            }, 3000)
        }

        // 蓝牙状态监听变化
        LiveEventBus.get().with(Constants.Tuya.KEY_TUYA_DEVICE_TO_APP, String::class.java)
            .observe(this) {
                val map = GSON.parseObject(it, Map::class.java)
                map?.forEach { (key, value) ->
                    when (key) {
                        TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                            // 涂鸦指令，添加排水功能
//                            isOpenOrStop(false)
                            // 排水成功
                            if ((value as? Boolean == false)) return@observe
                            onWaterFinishedAction?.invoke()
                            dismiss()
                        }
                        //
                        TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_INSTRUCTIONS -> {
                            // 涂鸦指令，添加排水功能
//                            isOpenOrStop(value)
                            synchronized(this) {
                                // 加锁的目的是为了
                                // 当用户在点击时,又突然接收到设备的指令,导致显示不正确的问题
                                binding?.btnSuccess?.background =
                                    if ((value as? Boolean != true)) context.resources.getDrawable(
                                        R.mipmap.base_start_bg,
                                        context.theme
                                    ) else context.resources.getDrawable(
                                        R.mipmap.base_suspend_bg,
                                        context.theme
                                    )

                                binding?.tvAddClockTime?.text =
                                    if ((value as? Boolean != true)) "Click the button to start draining"
                                    else "Click the button to stop draining"
                            }

                            if ((value as? Boolean == true)) return@observe
                            // 查询是否排水结束
                            TuyaHomeSdk.newDeviceInstance(tuYaDeviceBean?.devId)?.let {
                                it.getDp(TuYaDeviceConstants.KAY_PUMP_WATER_FINISHED, object :
                                    IResultCallback {
                                    override fun onError(code: String?, error: String?) {
                                        logI(
                                            """
                                            KAY_PUMP_WATER_FINISHED: error
                                            code: $code
                                            error: $error
                                        """.trimIndent()
                                        )
                                    }

                                    override fun onSuccess() {
                                        logI("onSuccess")
                                    }
                                })
                            }
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