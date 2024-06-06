package com.cl.modules_my.ui

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.adapter.ExchangeAdapter
import com.cl.modules_my.databinding.MyExchangeActivityBinding
import com.cl.modules_my.viewmodel.WalletViewModel
import com.cl.modules_my.widget.RedeemPop
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 氧气币兑换界面
 */
@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class ExchangeActivity: BaseActivity<MyExchangeActivityBinding>() {
    /**
     * refreshToken 接口返回
     */
    private val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    @Inject
    lateinit var mViewModel: WalletViewModel

    // 选中的氧气币
    private var chooserOxygen = -1
    // 计算出来的价格
    private var exchangeRate = -1

    private val animation by lazy {
        val animation = AlphaAnimation(
            0f, 1f
        )
        animation.duration = 1500 //执行时间
        animation.repeatCount = 1 //重复执行动画
        animation
    }

    private val adapter by lazy {
        ExchangeAdapter(mutableListOf(), chooserOxygen = {
            // 更新当前氧气币的兑换
            // 100 / 20  = 美元
            runCatching {
                val money = it.safeToInt().div(mViewModel.exchangeInfo.value?.data?.exchangeRatio.safeToInt())
                binding.tvYouGetTitle.text = "$$money"
                chooserOxygen = it.safeToInt()
                exchangeRate = money.safeToInt()

                binding.tvYouGet.visibility = View.VISIBLE
                binding.clYouGet.visibility = View.VISIBLE
                binding.clYouGet.animation = animation
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        animation.cancel()
    }

    override fun initView() {
        mViewModel.getExchangeInfo()

        binding.rvMoney.layoutManager = LinearLayoutManager(this@ExchangeActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMoney.adapter = adapter

        binding.tvYouGet.visibility = View.GONE
        binding.clYouGet.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    override fun observe() {
        mViewModel.apply {
            exchangeInfo.observe(this@ExchangeActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    adapter.oxygen = data?.oxygen.toString()
                    adapter.setList(data?.exchangeAmounts?.toMutableList())

                    binding.tvDescription.text = data?.description
                    // *900 Oxygen Coins Available
                    binding.tvAvailable.text = "*${data?.oxygen} Oxygen Coins Available"
                }
            })

            exchangeVoucher.observe(this@ExchangeActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // Your redemption is complete! Please check your email for the e-gift card details.
                    xpopup(this@ExchangeActivity) {
                        dismissOnTouchOutside(false)
                        isDestroyOnDismiss(false)
                        asCustom(BaseCenterPop(this@ExchangeActivity, content = if (userInfo?.email.isNullOrEmpty()) "Your redemption is complete! " else "Your redemption is complete! Please check your email for the e-gift card details.", isShowCancelButton = false, confirmText = "OK", onConfirmAction = {
                            mViewModel.getExchangeInfo()
                            chooserOxygen = -1
                            exchangeRate = -1
                            binding.tvYouGet.visibility = View.GONE
                            binding.clYouGet.visibility = View.GONE

                            adapter.newSelectIndex = -1
                            adapter.oldSelectIndex = -1
                        })).show()
                    }
                }
            })
        }
    }

    override fun initData() {
        binding.tvTerms.setSafeOnClickListener {
            if (chooserOxygen == -1 && exchangeRate == -1) return@setSafeOnClickListener

            xpopup(this@ExchangeActivity) {
                dismissOnTouchOutside(true)
                isDestroyOnDismiss(false)
                asCustom(RedeemPop(this@ExchangeActivity, userInfo, chooserOxygen, exchangeRate, mViewModel.exchangeInfo.value?.data?.oxygen) {
                    // 确认兑换
                    mViewModel.exchangeVoucher(exchangeRate.toString())
                }).show()
            }
        }
    }
}