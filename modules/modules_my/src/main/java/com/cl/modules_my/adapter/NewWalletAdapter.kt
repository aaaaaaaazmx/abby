package com.cl.modules_my.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.ext.DateHelper.formatToStr
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.safeToLong
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.web.VideoPLayActivity
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyNewWalletItemBinding
import com.cl.modules_my.request.VoucherBean

class NewWalletAdapter (data: MutableList<VoucherBean>?) :
    BaseQuickAdapter<VoucherBean, BaseDataBindingHolder<MyNewWalletItemBinding>>(com.cl.modules_my.R.layout.my_new_wallet_item, data) {


    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseDataBindingHolder<MyNewWalletItemBinding>, item: VoucherBean) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()

            // 设置时间
            if (!item.startTime.isNullOrEmpty() && !item.endTime.isNullOrEmpty()) {
                tvTime.text = "${formatToStr(item.startTime.safeToLong(), "yyyy/MM/dd")}-${formatToStr(item.endTime.safeToLong(), "yyyy/MM/dd")}"
            }

            ivCopy.setSafeOnClickListener {
                // 复制到粘贴板
                // 复制评论，需要是自己发的帖子
                // 复制内容
                val cm: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                // 创建普通字符型ClipData
                val mClipData = ClipData.newPlainText("Code", item.discountCode)
                // 将ClipData内容放到系统剪贴板里。
                cm?.setPrimaryClip(mClipData)

                xpopup(context) {
                    isDestroyOnDismiss(false)
                    dismissOnTouchOutside(true)
                    asCustom(BaseCenterPop(context, confirmText = "OK", isShowCancelButton = false, content = "Your coupon code has been copied!")).show()
                }
            }

            tvShopNow.setSafeOnClickListener {
                // 复制到粘贴板
                // 复制评论，需要是自己发的帖子
                // 复制内容
                val cm: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                // 创建普通字符型ClipData
                val mClipData = ClipData.newPlainText("Code", item.discountCode)
                // 将ClipData内容放到系统剪贴板里。
                cm?.setPrimaryClip(mClipData)

                val intent = Intent(context, VideoPLayActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, item.url)
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, item.title)
                context.startActivity(intent)
            }

            ivDown.setSafeOnClickListener {
                // 弹窗。告知用户是怎么使用的
                xpopup(context) {
                   dismissOnTouchOutside(true)
                    isDestroyOnDismiss(false)
                    asCustom(BaseCenterPop(context, confirmText = "OK", isShowCancelButton = false, content = item.description)).show()
                }
            }
        }
    }
}