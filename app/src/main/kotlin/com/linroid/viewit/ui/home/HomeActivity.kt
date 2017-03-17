package com.linroid.viewit.ui.home

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.avos.avoscloud.AVAnalytics
import com.avos.avoscloud.feedback.FeedbackAgent
import com.jakewharton.rxbinding.support.v7.widget.RxSearchView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.repo.AppRepo
import com.linroid.viewit.data.repo.local.AppUsageRepo
import com.linroid.viewit.ui.BaseListActivity
import com.linroid.viewit.ui.about.AboutActivity
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.utils.EVENT_CLICK_ABOUT
import com.linroid.viewit.utils.EVENT_LIST_APP
import com.linroid.viewit.utils.onMain
import com.linroid.viewit.widget.divider.CategoryItemDecoration
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import rx.Observable
import rx.lang.kotlin.onErrorReturnNull
import rx.schedulers.Schedulers
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
    lateinit var searchCategory: Category<ApplicationInfo>

    @Inject
    lateinit var appRepo: AppRepo
    @Inject
    lateinit var usageRepo: AppUsageRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        App.graph.inject(this)
        super.onCreate(savedInstanceState)
        // 启动应用时检查是否有反馈通知
        FeedbackAgent(this).sync()
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        refresher.isEnabled = true
        adapter.register(ApplicationInfo::class.java, AppViewProvider(this, usageRepo))
        adapter.register(Category::class.java, CategoryViewProvider())

        searchCategory = Category<ApplicationInfo>(null, adapter, items, getString(R.string.category_search_app))
        topUsageCategory = Category<ApplicationInfo>(searchCategory, adapter, items, getString(R.string.category_top_usage_app))
        otherAppCategory = Category<ApplicationInfo>(topUsageCategory, adapter, items, getString(R.string.category_other_app))

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

    override fun onRefresh() {
        val listStartMills = SystemClock.uptimeMillis()
        appRepo.list(SPAN_COUNT * 2)
                .bindToLifecycle(this)
                .onMain()
                .subscribe({
                    if (it.key) { // top
                        it.bindToLifecycle(this).toList().subscribe { topApps ->
                            topUsageCategory.items = topApps
                        }
                    } else {
                        it.bindToLifecycle(this).toList().subscribe { otherApps ->
                            otherAppCategory.items = otherApps
                        }
                    }
                    refresher.isRefreshing = false
                }, { error ->
                }, {
                    AVAnalytics.onEventDuration(this, EVENT_LIST_APP, SystemClock.uptimeMillis() - listStartMills)
                })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        RxSearchView.queryTextChangeEvents(searchView)
                .bindToLifecycle(searchView)
                .map { it.queryText() }
                .observeOn(Schedulers.computation())
                .flatMap { text ->
                    if (appRepo.scannedApps == null) {
                        return@flatMap Observable.just(null)
                    }
                    Observable.from(appRepo.scannedApps)
                            .filter {
                                if (text.isEmpty()) {
                                    return@filter false
                                }
                                return@filter packageManager.getApplicationLabel(it).contains(text, true)
                            }
                            .toList()
                            .onErrorReturnNull()
                }
                .onMain()
                .onErrorReturnNull()
                .subscribe {
                    searchCategory.items = it
                    recyclerView.scrollToPosition(0)
                }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                AVAnalytics.onEvent(this, EVENT_CLICK_ABOUT)
                AboutActivity.navTo(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
