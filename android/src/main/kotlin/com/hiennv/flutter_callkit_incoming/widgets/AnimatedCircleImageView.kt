package com.hiennv.flutter_callkit_incoming.widgets

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Animatable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.hiennv.flutter_callkit_incoming.R

class AnimatedCircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var borderWidth: Float = 0f
    private var borderColor: Int = Color.WHITE
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()
    private var radius: Float = 0f

    init {
        // Parse custom attributes
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.AnimatedCircleImageView,
                defStyleAttr,
                0
            )

            borderWidth = typedArray.getDimension(
                R.styleable.AnimatedCircleImageView_aciv_border_width,
                0f
            )

            borderColor = typedArray.getColor(
                R.styleable.AnimatedCircleImageView_aciv_border_color,
                Color.WHITE
            )

            typedArray.recycle()
        }

        // Configure border paint
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calculate radius and update clip path when size changes
        val minSize = Math.min(w, h)
        radius = if (minSize > borderWidth) {
            (minSize / 2f) - (borderWidth / 2f)
        } else {
            minSize / 2f
        }
        updateClipPath()
    }

    private fun updateClipPath() {
        if (width <= 0 || height <= 0) return

        clipPath.reset()
        val centerX = width / 2f
        val centerY = height / 2f
        if (radius > 0) {
            clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable == null) {
            super.onDraw(canvas)
            return
        }

        // Save canvas state
        val saveCount = canvas.save()

        // Apply circular clipping
        canvas.clipPath(clipPath)

        // Draw the drawable (animated or static)
        super.onDraw(canvas)

        // Restore canvas state
        canvas.restoreToCount(saveCount)

        // Draw border on top
        if (borderWidth > 0) {
            val centerX = width / 2f
            val centerY = height / 2f
            canvas.drawCircle(centerX, centerY, radius, borderPaint)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        // Stop previous animation if it exists
        stopAnimation()

        super.setImageDrawable(drawable)

        // Start animation if view is already attached
        if (isAttachedToWindow) {
            startAnimation()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: android.view.View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility == android.view.View.VISIBLE) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    private fun startAnimation() {
        val drawable = drawable ?: return

        // Handle AnimatedImageDrawable (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
            if (!drawable.isRunning) {
                drawable.start()
            }
            return
        }

        // Handle any Animatable drawable (works for Coil's MovieDrawable on older APIs)
        if (drawable is Animatable && !drawable.isRunning) {
            drawable.start()
        }
    }

    private fun stopAnimation() {
        val drawable = drawable ?: return

        // Handle AnimatedImageDrawable (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
            if (drawable.isRunning) {
                drawable.stop()
            }
            return
        }

        // Handle any Animatable drawable (works for Coil's MovieDrawable on older APIs)
        if (drawable is Animatable && drawable.isRunning) {
            drawable.stop()
        }
    }

    override fun invalidateDrawable(who: Drawable) {
        // Trigger redraw for each animation frame
        super.invalidateDrawable(who)
        invalidate()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        super.scheduleDrawable(who, what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        super.unscheduleDrawable(who, what)
    }

    // Public methods to control border appearance
    fun setBorderWidth(width: Float) {
        if (borderWidth != width) {
            borderWidth = width
            borderPaint.strokeWidth = width
            updateClipPath()
            invalidate()
        }
    }

    fun setBorderColor(color: Int) {
        if (borderColor != color) {
            borderColor = color
            borderPaint.color = color
            invalidate()
        }
    }
}
