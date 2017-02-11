package com.linroid.viewit.ui.path

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.linroid.viewit.ui.BaseListActivity
import me.drakeet.multitype.MultiTypeAdapter
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class PathManagerActivity : BaseListActivity() {
    val items = LinkedList<Any>()
    val adapter = MultiTypeAdapter()

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    private fun initCategories() {
    }
}