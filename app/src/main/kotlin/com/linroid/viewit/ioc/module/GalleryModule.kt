package com.linroid.viewit.ioc.module

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.ScanRepo
import com.linroid.viewit.data.ScanRepoManager
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
    fun provideRepo(repoManager: ScanRepoManager): ScanRepo {
        return repoManager.getRepo(info)
    }
}