package com.linroid.viewit.ui.home

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.linroid.viewit.R
import com.linroid.viewit.data.scanner.AppScanner
import com.linroid.viewit.ui.BaseListActivity
import com.linroid.viewit.ui.about.AboutActivity
import com.linroid.viewit.widget.divider.CategoryItemDecoration
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class HomeActivity : BaseListActivity() {
    val apps: MutableList<Any> = ArrayList();
    val adapter: MultiTypeAdapter = MultiTypeAdapter(apps)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadApps();
    }

    private fun loadApps() {
        AppScanner.scan(this)
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ app ->
                    apps.add(app)
                    adapter.notifyItemInserted(apps.size - 1);
                }, { error ->
                    Timber.e(error, "onError")
                }, {
                    Timber.d("onCompleted")
                })
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter.register(ApplicationInfo::class.java, AppViewProvider(this))
        val gridLayoutManager = GridLayoutManager(this, 4);
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
