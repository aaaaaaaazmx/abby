package com.cl.modules_my.pop

import android.view.animation.Animation
import android.view.animation.Transformation
import android.graphics.Camera
import android.graphics.Matrix

class Rotate3dAnimation(private val fromDegrees: Float, private val toDegrees: Float,
                        private val centerX: Float, private val centerY: Float,
                        private val depthZ: Float, private val reverse: Boolean) : Animation() {

    private lateinit var camera: Camera

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        camera = Camera()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val degrees = fromDegrees + (toDegrees - fromDegrees) * interpolatedTime
        val matrix = t.matrix

        camera.save()
        if (reverse) {
            camera.translate(0.0f, 0.0f, depthZ * interpolatedTime)
        } else {
            camera.translate(0.0f, 0.0f, depthZ * (1.0f - interpolatedTime))
        }
        camera.rotateY(degrees)
        camera.getMatrix(matrix)
        camera.restore()

        matrix.preTranslate(-centerX, -centerY)
        matrix.postTranslate(centerX, centerY)
    }
}
