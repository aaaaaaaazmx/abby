package com.cl.modules_my.ui

import android.content.Intent
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.databinding.FragmentMyBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint

/**
 * 我的页面
 */
@Route(path = RouterPath.My.PAGE_MY)
@AndroidEntryPoint
class MyFragment : BaseFragment<FragmentMyBinding>() {

    override fun initView(view: View) {
        // 跳转到个人设置界面
        binding.rlEdit.setOnClickListener {
            startActivity(Intent(context, ProfileActivity::class.java))
        }
        binding.llRoot.setOnClickListener {
            startActivity(Intent(context, ProfileActivity::class.java))
        }
        binding.ivHead.setOnClickListener {
            startActivity(Intent(context, ProfileActivity::class.java))
        }

        binding.clMessgae.setOnClickListener {

        }

        binding.clSetting.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }

        binding.clAbout.setOnClickListener {
            startActivity(Intent(context, AboutUsActivity::class.java))
        }

    }

    override fun lazyLoad() {

    }

    override fun onResume() {
        super.onResume()
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)

        // 头像链接
        val headUrl = parseObject?.avatarPicture ?: parseObject?.userDetailData?.avatarPicture

        // 头像的显示与隐藏
        ViewUtils.setVisible(headUrl.isNullOrEmpty(), binding.noheadShow)
        binding.ivHead.visibility = if (headUrl.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

        // 设置头像
        Glide.with(this)
            .load(headUrl)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.ivHead) //标准圆形图片。
        // 名字
        binding.tvName.text = parseObject?.nickName ?: parseObject?.userDetailData?.nickName
        binding.tvEmail.text = parseObject?.email ?: parseObject?.userDetailData?.email

        // 显示和隐藏
        if (headUrl.isNullOrEmpty()) {
            val name = parseObject?.nickName ?: parseObject?.userDetailData?.nickName
            binding.noheadShow.text = name?.substring(0, 1)
        }
    }

    override fun FragmentMyBinding.initBinding() {

    }
}