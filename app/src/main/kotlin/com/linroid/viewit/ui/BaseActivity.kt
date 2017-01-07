package com.linroid.viewit.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v7.widget.Toolbar
import com.linroid.viewit.R
import com.trello.rxlifecycle.components.support.RxAppCompatActivity

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */

abstract class BaseActivity : RxAppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(provideContentView())
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    @LayoutRes
    abstract fun provideContentView(): Int
}