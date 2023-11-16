package com.cl.common_base.pop

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cl.common_base.R
import com.cl.common_base.bean.MedalPopData
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.MedalPopBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MedalPop(context: Context, private val medalPopData: MedalPopData?) : BottomPopupView(context) {
    private val service by lazy {
        ServiceCreators.create(BaseApiService::class.java)
    }

    override fun getImplLayoutId(): Int {
        return R.layout.medal_pop
    }

    private var binding: MedalPopBinding? = null

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<MedalPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@MedalPop
            executePendingBindings()

            ivClose.setOnClickListener {
                dismiss()
            }

            medalPopData?.let { bean ->
                tvAddAccessory.text = bean.describe
                tvAddAccessoryTitle.text = bean.name
                ivAddAccessory.let { Glide.with(context).load(bean.backgroundPicture).into(it) }
                // 隐藏
                ViewUtils.setVisible(bean.describe.isEmpty() || bean.popupType != "frame", tvAddAccessory)
                ViewUtils.setVisible(bean.name.isEmpty() || bean.popupType != "frame", tvAddAccessoryTitle)
                tvAdd.setOnClickListener {
                    if (tvAdd.text == "Remove") {
                        when (bean.popupType) {
                            "frame" -> {
                                lifecycleScope.launch {
                                    showFrame(0)
                                }
                            }

                            "achievement" -> {
                                lifecycleScope.launch {
                                    showAchievement(0)
                                }
                            }
                        }
                        return@setOnClickListener
                    }
                    when (bean.popupType) {
                        "frame" -> {
                            lifecycleScope.launch {
                                showFrame(bean.relationId)
                            }
                        }

                        "achievement" -> {
                            lifecycleScope.launch {
                                showAchievement(bean.relationId)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun showFrame(relationId: Int) {
        // service.showFrame(bean.relationId)
        service.showFrame(relationId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            if (it is Resource.DataError) {
                ToastUtil.show(it.errorMsg)
            }
            if (it is Resource.Success) {
                if (binding?.tvAdd?.text == "Remove") {
                    binding?.tvAdd?.text = "Use"
                } else {
                    binding?.tvAdd?.text = "Remove"
                }
            }
        }
    }

    private suspend fun showAchievement(relationId: Int) {
        // service.showAchievement(bean.relationId)
        service.showAchievement(relationId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            if (it is Resource.DataError) {
                ToastUtil.show(it.errorMsg)
            }
            if (it is Resource.Success) {
                if (binding?.tvAdd?.text == "Remove") {
                    binding?.tvAdd?.text = "Use"
                } else {
                    binding?.tvAdd?.text = "Remove"
                }
            }
        }
    }
}