package com.linroid.viewit.ui

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import butterknife.bindView
import com.linroid.viewit.R

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
abstract class BaseListActivity : BaseActivity(), SwipeRefreshLayout.OnRefreshListener {

    val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    val refresher: SwipeRefreshLayout by bindView(R.id.refresher)

    final override fun provideContentLayoutId(): Int = R.layout.activity_base_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refresher.setOnRefreshListener(this)
        refresher.isEnabled = false
        setupRecyclerView(recyclerView)
        if (refresher.isEnabled) {
            refresher.isRefreshing = true
        }
        onRefresh()
    }

    abstract override fun onRefresh()

    abstract fun setupRecyclerView(recyclerView: RecyclerView)
}