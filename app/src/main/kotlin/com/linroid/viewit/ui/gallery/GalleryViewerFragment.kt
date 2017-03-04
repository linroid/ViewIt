package com.linroid.viewit.ui.gallery

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.ui.favorite.CreateFavoriteFragment
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.EVENT_ADD_FAVORITE
import com.linroid.viewit.utils.EVENT_DELETE_FAVORITE
import com.linroid.viewit.utils.PathUtils
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * @author linroid <linroid@gmail.com>
 * @since 05/02/2017
 */
abstract class GalleryViewerFragment : GalleryChildFragment() {
    private var favorite: Favorite? = null
    protected lateinit var path: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = arguments!!.getString(ARG_IMAGE_TREE_PATH)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteRepo.find(PathUtils.formatToVariable(path, appInfo), appInfo)
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
        menu.findItem(R.id.action_create_favorite)?.isVisible = favorite == null
        menu.findItem(R.id.action_delete_favorite)?.isVisible = favorite != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_create_favorite -> {
                AVAnalytics.onEvent(context, EVENT_ADD_FAVORITE, appInfo.packageName)
                showCreateFavoriteDialog()
                return true
            }
            R.id.action_delete_favorite -> {
                AVAnalytics.onEvent(context, EVENT_DELETE_FAVORITE, appInfo.packageName)
                alertDeleteFavorite()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreateFavoriteDialog() {
        CreateFavoriteFragment.newInstance(activity, path, appInfo)
                .show(childFragmentManager, "create_favorite:" + path)
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
        if (favorite != null) {
            favoriteRepo.delete(favorite!!, appInfo)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it) {
                            toastShort(R.string.msg_operate_success)
                        }
                    })
        }
    }
}