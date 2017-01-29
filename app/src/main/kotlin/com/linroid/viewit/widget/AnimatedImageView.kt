package com.linroid.viewit.widget

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.widget.ImageView

/**
 * @author linroid <linroid@gmail.com>
 * @since 28/01/2017
 */
class AnimatedImageView : ImageView, Animatable {

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
        val src = drawable
        if (src is AnimationDrawable) {
            return src.isRunning
        }
        return false
    }

    override fun start() {
        val src = drawable
        if (src is AnimationDrawable) {
            src.start()
        }
    }

    override fun stop() {
        val src = drawable
        if (src is AnimationDrawable) {
            src.stop()
        }
    }
}
