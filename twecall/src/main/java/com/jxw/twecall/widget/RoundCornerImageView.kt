package com.jxw.twecall.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.jxw.twecall.R
/**
 *
 * @author: jyq
 * @desc: 圆角ImageView
 * @date: 2024/11/28
 */
class RoundCornerImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var leftTopRadius = 0
    private var rightTopRadius = 0
    private var rightBottomRadius = 0
    private var leftBottomRadius = 0
    private val path = Path()
    private val rectF = RectF()
    private var radius = 0
    private val aliasFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        val defaultRadius = 0
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView)
            radius = array.getDimensionPixelOffset(R.styleable.RoundCornerImageView_corner_radius, defaultRadius)
            leftTopRadius =
                array.getDimensionPixelOffset(R.styleable.RoundCornerImageView_left_top_radius, defaultRadius)
            rightTopRadius =
                array.getDimensionPixelOffset(R.styleable.RoundCornerImageView_right_top_radius, defaultRadius)
            rightBottomRadius =
                array.getDimensionPixelOffset(R.styleable.RoundCornerImageView_right_bottom_radius, defaultRadius)
            leftBottomRadius =
                array.getDimensionPixelOffset(R.styleable.RoundCornerImageView_left_bottom_radius, defaultRadius)
            array.recycle()
        }
        if (defaultRadius == leftTopRadius) {
            leftTopRadius = radius
        }
        if (defaultRadius == rightTopRadius) {
            rightTopRadius = radius
        }
        if (defaultRadius == rightBottomRadius) {
            rightBottomRadius = radius
        }
        if (defaultRadius == leftBottomRadius) {
            leftBottomRadius = radius
        }
    }

    fun setRadius(radius: Int) {
        this.radius = radius
        leftBottomRadius = radius
        rightBottomRadius = radius
        rightTopRadius = radius
        leftTopRadius = radius
    }

    fun getRadius(): Int {
        return radius
    }

    override fun onDraw(canvas: Canvas) {
        path.reset()
        canvas.drawFilter = aliasFilter
        rectF[0f, 0f, measuredWidth.toFloat()] = measuredHeight.toFloat()
        val radius = floatArrayOf(
            leftTopRadius.toFloat(),
            leftTopRadius.toFloat(),
            rightTopRadius.toFloat(),
            rightTopRadius.toFloat(),
            rightBottomRadius.toFloat(),
            rightBottomRadius.toFloat(),
            leftBottomRadius.toFloat(),
            leftBottomRadius.toFloat()
        )
        path.addRoundRect(rectF, radius, Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}