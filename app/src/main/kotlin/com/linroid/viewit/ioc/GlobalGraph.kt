package com.linroid.viewit.ioc

import com.linroid.viewit.App
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.ioc.module.AndroidModule
import com.linroid.viewit.ioc.module.DataModule
import com.linroid.viewit.ioc.module.RepoModule
import com.linroid.viewit.ui.home.HomeActivity
import dagger.Component
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@Singleton
@Component(modules = arrayOf(AndroidModule::class, DataModule::class, RepoModule::class))
interface GlobalGraph {
    fun inject(app: App)
    fun inject(activity: HomeActivity)
    //    fun inject(activity: GalleryActivity)
    fun getImageRepo(): ImageRepo
}