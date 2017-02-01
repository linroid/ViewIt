package com.linroid.viewit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.utils.AndroidUtils
import com.linroid.viewit.utils.THUMBNAIL_MAX_COUNT
import timber.log.Timber
import java.util.*

/**
 * 4 个 ImageView 的缩略图
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class ThumbnailView : ViewGroup {
    val DEFAULT_SIZE = 88F;

    private var dividerSize = 0
    private val imageViews = ArrayList<ImageView>(THUMBNAIL_MAX_COUNT)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ThumbnailView)
        dividerSize = ta.getDimensionPixelSize(R.styleable.ThumbnailView_dividerSize, AndroidUtils.dp(1F).toInt())
        ta.recycle()
        for (i in 0 until THUMBNAIL_MAX_COUNT) {
            val view = ImageView(context)
            imageViews.add(view)
            addView(view)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY) {
            width = AndroidUtils.dp(DEFAULT_SIZE).toInt()
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            height = AndroidUtils.dp(DEFAULT_SIZE).toInt()
        }

        val childWidthSpec = MeasureSpec.makeMeasureSpec((width - dividerSize) / 2, MeasureSpec.EXACTLY)
        val childHeightSpec = MeasureSpec.makeMeasureSpec((height - dividerSize) / 2, MeasureSpec.EXACTLY)
        (0 until childCount)
                .map { getChildAt(it) }
                .forEach { it.measure(childWidthSpec, childHeightSpec) }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val perWidth = (r - l - dividerSize) / 2
        val perHeight = (b - t - dividerSize) / 2
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val left = if (i % 2 == 0) 0 else perWidth + dividerSize
            val top = if (i < 2) 0 else perHeight + dividerSize
            child.layout(left, top, left + perWidth, top + perHeight)
        }
    }

    fun clear() {
        (0 until childCount)
                .map { getChildAt(it) as ImageView }
                .forEach {
                    it.setImageBitmap(null)
                    it.setImageDrawable(null)
                    Glide.clear(it)
                }

    }

    fun setImages(images: List<Image>) {
        clear()
        val count = Math.min(images.size, THUMBNAIL_MAX_COUNT)
        for (i in 0 until count) {
            val child = getChildAt(i) as ImageView
            val perWidth = (measuredWidth - dividerSize) / 2
            val perHeight = (measuredHeight - dividerSize) / 2
            Glide.with(context)
                    .load(images[i].file())
                    .override(perWidth, perHeight)
                    .centerCrop()
                    .crossFade()
                    .into(child)
        }
    }
}