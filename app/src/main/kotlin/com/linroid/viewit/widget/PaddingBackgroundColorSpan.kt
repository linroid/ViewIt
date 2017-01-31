package com.linroid.viewit.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.style.LineBackgroundSpan

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
class PaddingBackgroundColorSpan(private val backgroundColor: Int, private val padding: Int) : LineBackgroundSpan {
    private val paddingRect: Rect = Rect()

    override fun drawBackground(c: Canvas, paint: Paint,
                                left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
                                text: CharSequence, start: Int, end: Int, lnum: Int) {
        val textWidth = Math.round(paint.measureText(text, start, end))
        val paintColor = paint.color
        // Draw the background
        paddingRect.set(left - padding,
                top - if (lnum == 0) padding / 2 else -(padding / 2),
                left + textWidth + padding,
                bottom + padding / 2)
        paint.color = backgroundColor
        c.drawRect(paddingRect, paint)
        paint.color = paintColor
    }
}