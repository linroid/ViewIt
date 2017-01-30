package com.linroid.viewit.ui

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.app.NavUtils
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
import com.linroid.viewit.R
import com.trello.rxlifecycle.components.support.RxAppCompatActivity

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */

abstract class BaseActivity : RxAppCompatActivity() {
    var toolbar: Toolbar? = null
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(provideContentLayoutId())
        toolbar = findViewById(R.id.toolbar) as? Toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        }
        val parent = NavUtils.getParentActivityName(this)
        if (parent != null && parent.isEmpty().not()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    @LayoutRes
    abstract fun provideContentLayoutId(): Int

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    protected fun toastShort(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    protected fun toastShort(@StringRes msgResId: Int) {
        Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show()
    }

    protected fun toastLong(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    protected fun toastLong(@StringRes msgResId: Int) {
        Toast.makeText(this, msgResId, Toast.LENGTH_LONG).show()
    }
}