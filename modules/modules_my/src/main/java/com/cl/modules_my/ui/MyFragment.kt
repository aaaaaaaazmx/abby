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
import com.cl.common_base.R
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.viewmodel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 我的页面
 */
@Route(path = RouterPath.My.PAGE_MY)
@AndroidEntryPoint
class MyFragment : BaseFragment<FragmentMyBinding>() {
    @Inject
    lateinit var mViewMode: MyViewModel

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

        // 解答疑问
        binding.clTrouble.setOnClickListener {
            startActivity(Intent(context, MyTroubleShootingActivity::class.java))
        }

        // How to
        binding.clHow.setOnClickListener {
            startActivity(Intent(context, MyHowToActivity::class.java))
        }
    }

    override fun observe() {
        mViewMode.apply {
            userDetail.observe(viewLifecycleOwner, resourceObserver {
                loading { }
                error { errorMsg, code ->
                    errorMsg?.let {
                        ToastUtil.shortShow(it)
                    }
                }
                success {
                    data?.let {
                        // 头像链接
                        val headUrl = it.avatarPicture

                        // 头像的显示与隐藏
                        ViewUtils.setVisible(headUrl.isNullOrEmpty(), binding.noheadShow)
                        binding.ivHead.visibility =
                            if (headUrl.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

                        // todo 设置头像没有添加占位图
                        Glide.with(this@MyFragment)
                            .load(headUrl)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.ivHead) //标准圆形图片。
                        // 名字
                        binding.tvName.text = it.nickName
                        binding.tvEmail.text = it.email

                        // 显示和隐藏
                        if (headUrl.isNullOrEmpty()) {
                            val name = it.nickName
                            binding.noheadShow.text = name?.substring(0, 1)
                        }
                    }
                }
            })
        }
    }

    override fun lazyLoad() {

    }

    override fun onResume() {
        super.onResume()
        // 获取用户信息
        mViewMode.userDetail()
    }

    override fun FragmentMyBinding.initBinding() {

    }
}