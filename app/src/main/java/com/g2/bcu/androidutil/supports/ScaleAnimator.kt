package com.g2.bcu.androidutil.supports

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

@SuppressLint("Recycle")
class ScaleAnimator(target: View, mode: AnimatorConst.Dimension, duration: Int, animator: AnimatorConst.Accelerator, from: Int, to: Int) : ValueAnimator() {

    init {
        setIntValues(from,to)
        addUpdateListener { animation ->
            val v = animation.animatedValue as Int
            val layout = target.layoutParams as MarginLayoutParams

            when(mode) {
                AnimatorConst.Dimension.HEIGHT -> layout.height = v
                AnimatorConst.Dimension.WIDTH -> layout.width = v
                AnimatorConst.Dimension.TOP_MARGIN -> layout.topMargin = v
            }

            target.layoutParams = layout
        }
        this.duration = duration.toLong()

        interpolator = when(animator) {
            AnimatorConst.Accelerator.DECELERATE -> DecelerateInterpolator()
            AnimatorConst.Accelerator.ACCELERATE -> AccelerateInterpolator()
            AnimatorConst.Accelerator.ACCELDECEL -> AccelerateDecelerateInterpolator()
        }
    }
}