package com.linroid.viewit.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.trello.rxlifecycle.components.support.RxFragment

/**
 * @author linroid <linroid@gmail.com>
 * @since 11/01/2017
 */
abstract class BaseFragment : RxFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(provideLayoutId(), container, false)
    }

    @LayoutRes
    abstract fun provideLayoutId(): Int

    protected fun supportInvalidateOptionsMenu() {
        activity.supportInvalidateOptionsMenu()
    }

    protected fun getSupportActionBar(): ActionBar? {
        if (activity != null && activity is AppCompatActivity) {
            return (activity as AppCompatActivity).supportActionBar
        }
        return null
    }


    protected fun toastShort(msg: String) {
        if (activity != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun toastShort(@StringRes msgResId: Int) {
        if (activity != null) {
            Toast.makeText(activity, msgResId, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun toastLong(msg: String) {
        if (activity != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        }
    }

    protected fun toastLong(@StringRes msgResId: Int) {
        if (activity != null) {
            Toast.makeText(activity, msgResId, Toast.LENGTH_LONG).show()
        }
    }
}