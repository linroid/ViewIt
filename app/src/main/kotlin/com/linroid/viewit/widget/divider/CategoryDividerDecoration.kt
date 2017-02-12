package com.linroid.viewit.widget.divider

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.linroid.viewit.R
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 12/02/2017
 */
class CategoryDividerDecoration : RecyclerView.ItemDecoration {
    var divider: Drawable? = null

    constructor(recyclerView: RecyclerView) : super() {
        init(recyclerView, ContextCompat.getDrawable(recyclerView.context, R.drawable.bg_category_divider))
    }

    constructor(recyclerView: RecyclerView, divider: Drawable) : super() {
        init(recyclerView, divider)
    }

    private fun init(recyclerView: RecyclerView, divider: Drawable) {
        this.divider = divider
        recyclerView.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                recyclerView.invalidateItemDecorations()
            }
        })
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            if (canDrawAt(parent, child)) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                val bottom = child.top - params.topMargin + Math.round(ViewCompat.getTranslationY(child))
                val top = bottom - divider!!.intrinsicHeight
                divider!!.setBounds(left, top, right, bottom)
                divider!!.draw(c)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (canDrawAt(parent, view)) {
            outRect.set(0, divider!!.intrinsicHeight, 0, 0)
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }

    private fun canDrawAt(parent: RecyclerView, view: View): Boolean {
        val viewHolder = parent.findViewHolderForLayoutPosition(parent.getChildAdapterPosition(view))
        if (divider != null && viewHolder != null
                && viewHolder is CategoryViewProvider.ViewHolder && viewHolder.adapterPosition > 0) {
            return true
        }
        return false

    }
}