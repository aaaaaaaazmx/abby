package com.cl.modules_contact.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition


abstract class SeamlessImageViewTargets<Z : Any>(view: ImageView) : CustomViewTarget<ImageView, Z>(view),
    Transition.ViewAdapter {
    private var animatable: Animatable? = null

    override fun onStart() {
        animatable?.start()
    }

    override fun onStop() {
        animatable?.stop()
    }

    override fun getCurrentDrawable(): Drawable? = view.drawable

    override fun setDrawable(drawable: Drawable?) {
        view.setImageDrawable(drawable)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        setResourceInternal(null)
        setDrawable(errorDrawable)
    }

    override fun onResourceLoading(placeholder: Drawable?) {
        super.onResourceLoading(placeholder)
        // placeholder 为空则不设置，会显示上一张图
        placeholder?.let { setDrawable(placeholder) }
    }

    override fun onResourceReady(resource: Z, transition: Transition<in Z>?) {
        if (transition == null || !transition.transition(resource, this)) {
            setResourceInternal(resource)
        } else {
            maybeUpdateAnimatable(resource)
        }
    }

    override fun onResourceCleared(placeholder: Drawable?) {
        animatable?.stop()
        // placeholder 为空则不设置，会显示上一张图
        placeholder?.let { setDrawable(placeholder) }
    }

    private fun setResourceInternal(resource: Z?) {
        // Order matters here. Set the resource first to make sure that the Drawable has a valid and
        // non-null Callback before starting it.
        setResource(resource)
        maybeUpdateAnimatable(resource)
    }

    private fun maybeUpdateAnimatable(resource: Z?) {
        animatable = if (resource is Animatable) {
            resource.apply { start() }
        } else {
            null
        }
    }

    protected abstract fun setResource(resource: Z?)
}

class SeamlessBitmapImageViewTarget(view: ImageView) : SeamlessImageViewTargets<Bitmap>(view) {
    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
    }
}

class SeamlessDrawableImageViewTarget(view: ImageView) : SeamlessImageViewTargets<Drawable>(view) {
    override fun setResource(resource: Drawable?) {
        view.setImageDrawable(resource)
    }
}