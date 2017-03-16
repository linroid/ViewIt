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
        var actionClickListener: View.OnClickListener? = null,
        val alwaysShow: Boolean = false  // 不管 items.size 是否大于0都显示
) {

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
        val begin = position + 1
        if (oldCount > 0 || alwaysShow) {
            val oldEnd = Math.min(begin + oldCount + 1, listItems.size);
            if (oldEnd >= begin) {
                Timber.d("remove items: $begin..$oldEnd, total:${listItems.size}")
                listItems.subList(begin, oldEnd).clear()
                Timber.d("after removed, total:${listItems.size}")
                adapter.notifyItemRangeRemoved(begin, oldCount + 1)
            }
        }
        if (newCount > 0 || alwaysShow) {
            Timber.d("add category[$label] at:$begin with $newCount items")
            listItems.add(begin, this)
            listItems.addAll(begin + 1, newVal!!)
            adapter.notifyItemRangeInserted(begin, newCount + 1)
        }
        itemCount = newCount
        updateNext()
        listeners.forEach {
            it.onChanged(oldVal, newVal)
        }
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

    private var listeners = HashSet<OnItemsChangedListener<T>>()

    fun registerChangedListener(listener: OnItemsChangedListener<T>) {
        listeners.add(listener)
    }

    fun unregisterChangedListener(listener: OnItemsChangedListener<T>) {
        listItems.remove(listener)
    }

    interface OnItemsChangedListener<T> {
        fun onChanged(oldVal: List<T>?, newVal: List<T>?)
    }
}