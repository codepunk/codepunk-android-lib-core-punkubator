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

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.TextureView
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import com.codepunk.punkubator.R

/**
 * An implementation of [TextureView] that incorporates a [scaleType] property for scaling
 * content similar to [ImageView].
 */
open class TextureViewEx : TextureView {

    // region Properties

    /**
     * Controls how the content should be resized or moved to match the size of this TextureViewEx.
     */
    @Suppress("WEAKER_ACCESS")
    var scaleType: ScaleType = ScaleType.FIT_XY
        set(value) {
            field = value
            updateTransform()
        }

    /**
     * A gravity value that potentially overrides the placement resulting from [scaleType].
     */
    var gravity: Int = Gravity.NO_GRAVITY
        set(value) {
            field = value
            updateTransform()
        }

    /**
     * A [Rect] holding the current content size. You can set this via [setContentSize].
     */
    @Suppress("WEAKER_ACCESS")
    protected val contentSize = Rect()

    /**
     * A reusable bucket for storing the transform matrix.
     */
    private val transform = Matrix()

    /**
     * A reusable bucket for storing the container when calling [Gravity.apply]
     */
    private val container = Rect()

    /**
     * A reusable bucket for storing the outRect when calling [Gravity.apply]
     */
    private val outRect = Rect()

    // endregion Properties

    // region Constructors

    constructor(context: Context?) : super(context) {
        initTextureViewEx(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initTextureViewEx(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initTextureViewEx(context, attrs, defStyleAttr)
    }

    @Suppress("UNUSED")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initTextureViewEx(context, attrs, defStyleAttr, defStyleRes)
    }

    // endregion Constructors

    // region Inherited methods

    /**
     * Updates the transform matrix.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTransform()
    }

    /**
     * Updates the transform matrix.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateTransform()
    }

    // endregion Inherited methods

    // region Methods

    /**
     * Initializes the TextureViewEx.
     */
    private fun initTextureViewEx(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.textureViewExStyle,
        defStyleRes: Int = R.style.TextureViewEx
    ) {
        val a = context?.obtainStyledAttributes(
            attrs,
            R.styleable.TextureViewEx,
            defStyleAttr,
            defStyleRes
        )

        if (a != null) {
            scaleType = ScaleType.values()[a.getInt(
                R.styleable.TextureViewEx_android_scaleType,
                ScaleType.FIT_XY.ordinal
            )]

            gravity = a.getInt(R.styleable.TextureViewEx_android_gravity, Gravity.NO_GRAVITY)

            a.recycle()
        }
    }

    /**
     * Sets the size of the content.
     */
    fun setContentSize(width: Int, height: Int) {
        contentSize.set(0, 0, width, height)
        updateTransform()
    }

    /**
     * Updates the transform matrix to size the content according to [scaleType].
     */
    protected open fun updateTransform() {
        if (!ViewCompat.isAttachedToWindow(this)
            || width == 0
            || height == 0
            || contentSize.isEmpty
        ) {
            return
        }

        transform.reset()

        val vw: Int = contentSize.width()
        val vh: Int = contentSize.height()

        val sx: Float
        val sy: Float
        val dx: Float
        val dy: Float
        when (scaleType) {
            ScaleType.FIT_XY, ScaleType.MATRIX -> {
                // TODO Process MATRIX. For now defaults to identity matrix.
                sx = 1.0f
                sy = 1.0f
                dx = 0.0f
                dy = 0.0f
            }
            else -> {
                val ratioX = vw.toFloat() / width
                val ratioY = vh.toFloat() / height
                val factor: Float = when (scaleType) {
                    ScaleType.CENTER_CROP -> Math.max(1 / ratioX, 1 / ratioY)
                    ScaleType.CENTER_INSIDE -> Math.max(Math.min(1 / ratioX, 1 / ratioY), 1.0f)
                    ScaleType.FIT_CENTER, ScaleType.FIT_END, ScaleType.FIT_START ->
                        Math.min(1 / ratioX, 1 / ratioY)
                    else -> 1.0f
                }

                sx = ratioX * factor
                sy = ratioY * factor

                val gr = when {
                    gravity != Gravity.NO_GRAVITY -> gravity
                    scaleType == ScaleType.FIT_START -> Gravity.START or Gravity.TOP
                    scaleType == ScaleType.FIT_END -> Gravity.END or Gravity.BOTTOM
                    else -> Gravity.CENTER
                }
                container.set(0, 0, width, height)
                GravityCompat.apply(
                    gr,
                    (width * sx).toInt(),
                    (height * sy).toInt(),
                    container,
                    outRect,
                    ViewCompat.getLayoutDirection(this)
                )
                dx = outRect.left.toFloat()
                dy = outRect.top.toFloat()
            }
        }

        transform.setScale(sx, sy)
        transform.postTranslate(dx, dy)
        setTransform(transform)
    }

    // endregion Methods

}
