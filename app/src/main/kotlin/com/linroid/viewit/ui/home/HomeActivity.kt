package com.linroid.viewit.ui.home

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.scanner.AppScanner
import com.linroid.viewit.ui.BaseActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class HomeActivity : BaseActivity() {
    val appListView: RecyclerView by bindView(R.id.recycler)
    val apps: MutableList<Any> = ArrayList();
    val adapter: MultiTypeAdapter = MultiTypeAdapter(apps)

    override fun provideContentLayoutId(): Int {
        return R.layout.activity_home;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView();
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

    private fun initView() {
        adapter.register(ApplicationInfo::class.java, AppViewProvider(this))
        val gridLayoutManager = GridLayoutManager(this, 4);
        appListView.layoutManager = gridLayoutManager
        appListView.adapter = adapter
        appListView.itemAnimator = DefaultItemAnimator()
        appListView.setHasFixedSize(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
