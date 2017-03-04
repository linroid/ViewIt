package com.linroid.viewit.ui.gallery.provider

import android.content.pm.ApplicationInfo
import android.view.View
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.EVENT_CLICK_CLOUD_FAVORITE
import com.linroid.viewit.utils.EVENT_CLICK_TREE
import com.linroid.viewit.utils.FormatUtils
import com.linroid.viewit.utils.PathUtils

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
class ImageTreeViewProvider(activity: GalleryActivity, val visitPath: String, appInfo: ApplicationInfo, imageRepo: ImageRepo)
    : TreeViewProvider<ImageTree>(activity, appInfo, imageRepo) {

    override fun onClick(it: View, data: ImageTree) {
        AVAnalytics.onEvent(activity, EVENT_CLICK_TREE, mapOf("path" to data.dir, "packageName" to appInfo.packageName))
        activity.visitTree(data)
    }

    override fun obtainTree(data: ImageTree): ImageTree = data

    override fun obtainName(data: ImageTree): CharSequence = FormatUtils.formatPath(PathUtils.relative(visitPath, data.dir), appInfo)
}