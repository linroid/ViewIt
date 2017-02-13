package com.linroid.viewit.widget.divider

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.linroid.viewit.R
import com.linroid.viewit.utils.AndroidUtils

/**
 * @author linroid <linroid@gmail.com>
 * @since 12/02/2017
 */
class CategoryItemDecoration : RecyclerView.ItemDecoration {
    var drawable: Drawable? = null
    var offset = 0 // 偏移量，覆盖住分割线

    constructor(recyclerView: RecyclerView) : super() {
        init(recyclerView, ContextCompat.getDrawable(recyclerView.context, R.drawable.bg_category_divider))
    }

    constructor(recyclerView: RecyclerView, divider: Drawable) : super() {
        init(recyclerView, divider)
    }

    private fun init(recyclerView: RecyclerView, divider: Drawable) {
        this.drawable = divider
        this.offset = -AndroidUtils.dp(1F).toInt()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            if (canDrawAt(parent, child)) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                val bottom = child.top - params.topMargin + Math.round(ViewCompat.getTranslationY(child)) + offset
                val top = bottom - drawable!!.intrinsicHeight
                drawable!!.setBounds(left, top, right, bottom)
                drawable!!.draw(c)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (canDrawAt(parent, view)) {
            outRect.set(0, drawable!!.intrinsicHeight + offset, 0, 0)
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }

    private fun canDrawAt(parent: RecyclerView, view: View): Boolean {
//        val viewHolder = parent.findViewHolderForLayoutPosition(parent.getChildAdapterPosition(view))
        if (drawable != null && view.id == R.id.category_view && parent.getChildAdapterPosition(view) > 0) {
            return true
        }
        return false

    }
}