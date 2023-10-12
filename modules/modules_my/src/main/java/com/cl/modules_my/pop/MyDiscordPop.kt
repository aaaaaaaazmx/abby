package com.cl.modules_my.pop

import android.content.Context
import android.content.Intent
import android.text.method.LinkMovementMethod
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDiscordPopBinding
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MyDiscordPop(
    context: Context,
    private val onConfirmAction: ((email: String?, code: String?) -> Unit)? = null,
) : BottomPopupView(context) {

    private val service = ServiceCreators.create(BaseApiService::class.java)

    override fun getImplLayoutId(): Int {
        return R.layout.my_discord_pop
    }

    // 查询是否绑定成功
    fun setQueryBind() {
        lifecycleScope.launch {
            queryBind()
        }
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            getLink()
        }
        DataBindingUtil.bind<MyDiscordPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@MyDiscordPop
            executePendingBindings()

            tvLogin.setOnClickListener {
                // 跳转绑定页面
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, discordLink)
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Discord")
                context.startActivity(intent)
            }

            // Not in the discord community yet? Click here to join.
            tvJoin.text = buildSpannedString {
                append("Not in the discord community yet? Click ")
                appendClickable("here", isUnderlineText = true) {
                     //  跳转到discord指定的频道
                    // https://discord.gg/8F747ZGbuv
                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(WebActivity.KEY_WEB_URL, "https://discord.gg/8F747ZGbuv")
                    intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "")
                    context.startActivity(intent)
                }
                append(" to join.")
            }
            tvJoin.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
            tvJoin.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
                resources,
                com.cl.common_base.R.color.transparent,
                context.theme
            )
        }
    }

    private var discordLink = ""
    private suspend fun getLink(){
        service.authorizeLink().map {
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
            logI(it.toString())
            when(it){
                is Resource.Success -> {
                    discordLink = it.data.toString()
                }
                is Resource.DataError -> {
                    ToastUtil.shortShow(it.errorMsg)
                }
                else -> {}
            }
        }
    }


    // 查询是否绑定成功
    private suspend fun queryBind() {
        service.isBind().map {
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
            logI(it.toString())
            when(it){
                is Resource.Success -> {
                    if (it.data.toString() == "true") {
                        // 绑定成功
                        ToastUtil.shortShow("Binding successful.")
                        dismiss()
                    }
                }
                is Resource.DataError -> {
                    ToastUtil.shortShow(it.errorMsg)
                }
                else -> {}
            }
        }
    }
}