package com.linroid.viewit.ioc

import com.linroid.viewit.ioc.module.GalleryModule
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.gallery.ImagesViewerFragment
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.ui.gallery.TreeViewerFragment
import com.linroid.viewit.ui.gallery.SummaryFragment
import dagger.Component

/**
 * @author linroid <linroid@gmail.com>
 * @since 24/01/2017
 */
@ActivityScope
@Component(
        modules = arrayOf(GalleryModule::class),
        dependencies = arrayOf(GlobalGraph::class)
)
interface GalleryGraph {
    fun inject(activity: GalleryActivity)
    fun inject(fragment: TreeViewerFragment)
    fun inject(fragment: ImagesViewerFragment)
    fun inject(fragment: SummaryFragment)
}