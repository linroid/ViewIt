package com.linroid.viewit.widget

import android.content.Context
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.widget.ImageView

/**
 * @author linroid <linroid@gmail.com>
 * @since 28/01/2017
 */
class AnimatedScrollView : ImageView, Animatable {

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

    override fun isRunning(): Boolean {
        return true
    }

    override fun start() {
    }

    override fun stop() {
    }
}