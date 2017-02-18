package com.linroid.viewit.ui.gallery

import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.linroid.viewit.data.model.CloudFavorite
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.PathUtils

/**
 * @author linroid <linroid@gmail.com>
 * @since 05/02/2017
 */
class CloudFavoriteViewerFragment : TreeViewerFragment() {
    companion object {
        fun newInstance(cloudFavorite: CloudFavorite, appInfo: ApplicationInfo): CloudFavoriteViewerFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, PathUtils.formatToDevice(cloudFavorite.path, appInfo))
            val fragment = CloudFavoriteViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}