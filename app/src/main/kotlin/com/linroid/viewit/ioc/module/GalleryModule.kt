package com.linroid.viewit.ioc.module

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.data.repo.ImageRepoManager
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.gallery.GalleryActivity
import dagger.Module
import dagger.Provides
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 24/01/2017
 */
@Module
class GalleryModule(val activity: GalleryActivity, val info: ApplicationInfo) {
    @Provides
    @ActivityScope
    fun provideInfo(): ApplicationInfo = info

    @Provides
    @ActivityScope
    fun provideGalleryActivity(): GalleryActivity = activity;

    @Provides
    @ActivityScope
    fun provideImages(): MutableList<Any> = ArrayList()

    @Provides
    @ActivityScope
    fun provideRepo(repoManager: ImageRepoManager): ImageRepo {
        return repoManager.getRepo(info)
    }
}