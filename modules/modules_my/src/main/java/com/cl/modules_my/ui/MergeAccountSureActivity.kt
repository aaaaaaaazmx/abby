package com.cl.modules_my.ui

import android.content.Intent
import androidx.core.text.buildSpannedString
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.bean.UserinfoBean.BasicUserBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.showToast
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.Prefs.getString
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.json.GSON.parseObject
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyMergrAccountBinding
import com.cl.modules_my.request.MergeAccountReq
import com.cl.modules_my.viewmodel.CloneAndReplantViewModel
import com.cl.modules_my.viewmodel.MergeAccountSureViewModel
import com.hyphenate.helpdesk.easeui.agora.board.misc.flat.FileConverter
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 合并账号确认界面
 */
@AndroidEntryPoint
class MergeAccountSureActivity : BaseActivity<MyMergrAccountBinding>() {

    @Inject
    lateinit var mViewModel: MergeAccountSureViewModel

    val email by lazy {
        intent.getStringExtra("email")
    }

    val code by lazy {
        intent.getStringExtra("code")
    }


    private val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        parseObject(bean, UserinfoBean::class.java)
    }


    override fun initView() {

        binding.tvDesc.text = buildSpannedString {
            // 3 Month Digital will be added to dee@baypac.com
            /*bold { append(it) }
            appendLine()
            color(context.getColor(R.color.mainColor)) {
                bold {
                    appendLine("$richText")
                }
            }*/
            /*Please be ware that [被合并的email address] will be merged and deleted and this cannot be undone.

            the below items will be merged into [主账号】
            1. Your subscription. (the subscription will be associated with the device) There is no need to pay for the existing subscription again.

            2. Plant information and progress

            3. Oxygen coins and transaction records

            4. Calendar

            5. Your trend post and replies*/
            appendLine("Please be ware that $email will be merged and deleted and this cannot be undone.")
            appendLine()
            appendLine("the below items will be merged into ${userinfoBean?.email}")
            appendLine()
            appendLine("1. Your subscription. (the subscription will be associated with the device) There is no need to pay for the existing subscription again.")
            appendLine()
            appendLine("2. Plant information and progress")
            appendLine()
            appendLine("3. Oxygen coins and transaction records")
            appendLine()
            appendLine("4. Calendar")
            appendLine()
            appendLine("5. Your trend post and replies")
            appendLine()
        }

    }


    override fun observe() {
        mViewModel.mergeAccount.observe(this@MergeAccountSureActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                hideProgressLoading()
                ToastUtil.shortShow(errorMsg)

                /*The accounts could not be merged, please try again. If you need further assistance, email:support@heyabby.com*/
                // 合并失败弹窗
                XPopup.Builder(this@MergeAccountSureActivity)
                    .isDestroyOnDismiss(false)
                    .isDestroyOnDismiss(false)
                    .asCustom(
                        BaseCenterPop(this@MergeAccountSureActivity,
                            titleText = "Congratulations!",
                            confirmText = "Try Again",
                            cancelText = "Go Back",
                            content = "The accounts could not be merged, please try again. If you need further assistance, email:support@heyabby.com",
                            isShowCancelButton = true,
                            onConfirmAction = {
                                // todo 重试合并
                                mViewModel.mergeAccount(req = MergeAccountReq(this@MergeAccountSureActivity.code, email))
                            })
                    )
                    .show()
            }
            success {
                hideProgressLoading()
                XPopup.Builder(this@MergeAccountSureActivity)
                    .isDestroyOnDismiss(false)
                    .isDestroyOnDismiss(false)
                    .asCustom(
                        BaseCenterPop(this@MergeAccountSureActivity,
                            titleText = "Congratulations!",
                            confirmText = "Go to Device List",
                            content = "Congrats! The merge is complete. You can now manage all your devices in the device management page", isShowCancelButton = false, onConfirmAction = {
                                // todo 跳转到设备列表界面
                                startActivity(Intent(this@MergeAccountSureActivity, DeviceListActivity::class.java))
                                this@MergeAccountSureActivity.finish()
                            })
                    )
                    .show()
            }
        })
    }

    override fun initData() {
        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnMeger.setOnClickListener {
            // todo 调用接口、还差一个合并失败的弹窗
            mViewModel.mergeAccount(
                req = MergeAccountReq(code, email)
            )
        }
    }
}