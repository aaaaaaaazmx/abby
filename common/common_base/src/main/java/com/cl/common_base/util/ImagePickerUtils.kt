package com.cl.common_base.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.cl.common_base.ext.equalsIgnoreCase
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.util.mesanbox.MeSandboxFileEngine
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.util.Locale

class ImagePickerUtils {

    // 请求权限并打开图片选择器的方法
    fun requestPermissionAndOpenPicker(
        context: FragmentActivity,  // 传入上下文，确保不会持有引用
        permissionMessage: String,
        onPermissionGranted: () -> Unit,
        vararg permissions: String,
    ) {
        PermissionHelp().applyPermissionHelp(
            context,
            permissionMessage,
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    // 权限通过后，执行选择图片的操作
                    onPermissionGranted()
                }
            },
            *permissions
        )
    }

    // 打开图片选择器
    fun openImagePicker(context: Context, maxSelectNum: Int = 1, requestCode: Int? = -1, call: OnResultCallbackListener<LocalMedia>? = null): PictureSelectionModel {
        val style = PictureSelectorStyle()
        val ss = BottomNavBarStyle()
        ss.isCompleteCountTips = false
        style.bottomBarStyle = ss

        // 获取语言设置
        val language = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en" // 获取当前语言，默认是英语
        val languageCode = when (language.lowercase()) {
            "en" -> LanguageConfig.ENGLISH
            "de" -> LanguageConfig.GERMANY
            "es" -> LanguageConfig.SPANISH
            else -> LanguageConfig.ENGLISH
        }

        return PictureSelector.create(context)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setCompressEngine(CompressFileEngine { ctx, source, calls ->
                Luban.with(ctx).load(source).ignoreBy(100)
                    .setCompressListener(object : OnNewCompressListener {
                        override fun onSuccess(source: String?, compressFile: File?) {
                            calls?.onCallback(source, compressFile?.absolutePath)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            calls?.onCallback(source, null)
                        }

                        override fun onStart() {}
                    }).launch()
            })
            .setSandboxFileEngine(MeSandboxFileEngine()) // Android10 沙盒文件
            .isOriginalControl(false) // 原图功能
            .isDisplayTimeAxis(true) // 资源轴
            .setEditMediaInterceptListener(null) // 是否开启图片编辑功能
            .isMaxSelectEnabledMask(true) // 是否显示蒙层
            .isDisplayCamera(false) // 是否显示摄像
            .setLanguage(languageCode) // 显示英语
            .setMaxSelectNum(maxSelectNum)
            .setSelectorUIStyle(style).apply {
                if (requestCode == -1 && null == call) {
                    // 兜底方案
                    forResult(PictureConfig.CHOOSE_REQUEST)
                } else if (requestCode != -1 && null == call) {
                    requestCode?.let { forResult(it) }
                } else if (requestCode == -1 && null != call) {
                    forResult(call)
                } else if (requestCode != -1 && null != call) {
                    // 瞎几把传的方案
                    forResult(PictureConfig.CHOOSE_REQUEST)
                }
            }
    }
}
