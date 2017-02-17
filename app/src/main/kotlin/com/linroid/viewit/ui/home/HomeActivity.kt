package com.linroid.viewit.ui.home

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.repo.AppRepo
import com.linroid.viewit.data.repo.local.AppUsageRepo
import com.linroid.viewit.ui.BaseListActivity
import com.linroid.viewit.ui.about.AboutActivity
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.widget.divider.CategoryItemDecoration
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class HomeActivity : BaseListActivity() {
    val SPAN_COUNT = 4;

    val items = Items()
    val adapter: MultiTypeAdapter = MultiTypeAdapter(items)

    lateinit var topUsageCategory: Category<ApplicationInfo>
    lateinit var otherAppCategory: Category<ApplicationInfo>

    @Inject
    lateinit var appRepo: AppRepo
    @Inject
    lateinit var usageRepo: AppUsageRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        App.graph.inject(this)
        super.onCreate(savedInstanceState)
        loadApps();
    }

    private fun loadApps() {
//        appRepo.list(8)
//                .bindToLifecycle(this)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ apps ->
//                    items.clear()
//                    items.addAll(apps)
//                    adapter.notifyItemInserted(apps.size - 1);
//                }, { error ->
//                    Timber.e(error, "onError")
//                }, {
//                    Timber.d("onCompleted")
//                })
        appRepo.list(SPAN_COUNT * 2).bindToLifecycle(this).subscribe {
            if (it.key) { // top
                it.bindToLifecycle(this).toList().subscribe { topApps ->
                    topUsageCategory.items = topApps
                }
            } else {
                it.bindToLifecycle(this).toList().subscribe { otherApps ->
                    otherAppCategory.items = otherApps
                }
            }
        }
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter.register(ApplicationInfo::class.java, AppViewProvider(this, usageRepo))
        adapter.register(Category::class.java, CategoryViewProvider())

        topUsageCategory = Category<ApplicationInfo>(null, adapter, items, getString(R.string.category_top_usage_app))
        otherAppCategory = Category<ApplicationInfo>(null, adapter, items, getString(R.string.category_other_app))

        val gridLayoutManager = GridLayoutManager(this, SPAN_COUNT);
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (items[position] is Category<*>) SPAN_COUNT else 1
            }
        }

        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(CategoryItemDecoration(recyclerView))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                AboutActivity.navTo(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
