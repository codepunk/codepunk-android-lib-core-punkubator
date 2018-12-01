/*
 * Copyright (C) 2018 Codepunk, LLC
 * Author(s): Scott Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codepunk.punkubator.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import android.view.TextureView
import android.view.animation.CycleInterpolator
import com.codepunk.punkubator.R
import com.codepunk.punkubator.util.cycleInterpolatorInverse
import com.codepunk.punkubator.widget.TextureViewPanner.Builder
import com.codepunk.punkubator.widget.TextureViewPanner.Style
import java.util.*

/**
 * A helper class designed to automatically pan the contents of a [TextureView] back and forth
 * when those contents do not fit well in the current aspect ratio. For example, a video file
 * designed in portrait mode may not look good presented in landscape mode. This allows the
 * video to be presented, for example, in CENTER_CROP scale type and will automatically pan the
 * video back and forth. Panning in both dimensions is possible as well, by supplying the
 * appropriate panningStyle(s) to the [Builder].
 */
class TextureViewPanner private constructor(

    /**
     * The [TextureView] that will be controlled by this panner.
     */
    private val textureView: TextureView,

    /**
     * A map of panning attributes mapped to a [Style].
     */
    styleMap: Map<Style, PanningAttributes>

) {

    // region Properties

    /**
     * A map of panning style information that controls animation delay, duration and
     * threshold (that is, how much larger the content needs to be before panning kicks in).
     */
    private val styleMap: HashMap<Style, PanningAttributes> = HashMap()

    /**
     * A [Runnable] that updates the [TextureView]'s transform matrix to the values currently
     * stored in the [values] array.
     */
    private val runnable: Runnable = Runnable {
        transform.setValues(values)
        textureView.setTransform(transform)
    }

    /**
     * An set of animators that pan the content in the [TextureView]. Normally we would wrap
     * these in an [AnimatorSet] but we want to control the currentPlayTime of each individual
     * animator so we'll keep them separate.
     */
    private val animators = HashSet<Animator>()

    /**
     * A reusable bucket for storing transform information without constantly creating new matrices
     * in [update].
     */
    private val transform: Matrix = Matrix()

    /**
     * A reusable bucked for storing transform values without constant creating new FloatArrays.
     */
    private val values: FloatArray = FloatArray(9)

    /**
     * The last transform scale that was processed by this panner. When the scale changes,
     * any animations will be stopped and potentially recreated depending on the new scale.
     */
    private val lastProcessedScale: PointF = PointF(1.0f, 1.0f)

    // endregion Properties

    // region Constructors

    init {
        this.styleMap.putAll(styleMap)
    }

    // endregion Constructors

    // region Methods

    /**
     * Invalidates the latest transform information and posts the runnable that updates the
     * transform matrix.
     */
    @Suppress("WEAKER_ACCESS")
    fun invalidateTransform() {
        textureView.removeCallbacks(runnable)
        textureView.post(runnable)
    }

    /**
     * Ends and clears animations.
     */
    fun release() {
        for (animator in animators) {
            animator.end()
        }
        animators.clear()
    }

    /**
     * Potentially kicks off new panning animation(s) based on the transform matrix.
     */
    fun update() {
        textureView.getTransform(transform)
        transform.getValues(values)
        val sx = values[Matrix.MSCALE_X]
        val sy = values[Matrix.MSCALE_Y]
        if (sx == lastProcessedScale.x && sy == lastProcessedScale.y) {
            return
        }

        lastProcessedScale.set(sx, sy)

        release()

        // Resolve style attributes
        val hAttributes: PanningAttributes?
        val vAttributes: PanningAttributes?
        when {
            styleMap.containsKey(Style.HORIZONTAL) || styleMap.containsKey(Style.VERTICAL) -> {
                hAttributes = styleMap[Style.HORIZONTAL]
                vAttributes = styleMap[Style.VERTICAL]
            }
            sx >= sy -> {
                hAttributes = styleMap[Style.PRIMARY]
                vAttributes = styleMap[Style.SECONDARY]
            }
            else -> {
                vAttributes = styleMap[Style.PRIMARY]
                hAttributes = styleMap[Style.SECONDARY]
            }
        }

        val animators: ArrayList<ValueAnimator> = ArrayList()
        if (hAttributes != null && sx > hAttributes.threshold) {
            val hAnimator = createAnimator(hAttributes)

            val minDx = textureView.width * (1 - sx)
            val maxDx = 0.0f
            val midDx = (minDx + maxDx) / 2.0f
            val animatedFraction = (values[Matrix.MTRANS_X] - midDx) / (maxDx - midDx)
            val timeFraction = cycleInterpolatorInverse(1.0f, animatedFraction)
            val currentPlayTime = (hAnimator.duration * timeFraction).toLong()
            hAnimator.currentPlayTime = when {
                currentPlayTime >= 0 -> currentPlayTime
                else -> hAnimator.duration + currentPlayTime
            }

            hAnimator.addUpdateListener {
                val pct = (it.animatedFraction - LOW_VALUE) / (TO_VALUE - LOW_VALUE)
                values[Matrix.MTRANS_X] = minDx + (maxDx - minDx) * pct
                invalidateTransform()
            }
            animators.add(hAnimator)
        }

        if (vAttributes != null && sy > vAttributes.threshold) {
            val vAnimator = createAnimator(vAttributes)

            val minDy = textureView.height * (1 - sy)
            val maxDy = 0.0f
            val midDy = (minDy + maxDy) / 2.0f
            val animatedFraction = (values[Matrix.MTRANS_Y] - midDy) / (maxDy - midDy)
            val timeFraction = cycleInterpolatorInverse(1.0f, animatedFraction)
            val currentPlayTime = (vAnimator.duration * timeFraction).toLong()
            vAnimator.currentPlayTime = when {
                currentPlayTime >= 0 -> currentPlayTime
                else -> vAnimator.duration + currentPlayTime
            }

            vAnimator.addUpdateListener {
                val pct = (it.animatedFraction - LOW_VALUE) / (TO_VALUE - LOW_VALUE)
                values[Matrix.MTRANS_Y] = minDy + (maxDy - minDy) * pct
                invalidateTransform()
            }
            animators.add(vAnimator)
        }

        animators.forEach { it.reverse() }
    }

    // endregion methods

    // region Companion object

    companion object {

        /**
         * The "from value" for the [ValueAnimator].
         */
        private const val FROM_VALUE: Float = 0.0f

        /**
         * The "to value" for the [ValueAnimator].
         */
        private const val TO_VALUE: Float = 1.0f

        /**
         * The low value produced by the [CycleInterpolator], which is always equal to -TO_VALUE
         * but provided here for convenience.
         */
        private const val LOW_VALUE: Float = -TO_VALUE

        /**
         * Creates a [ValueAnimator] that cycles between two values.
         */
        @JvmStatic
        private fun createAnimator(attributes: PanningAttributes): ValueAnimator =
            ValueAnimator.ofFloat(FROM_VALUE, TO_VALUE).apply {
                startDelay = attributes.startDelay
                duration = attributes.duration
                interpolator = CycleInterpolator(1.0f)
                repeatCount = ValueAnimator.INFINITE
            }

    }

    // endregion Companion object

    // region Nested/inner classes

    /**
     * An enum value that describes the type of panning information we are adding.
     */
    enum class Style {

        /**
         * Horizontal panning style. When this style is added via the [Builder], then any
         * previously-added [PRIMARY] and [SECONDARY] information will be ignored.
         */
        HORIZONTAL,

        /**
         * Vertical panning style. When this style is added via the [Builder], then any
         * previously-added [PRIMARY] and [SECONDARY] information will be ignored.
         */
        VERTICAL,

        /**
         * Primary panning style. When this style is added via the [Builder] (and [HORIZONTAL]/
         * [VERTICAL] are not added), then this will describe panning along the primary axis. The
         * primary axis is the direction in which more of the [TextureView]'s contents are hidden
         * offscreen.
         */
        PRIMARY,

        /**
         * Secondary panning style. When this style is added via the [Builder] (and [HORIZONTAL]/
         * [VERTICAL] are not added), then this will describe panning along the primary axis. The
         * primary axis is the direction in which less of the [TextureView]'s contents are hidden
         * offscreen.
         */
        SECONDARY
    }

    /**
     * Convenience class for building [TextureViewPanner].
     */
    class Builder(private val textureView: TextureView) {

        /**
         * A map of panning attributes mapped to a [Style].
         */
        private val styleMap: HashMap<Style, PanningAttributes> = HashMap(Style.values().size)

        /**
         * Adds a set of panning attributes associated with the supplied [style].
         */
        @Suppress("WEAKER_ACCESS")
        fun panningStyle(style: Style, delay: Long, duration: Long, threshold: Float): Builder {
            styleMap[style] = PanningAttributes(delay, duration, threshold)
            return this
        }

        /**
         * Builds the [TextureViewPanner].
         */
        fun build(): TextureViewPanner {
            if (styleMap.isEmpty()) {
                // Create default styles if none supplied
                panningStyle(
                    Style.PRIMARY,
                    textureView.resources.getInteger(
                        R.integer.texture_view_default_primary_pan_delay
                    ).toLong(),
                    textureView.resources.getInteger(
                        R.integer.texture_view_default_primary_pan_duration
                    ).toLong(),
                    textureView.resources.getFraction(
                        R.fraction.texture_view_default_pan_threshold, 1, 1
                    )
                )
                panningStyle(
                    Style.SECONDARY,
                    textureView.resources.getInteger(
                        R.integer.texture_view_default_secondary_pan_delay
                    ).toLong(),
                    textureView.resources.getInteger(
                        R.integer.texture_view_default_secondary_pan_duration
                    ).toLong(),
                    textureView.resources.getFraction(
                        R.fraction.texture_view_default_pan_threshold, 1, 1
                    )
                )
            }
            return TextureViewPanner(textureView, styleMap)
        }

    }

    /**
     * A class containing panning attributes.
     */
    private class PanningAttributes(
        val startDelay: Long,
        val duration: Long,
        val threshold: Float
    )

    // endregion Nested/inner classes

}
