package com.cl.modules_contact.ui.pic

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class LanczosTransformation(private val radius: Int) : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("LanczosTransformation(radius=$radius)").toByteArray())
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val ratio = Math.min(outWidth.toFloat() / width, outHeight.toFloat() / height)
        val targetWidth = (width * ratio).toInt()
        val targetHeight = (height * ratio).toInt()
        return Bitmap.createScaledBitmap(toTransform, targetWidth, targetHeight, true)
    }
}