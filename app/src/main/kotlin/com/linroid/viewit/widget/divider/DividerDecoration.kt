package com.linroid.viewit.widget.divider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.linroid.viewit.R

/**
 * @author linroid <linroid@gmail.com>
 * @since 12/02/2017
 */
class DividerDecoration : RecyclerView.ItemDecoration {

    var divider: Drawable? = null
    private var orientation: Int = 0

    constructor(context: Context, orientation: Int) {
        divider = ContextCompat.getDrawable(context, R.drawable.base_divider)
        this.orientation = orientation
    }

    constructor(context: Context, orientation: Int, @DrawableRes resId: Int) {
        divider = ContextCompat.getDrawable(context, resId) ?: ContextCompat.getDrawable(context, R.drawable.base_divider)
        this.orientation = orientation
    }

    constructor(context: Context, orientation: Int, drawable: Drawable?) {
        divider = drawable ?: ContextCompat.getDrawable(context, R.drawable.base_divider)
        this.orientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView) {
        if (orientation == VERTICAL_LIST) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            if (canDrawAt(parent, child)) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + params.bottomMargin + Math.round(ViewCompat.getTranslationY(child))
                val bottom = top + divider!!.intrinsicHeight
                divider!!.setBounds(left, top, right, bottom)
                divider!!.draw(c)
            }
        }
    }

    fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom

        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            if (canDrawAt(parent, child)) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                val left = child.right + params.rightMargin + Math.round(ViewCompat.getTranslationX(child))
                val right = left + divider!!.intrinsicHeight
                divider!!.setBounds(left, top, right, bottom)
                divider!!.draw(c)
            }
        }
    }

    private fun canDrawAt(recyclerView: RecyclerView, child: View): Boolean {
        return child.id != R.id.category_view
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (canDrawAt(parent, view)) {
            if (orientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, divider!!.intrinsicHeight)
            } else {
                outRect.set(0, 0, divider!!.intrinsicWidth, 0)
            }
        }
    }

    companion object {
        val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }
}