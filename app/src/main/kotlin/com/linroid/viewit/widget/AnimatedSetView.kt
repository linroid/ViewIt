package com.linroid.viewit.widget

import android.content.Context
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * @author linroid <linroid@gmail.com>
 * @since 28/01/2017
 */
class AnimatedSetView : FrameLayout, Animatable {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if ((child is Animatable).not()) {
            throw IllegalArgumentException("not support none Animatable View")
        }
        super.addView(child, index, params)
    }

    override fun isRunning(): Boolean {
        return (0..childCount-1)
                .map { getChildAt(it) as Animatable }
                .any { it.isRunning }
    }

    override fun start() {
        (0..childCount-1)
                .map {
                    getChildAt(it) as Animatable
                }
                .forEach { it.start() }
    }

    override fun stop() {
        (0..childCount-1)
                .map { getChildAt(it) as Animatable }
                .forEach { it.stop() }
    }
}