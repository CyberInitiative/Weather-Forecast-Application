package com.example.weather.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources

object WeatherColorAnimator {
    fun animateColorChange(view: View, color: Int, animationDuration: Long) {
        val currentColor = (view.background as? ColorDrawable)?.color ?: Color.WHITE
        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), currentColor, color)

        colorAnimator.addUpdateListener { animator ->
            view.setBackgroundColor(animator.animatedValue as Int)
        }

        colorAnimator.duration = animationDuration
        colorAnimator.start()
    }

    fun animateDrawableChange(view: View, drawableId: Int, animationDuration: Int) {
        val context = view.context
        val currentDrawable = view.background ?: AppCompatResources.getDrawable(context, android.R.color.transparent)
        val newDrawable =  AppCompatResources.getDrawable(context, drawableId)

        if (newDrawable != null) {
            val transitionDrawable = TransitionDrawable(arrayOf(currentDrawable, newDrawable))
            view.background = transitionDrawable

            transitionDrawable.startTransition(animationDuration)
        }
    }
}