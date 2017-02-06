package com.linroid.viewit.ui.gallery.provider

import android.view.View
import timber.log.Timber
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
class Category(
        private val prev: Category?,
        var label: CharSequence,
        private var listItems: MutableList<Any>,
        var action: CharSequence? = null,
        var actionClickListener: View.OnClickListener? = null) {

    private var itemCount = 0

    var position: Int by Delegates.observable(-1) { prop: KProperty<*>, old: Int, new: Int ->
        if (next != null) {
            updateNext()
        }
    }

    private fun updateNext() {
        if (next != null) {
            next!!.position = position + displayedCount(items)
        }
    }

    fun displayedCount(_items: List<Any>?) = if (_items == null || _items.isEmpty()) 0 else _items.size + 1


    var items: List<Any>? by Delegates.observable(null) { prop: KProperty<*>, old: List<Any>?, new: List<Any>? ->
        val oldCount = itemCount
        val newCount = new?.size ?: 0
        val oldEnd = position + displayedCount(old)
        if (oldCount > 0) {
            Timber.w("remove items: ${position + 1}..$oldEnd, total:${listItems.size}")
            listItems.removeAll(listItems.subList(position + 1, oldEnd + 1))
        }
        if (newCount > 0) {
            Timber.w("add category[$label] at:${position + 1}")
            listItems.add(position + 1, this)
            listItems.addAll(position + 2, new!!)
        }
        itemCount = newCount
        updateNext()
    }

    var next: Category? = null

    init {
        if (prev != null) {
            prev.next = this
            prev.updateNext()
        }
        if (prev == null) {
            position = -1
        }
    }
}