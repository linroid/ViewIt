package com.linroid.viewit.ui.gallery

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.FavoriteRepo
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.ui.BaseFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
abstract class GalleryChildFragment : BaseFragment() {

    @Inject lateinit protected var imageRepo: ImageRepo
    @Inject lateinit protected var favoriteRepo: FavoriteRepo
    @Inject lateinit protected var appInfo: ApplicationInfo
    @Inject lateinit protected var activity: GalleryActivity

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