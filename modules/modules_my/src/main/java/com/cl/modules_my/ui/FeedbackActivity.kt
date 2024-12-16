package com.cl.modules_my.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.VersionUpdatePop
import com.cl.common_base.util.AppUtil
import com.cl.common_base.web.WebActivity
import com.cl.modules_my.databinding.MyFeedbackActivityBinding
import com.lxj.xpopup.XPopup


/**
 * 反馈界面
 */
class FeedbackActivity: BaseActivity<MyFeedbackActivityBinding>() {
    override fun initView() {
        binding.ftRate.setOnClickListener {
            startGooglePlay()
        }
        binding.ftUs.setOnClickListener {
            val intent = Intent(this@FeedbackActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://www.trustpilot.com/evaluate/heyabby.com")
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
            startActivity(intent)
        }
        binding.ftDiscord.setOnClickListener {
            // https://discord.gg/8F747ZGbuv
            val intent = Intent(this@FeedbackActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://discord.gg/8F747ZGbuv")
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
            startActivity(intent)
        }
        binding.ftInstagram.setOnClickListener {
            // https://www.instagram.com/heyabbygrowbox/
            val intent = Intent(this@FeedbackActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, "http://www.instagram.com/heyabby_official/")
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
            startActivity(intent)
        }
        binding.ftHeyabby.setOnClickListener {
            // 关于更多
            val intent = Intent(this@FeedbackActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.ABBY_OFFICIAL_WEBSITE)
            startActivity(intent)
        }
        binding.ftTerms.setOnClickListener {
            // 跳转到使用条款H5
            val localLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
            val intent = Intent(this@FeedbackActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, String.format(Constants.H5.PERSONAL_URL, localLanguage))
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
            startActivity(intent)
        }
        binding.ftPrivacy.setOnClickListener {
            // 跳转到隐私协议H5
            val localLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
            val intent = Intent(this@FeedbackActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, String.format(Constants.H5.PRIVACY_POLICY_URL, localLanguage))
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
            startActivity(intent)
        }
        binding.ftBecome.setOnClickListener {
            // https://heyabby.com/pages/brand-ambassador
            val intent = Intent(this@FeedbackActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/brand-ambassador")
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
            startActivity(intent)
        }
        binding.ftTell.setOnClickListener {
            // 发送短信
            XPopup.Builder(this@FeedbackActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false)
                .asCustom(
                    BaseCenterPop(this@FeedbackActivity, onConfirmAction = {
                        //"smsto:xxx" xxx是可以指定联系人的
                        val smsToUri = Uri.parse("smsto:")
                        val intent = Intent(Intent.ACTION_SENDTO, smsToUri)
                        //"sms_body"必须一样，smsbody是发送短信内容content
                        intent.putExtra("sms_body", getString(com.cl.common_base.R.string.string_1781))
                        startActivity(intent)
                    }, content = getString(com.cl.common_base.R.string.string_1782), confirmText = getString(com.cl.common_base.R.string.string_1783))
                ).show()
        }
    }

    override fun observe() {
    }

    override fun initData() {
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