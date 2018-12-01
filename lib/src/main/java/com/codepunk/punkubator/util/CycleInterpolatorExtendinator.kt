/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package com.codepunk.punkubator.util

import android.view.animation.CycleInterpolator

/**
 * Convenience method that converts an animatedFraction to an interpolation input (i.e. a
 * "time fraction". It's essentially the inverse of [CycleInterpolator.getInterpolation].
 */
fun cycleInterpolatorInverse(cycles: Float, animatedFraction: Float): Float =
    (Math.asin(animatedFraction.toDouble()) / (2 * cycles * Math.PI)).toFloat()
