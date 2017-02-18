package com.linroid.viewit.ui.gallery

import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.PathUtils

/**
 * @author linroid <linroid@gmail.com>
 * @since 05/02/2017
 */
class FavoriteViewerFragment : TreeViewerFragment() {
    companion object {
        fun newInstance(favorite: Favorite, appInfo: ApplicationInfo): FavoriteViewerFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, PathUtils.formatToDevice(favorite.path, appInfo))
            val fragment = FavoriteViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }

}