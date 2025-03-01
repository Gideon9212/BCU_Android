package com.g2.bcu.androidutil.supports

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

@SuppressLint("Recycle")
class AlphaAnimator(target: View, duration: Int, animator: AnimatorConst.Accelerator, from: Float, to: Float) : ValueAnimator() {
    init {
        setFloatValues(filter(from),filter(to))
        addUpdateListener { animation ->
            val v = animation.animatedValue as Float
            target.alpha = v
        }
        this.duration = duration.toLong()

        interpolator = when(animator) {
            AnimatorConst.Accelerator.DECELERATE -> DecelerateInterpolator()
            AnimatorConst.Accelerator.ACCELERATE -> AccelerateInterpolator()
            AnimatorConst.Accelerator.ACCELDECEL -> AccelerateDecelerateInterpolator()
        }
    }

    private fun filter(alpha: Float) : Float {
        var res = alpha

        if(alpha > 1f)
            res = 1f
        else if(alpha < 0f)
            res = 0f

        return res
    }
}