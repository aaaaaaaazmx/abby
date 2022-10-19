package com.cl.common_base.pop

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.bean.AppVersionData
import com.cl.common_base.databinding.BaseVersionUpdateBinding
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.ViewUtils
import com.lxj.xpopup.core.CenterPopupView


/**
 * app升级弹窗pop
 * @author 李志军 2022-08-16 15:55
 */
class VersionUpdatePop(
    context: Context,
    private var appVersionData: AppVersionData? = null,
    private val onConfirmAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_version_update
    }

    var forcedUpdate = "0"
    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseVersionUpdateBinding>(popupImplView)?.apply {
            tvContent.text =
                "To ensure the best user experience, you must update your app to the latest version."
            appVersionData?.let {
                forcedUpdate = it.forcedUpdate.toString()
                // 0 不强制，1 强制升级
                ViewUtils.setVisible(it.forcedUpdate == "0", tvCancel, xpopupDivider2)
            }

            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke()
                // 跳转到谷歌市场
                startGooglePlay()
                // 强制升级才可以取消弹窗
                if (appVersionData?.forcedUpdate == "0")dismiss()
            }
            tvCancel.setOnClickListener {
                onCancelAction?.invoke()
                // 强制升级才可以取消弹窗
                if (appVersionData?.forcedUpdate == "0")dismiss()
            }
        }
    }

    private fun startGooglePlay() {
        val playPackage = AppUtil.appPackage
        try {
            val currentPackageUri: Uri = Uri.parse("market://details?id=" + AppUtil.appPackage)
            val intent = Intent(Intent.ACTION_VIEW, currentPackageUri)
            intent.setPackage(playPackage)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val currentPackageUri: Uri =
                Uri.parse(KEY_GOOGLE_PLAY_URL + AppUtil.appPackage)
            val intent = Intent(Intent.ACTION_VIEW, currentPackageUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun setData(data: AppVersionData) {
        this.appVersionData = data
    }

    override fun onBackPressed(): Boolean {
        return forcedUpdate != "0"
    }

    companion object {
        const val KEY_GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id="
    }

}