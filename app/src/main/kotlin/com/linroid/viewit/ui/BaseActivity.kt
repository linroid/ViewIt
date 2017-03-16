package com.linroid.viewit.ui

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.app.NavUtils
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.avos.avoscloud.AVAnalytics
import com.avos.avoscloud.feedback.FeedbackAgent
import com.linroid.viewit.R
import com.linroid.viewit.utils.EVENT_CLICK_FEEDBACK
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

    override fun onPause() {
        super.onPause()
        AVAnalytics.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        AVAnalytics.onResume(this)
    }

    @LayoutRes
    abstract fun provideContentLayoutId(): Int

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.feedback, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_feedback -> {
                AVAnalytics.onEvent(this, EVENT_CLICK_FEEDBACK)
                val agent = FeedbackAgent(this);
                agent.startDefaultThreadActivity();
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun toastShort(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun toastShort(@StringRes msgResId: Int) {
        Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun toastLong(@StringRes msgResId: Int) {
        Toast.makeText(this, msgResId, Toast.LENGTH_LONG).show()
    }
}