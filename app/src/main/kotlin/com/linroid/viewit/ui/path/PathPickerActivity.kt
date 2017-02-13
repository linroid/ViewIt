package com.linroid.viewit.ui.path

import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.linroid.viewit.ui.BaseListActivity
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import java.io.File
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 13/02/2017
 */
class PathPickerActivity : BaseListActivity() {
    val items = Items()
    val adapter = MultiTypeAdapter(items)
    val stack = Stack<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sdcardDir = Environment.getExternalStorageDirectory()
        loadFile(sdcardDir)
    }

    private fun loadFile(sdcardDir: File) {

    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}