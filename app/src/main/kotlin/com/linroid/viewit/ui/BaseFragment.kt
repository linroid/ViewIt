package com.linroid.viewit.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author linroid <linroid@gmail.com>
 * @since 11/01/2017
 */
abstract class BaseFragment : Fragment() {

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
}