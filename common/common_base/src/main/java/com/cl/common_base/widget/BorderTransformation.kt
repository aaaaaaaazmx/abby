package com.cl.common_base.widget

import android.content.Context

import android.graphics.Bitmap

import android.graphics.Canvas

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool


import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.min

class BorderTransformation(
    @DrawableRes private val frameResId: Int, // 头像框的资源ID
) : BitmapTransformation() {

    override fun transform(
        context: Context, pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int
    ): Bitmap? {
        // 从资源加载头像框
        val frameDrawable = ContextCompat.getDrawable(context, frameResId)
        val frameBitmap = frameDrawable?.toBitmap()

        // 确定边框的尺寸
        val frameWidth = frameBitmap?.width ?: outWidth
        val frameHeight = frameBitmap?.height ?: outHeight

        // 计算缩放比例以适应头像框内部空间
        val scale = min(
            (frameWidth - 40 * 3) / toTransform.width.toFloat(),
            (frameHeight - 40 * 3) / toTransform.height.toFloat()
        )

        // 缩放原始图像
        val scaledWidth = (toTransform.width * scale).toInt()
        val scaledHeight = (toTransform.height * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(toTransform, scaledWidth, scaledHeight, true)

        // 创建新的位图并设置画布
        val resultBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // 计算绘制原始图像的位置
        val left = (frameWidth - scaledWidth) / 2f
        val top = (frameHeight - scaledHeight) / 2f

        // 绘制原始图像
        canvas.drawBitmap(scaledBitmap, left, top, null)

        // 确定边框的尺寸，如果需要，将边框缩放到与ImageView相同的尺寸
        val frameScaledBitmap = if (frameWidth != outWidth || frameHeight != outHeight) {
            Bitmap.createScaledBitmap(frameBitmap!!, outWidth, outHeight, true)
        } else {
            frameBitmap
        }

        // 绘制头像框图像
        frameBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // 清理
        scaledBitmap.recycle()

        // 使用Glide的BitmapPool确保内存管理
        return BitmapResource.obtain(resultBitmap, pool)?.get()
    }

    override fun updateDiskCacheKey(@NonNull messageDigest: MessageDigest) {
        messageDigest.update(("circle_frame" + frameResId).toByteArray(Charset.forName("UTF-8")))
    }

    override fun equals(other: Any?): Boolean {
        return other is BorderTransformation && other.frameResId == frameResId
    }

    override fun hashCode(): Int {
        return frameResId.hashCode()
    }
}
