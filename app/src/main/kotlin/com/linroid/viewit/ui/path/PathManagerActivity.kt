package com.linroid.viewit.ui.path

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.model.CloudScanPath
import com.linroid.viewit.data.model.ScanPath
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.BaseListActivity
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.ui.path.provider.BuildInPath
import com.linroid.viewit.ui.path.provider.BuildInPathViewProvider
import com.linroid.viewit.ui.path.provider.CloudScanPathViewProvider
import com.linroid.viewit.ui.path.provider.ScanPathViewProvider
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.widget.divider.CategoryItemDecoration
import com.linroid.viewit.widget.divider.DividerDecoration
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class PathManagerActivity : BaseListActivity() {
    val items = Items()
    val adapter = MultiTypeAdapter(items)
    val buildInCategory = Category<BuildInPath>(null, adapter, items, App.get().getString(R.string.category_built_in_path))
    val cloudCategory = Category<CloudScanPath>(buildInCategory, adapter, items, App.get().getString(R.string.category_cloud_path))
    val localCategory = Category<ScanPath>(cloudCategory, adapter, items, App.get().getString(R.string.category_local_path))
    private lateinit var appInfo: ApplicationInfo

    companion object {
        fun navTo(source: BaseActivity, info: ApplicationInfo) {
            val intent = Intent(source, PathManagerActivity::class.java);
            intent.putExtra(ARG_APP_INFO, info)
            source.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appInfo = intent.getParcelableExtra(ARG_APP_INFO)
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter.register(Category::class.java, CategoryViewProvider())
        adapter.register(BuildInPath::class.java, BuildInPathViewProvider())
        adapter.register(CloudScanPath::class.java, CloudScanPathViewProvider())
        adapter.register(ScanPath::class.java, ScanPathViewProvider())
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(CategoryItemDecoration(recyclerView))
        recyclerView.addItemDecoration(DividerDecoration(recyclerView.context, DividerDecoration.VERTICAL_LIST))
        initCategories()
    }

    private fun initCategories() {
        buildInCategory.items = ArrayList<BuildInPath>().apply {
            add(BuildInPath("内部存储"))
            add(BuildInPath("外部存储"))
        }
        val demoPath = "/sdcard/Tencent/MicroMsg"
        cloudCategory.items = ArrayList<CloudScanPath>().apply {
            add(CloudScanPath(ScanPath().apply {
                path = demoPath
            }.toAVObject("CloudScanPath")).apply {
                path = demoPath
            })
            add(CloudScanPath(ScanPath().apply {
                path = demoPath
            }.toAVObject("CloudScanPath")).apply {
                path = demoPath
            })
        }
        localCategory.items = ArrayList<ScanPath>().apply {
            add(ScanPath().apply {
                path = demoPath
            })
            add(ScanPath().apply {
                path = demoPath
            })
        }
    }
}