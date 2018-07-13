package com.codepunk.codepunklibstaging.animation

import android.animation.TimeInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.Interpolator

class ShakeInterpolator(
        frequency: Int = 3,
        private val amplitudeInterpolator: Interpolator? = CycleInterpolator(0.5f)):
        TimeInterpolator {

    private val cycleInterpolator = CycleInterpolator(frequency.toFloat())

    override fun getInterpolation(input: Float): Float {
        return cycleInterpolator.getInterpolation(input) *
                (amplitudeInterpolator?.getInterpolation(input) ?: 1.0f)
    }
}