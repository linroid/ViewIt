package com.linroid.viewit.ui.gallery

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.ui.favorite.FavoriteCreateActivity
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * @author linroid <linroid@gmail.com>
 * @since 05/02/2017
 */
abstract class GalleryViewerFragment : GalleryChildFragment() {
    protected lateinit var path: String
    private var favorite: Favorite? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = arguments!!.getString(ARG_IMAGE_TREE_PATH)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoriteRepo.find(path, appInfo)
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    favorite = it
                    supportInvalidateOptionsMenu()
                }, { error ->
                    Timber.e(error)
                })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.gallery_viewer, menu)
        menu.findItem(R.id.action_create_favorite)?.isVisible = favorite == null || !favorite!!.isValid
        menu.findItem(R.id.action_delete_favorite)?.isVisible = favorite != null && favorite!!.isValid
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_create_favorite -> {
                FavoriteCreateActivity.navTo(activity, path, appInfo)
                return true
            }
            R.id.action_delete_favorite -> {
                alertDeleteFavorite()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun alertDeleteFavorite() {
        AlertDialog.Builder(activity)
                .setMessage(R.string.alert_delete_favorite)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, { dialogInterface: DialogInterface, i: Int ->
                    performDeleteFavorite()
                })
                .show()
    }

    private fun performDeleteFavorite() {
        favoriteRepo.delete(path, appInfo)
    }
}