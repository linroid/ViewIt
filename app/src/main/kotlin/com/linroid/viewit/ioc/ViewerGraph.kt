package com.linroid.viewit.ioc

import com.linroid.viewit.ioc.module.ViewerModule
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.viewer.ImageViewerActivity
import com.linroid.viewit.ui.viewer.ImageViewerFragment
import dagger.Component

/**
 * @author linroid <linroid@gmail.com>
 * @since 24/01/2017
 */
@ActivityScope
@Component(
        modules = arrayOf(ViewerModule::class),
        dependencies = arrayOf(GlobalGraph::class)
)
interface ViewerGraph {
    fun inject(activity: ImageViewerActivity)
    fun inject(fragment: ImageViewerFragment)
}