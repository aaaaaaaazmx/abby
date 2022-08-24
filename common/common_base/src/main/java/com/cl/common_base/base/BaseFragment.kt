package com.cl.common_base.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.cl.common_base.BaseBinding
import com.cl.common_base.R
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.orhanobut.logger.Logger
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewDataBinding> : Fragment(), BaseBinding<VB> {

    /**
     * 日志输出标志
     */
    protected val TAG: String = this.javaClass.simpleName

    private var isLoaded = false

    /**
     * 防止回退到上一级页面时还会init view的问题
     */
    private var isInitializedRootView = false
    private var _binding: VB? = null

    protected val binding get() = _binding!!
    private var rootView: View? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            kotlin.runCatching {
                val type = javaClass.genericSuperclass as ParameterizedType
                val clazz2 = type.actualTypeArguments[0] as Class<VB>
                val method = clazz2.getMethod("inflate", LayoutInflater::class.java)
                _binding = method.invoke(null, layoutInflater) as VB
                rootView = _binding?.root
            }.onFailure {
                Logger.e("init root view error = ${it.message}")
            }
        }
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (isInitializedRootView) return
        super.onViewCreated(view, savedInstanceState)
        binding.initBinding()
        initView(view)
        observe()
        isInitializedRootView = true
    }

    override fun onStart() {
        super.onStart()
        // 蓝牙状态监听变化
        LiveEventBus.get().with(Constants.Ble.KEY_BLE_STATE, String::class.java)
            .observe(viewLifecycleOwner) {
                onBleChange(it)
            }
        // 设备状态监听变化
        LiveEventBus.get().with(Constants.Device.KEY_DEVICE_TO_APP, String::class.java)
            .observe(viewLifecycleOwner) {
                onDeviceChange(it)
            }
        // 涂鸦发送给app数据监听
        LiveEventBus.get().with(Constants.Tuya.KEY_TUYA_DEVICE_TO_APP, String::class.java)
            .observe(this) {
                onTuYaToAppDataChange(it)
            }
    }

    override fun onResume() {
        super.onResume()
        if (!isLoaded && !isHidden) {
            lazyLoad()
            isLoaded = true
        }
    }


    fun hideProgressLoading() {

        (context as? BaseActivity<*>)?.hideProgressLoading()
    }

    fun showProgressLoading(
        text: String? = getString(R.string.loading),
        cancelable: Boolean = true
    ) {
        (context as? BaseActivity<*>)?.showProgressLoading(text, cancelable)
    }

    /**
     * 初始化 View
     */
    abstract fun initView(view: View)

    /**
     * 懒加载
     */
    abstract fun lazyLoad()

    open fun observe() {
    }

    /**
     * 设备状态改变回调
     *
     * @param status
     */
    open fun onBleChange(status: String) {}

    /**
     * 设备状态改变回调
     */
    open fun onDeviceChange(status: String) {}

    /**
     * 涂鸦发送给App的信息
     */
    open fun onTuYaToAppDataChange(status: String) {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        rootView = null
    }
}