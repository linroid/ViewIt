package com.linroid.viewit.ioc

import com.linroid.viewit.ioc.module.GalleryModule
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.gallery.GalleryActivity
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
}