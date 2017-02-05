package com.linroid.viewit.ui.gallery

import com.linroid.viewit.ui.BaseFragment
import timber.log.Timber

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
abstract class GalleryAbstractFragment : BaseFragment() {
    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
        setHasOptionsMenu(false)
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        setHasOptionsMenu(true)
    }
}