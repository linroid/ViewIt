package com.linroid.viewit.ui.gallery.provider

import android.content.pm.ApplicationInfo
import android.view.View
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.data.model.CloudFavorite
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.EVENT_CLICK_CLOUD_FAVORITE
import com.linroid.viewit.utils.EVENT_CLICK_FAVORITE

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/02/2017
 */
class CloudFavoriteViewProvider(activity: GalleryActivity, appInfo: ApplicationInfo, imageRepo: ImageRepo)
    : TreeViewProvider<CloudFavorite>(activity, appInfo, imageRepo) {
    override fun onClick(it: View, data: CloudFavorite) {
        AVAnalytics.onEvent(activity, EVENT_CLICK_CLOUD_FAVORITE, mapOf("name" to data.name, "packageName" to appInfo.packageName))
        activity.visitCloudFavorite(data)
    }

    override fun obtainTree(data: CloudFavorite): ImageTree? {
        return data.tree
    }

    override fun obtainName(data: CloudFavorite): CharSequence = data.name
}