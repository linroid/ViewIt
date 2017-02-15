package com.linroid.viewit.ui

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import butterknife.bindView
import com.linroid.viewit.R

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
abstract class BaseListActivity : BaseActivity() {
    val recyclerView: RecyclerView by bindView(R.id.recyclerView)

    final override fun provideContentLayoutId(): Int = R.layout.activity_base_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerView(recyclerView)
    }

    abstract fun setupRecyclerView(recyclerView: RecyclerView)
}