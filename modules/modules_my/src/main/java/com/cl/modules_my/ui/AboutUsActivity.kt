package com.cl.modules_my.ui

import android.content.Intent
import android.net.Uri
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.pop.VersionUpdatePop
import com.cl.common_base.util.AppUtil
import com.cl.common_base.web.WebActivity
import com.cl.modules_my.databinding.MyAboutUsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is a short description.
 *
 * @author 李志军 2022-08-23 16:05
 */
@AndroidEntryPoint
class AboutUsActivity: BaseActivity<MyAboutUsBinding>() {
    override fun initView() {
        // 文字加粗
        binding.ftLearn.setItemTitle(getString(com.cl.common_base.R.string.about_learn), true)
        binding.ftRate.setItemTitle(getString(com.cl.common_base.R.string.about_rate), true)
        binding.ftTermUse.setItemTitle(getString(com.cl.common_base.R.string.about_terms), true)
        binding.ftPrivacy.setItemTitle(getString(com.cl.common_base.R.string.about_policy), true)
    }

    override fun observe() {
    }

    override fun initData() {
        binding.ftPrivacy.setOnClickListener {
            // 跳转到隐私协议H5
            val intent = Intent(this@AboutUsActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PRIVACY_POLICY_URL)
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Privacy Policy")
            startActivity(intent)
        }

        binding.ftTermUse.setOnClickListener {
            // 跳转到使用条款H5
            val intent = Intent(this@AboutUsActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PERSONAL_URL)
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Terms of Use")
            startActivity(intent)
        }

        binding.ftRate.setOnClickListener {
            // 跳转谷歌市场
            startGooglePlay()
        }

        binding.ftLearn.setOnClickListener {
            // 关于更多
            val intent = Intent(this@AboutUsActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.ABBY_OFFICIAL_WEBSITE)
            startActivity(intent)
        }
    }

    private fun startGooglePlay() {
        val playPackage = AppUtil.appPackage
        try {
            val currentPackageUri: Uri = Uri.parse("market://details?id=" + AppUtil.appPackage)
            val intent = Intent(Intent.ACTION_VIEW, currentPackageUri)
            intent.setPackage(playPackage)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val currentPackageUri: Uri =
                Uri.parse(VersionUpdatePop.KEY_GOOGLE_PLAY_URL + AppUtil.appPackage)
            val intent = Intent(Intent.ACTION_VIEW, currentPackageUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}