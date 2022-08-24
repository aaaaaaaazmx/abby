package com.cl.modules_pairing_connection.ui

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.modules_pairing_connection.databinding.PairReconnectBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 重新连接提示页面
 *
 * @author 李志军 2022-08-03 17:50
 */
@Route(path = RouterPath.PairConnect.KEY_PAIR_RECONNECTING)
@AndroidEntryPoint
class PairReconnectActivity : BaseActivity<PairReconnectBinding>() {
    /**
     * true -> guideOne
     * false -> bleTimeOut
     */
    private val extra by lazy {
        intent.getBooleanExtra(KEY_BLE_TIME_OUT_OR_GUIDE_CLICK, false)
    }


    override fun initView() {

    }

    override fun observe() {
    }

    override fun initData() {
        binding.cbBox.setOnCheckedChangeListener { compoundButton, b ->
            binding.btnSuccess.isEnabled = b
        }

        binding.btnSuccess.setOnClickListener {
            // 具体跳准到哪个页面
            startActivity(Intent(this, PairOnePageActivity::class.java))
        }
    }


    companion object {
        // 这个页面有2个地方会跳转过来
        // 需要判断是从哪个地方跳转过来到
        const val KEY_BLE_TIME_OUT_OR_GUIDE_CLICK = "KEY_BLE_TIME_OUT_OR_GUIDE_CLICK"
    }

}