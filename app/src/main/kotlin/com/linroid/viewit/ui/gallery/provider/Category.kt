package com.linroid.viewit.ui.gallery.provider

import android.view.View
import me.drakeet.multitype.MultiTypeAdapter
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
open class Category<T : Any>(
        prev: Category<*>?,
        private val adapter: MultiTypeAdapter,
        private var listItems: ArrayList<Any>,
        var label: CharSequence,
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

    fun invalidate() {
        adapter.notifyItemRangeChanged(position + 1, displayedCount(items))
    }

    private fun displayedCount(_items: List<T>?) = if (_items == null || _items.isEmpty()) 0 else _items.size + 1

    var items: List<T>? by Delegates.observable(null) { prop: KProperty<*>, oldVal: List<T>?, newVal: List<T>? ->
        Timber.d("update category[$label] items(size=${newVal?.size ?: 0})")
        val oldCount = itemCount
        val newCount = newVal?.size ?: 0
        if (oldCount > 0) {
            val oldEnd = position + (oldCount + 1)
            Timber.w("remove items: ${position + 1}..${oldEnd + 1}, total:${listItems.size}")
            listItems.subList(position + 1, oldEnd + 1).clear()
            adapter.notifyItemRangeRemoved(position + 1, oldCount + 1)
        }
        if (newCount > 0) {
            Timber.w("add category[$label] at:${position + 1} with $newCount items")
            listItems.add(position + 1, this)
            listItems.addAll(position + 2, newVal!!)
            adapter.notifyItemRangeInserted(position + 1, newCount + 1)
        }
        itemCount = newCount
        updateNext()
    }

    var next: Category<*>? = null

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